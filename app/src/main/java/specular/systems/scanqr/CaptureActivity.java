package specular.systems.scanqr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Collection;

import specular.systems.R;
import specular.systems.scanqr.camera.CameraManager;

public class CaptureActivity extends Activity implements SurfaceHolder.Callback {
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                // Creating the handler starts the preview_direct_msg, which can also throw a RuntimeException.
                try {
                    if (handler == null) {
                        handler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats, characterSet, cameraManager);
                    }
                } catch (RuntimeException r) {
                    displayFrameworkBugMessageAndExit();
                }
            } else displayFrameworkBugMessageAndExit();
        }
    };
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;

    ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        cameraManager.onF(event.getActionMasked() != MotionEvent.ACTION_UP);
        return true;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.capture);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the learn on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
        handler = null;
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
        inactivityTimer.onResume();
        Intent intent = getIntent();
        decodeFormats = null;
        characterSet = null;
        decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
        characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!hasSurface) {
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (surfaceHolder != null) {
                surfaceHolder.removeCallback(this);
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void handleDecode(Result rawResult) {
    }

    private void initCamera(final SurfaceHolder surfaceHolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraManager.openDriver(surfaceHolder);
                    hndl.sendEmptyMessage(0);
                } catch (IOException e) {
                    hndl.sendEmptyMessage(1);
                }
            }
        }).start();
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_scan_error_msg));
        builder.setMessage(getString(R.string.content_scan_error_msg));
        builder.setPositiveButton(getString(R.string.ok), new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }
}
