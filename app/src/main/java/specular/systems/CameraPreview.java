package specular.systems;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {
    public int currentSensor;
    public int progress = 0;
    private SurfaceHolder mHolder;
    public Camera mCamera;
    private SensorManager mSensorManager;
    private ArrayList<Sensor> sensors;
    public String[] names;

    public CameraPreview(Context context) {
        super(context);
        cp = this;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        if (mHolder != null) {
            mHolder.addCallback(this);
        }
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> temp = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        sensors = new ArrayList<Sensor>();
        sensors.add(null);
        for (Sensor s : temp) {
            if (s.getType() != Sensor.TYPE_PROXIMITY && s.getType() != Sensor.TYPE_LIGHT && s.getType() != Sensor.TYPE_SIGNIFICANT_MOTION) {
                sensors.add(s);
            }
        }
        names = new String[sensors.size()];
        for (int a = 1; a < names.length; a++)
            names[a] = sensors.get(a).getName();
        names[0] = context.getString(R.string.sensor_camera);
        currentSensor = 0;
        sensorsData = new byte[sensors.size()][64];
        dataCollected = new int[sensors.size()];
        for (int i = 0; i < dataCollected.length; i++)
            dataCollected[i] = 0;
    }

    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera = getCameraInstance();
            mCamera.setPreviewDisplay(holder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException ignored) {
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
            mCamera.setPreviewCallback(callback);

        } catch (Exception ignored) {
        }
    }

    private Camera.PreviewCallback callback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            int block = data.length / 64;
            if (progress == 0) {
                for (int a = 0; a < 64; a++) {
                    sensorsData[0][a] = data[block * a];
                }
                progress += 15;
            } else if (progress < 63) {
                for (int a = 0; a < 64; a++) {
                    sensorsData[0][a] = (byte) (sensorsData[0][a] ^ data[block * a]);
                }
                progress += 15;
            } else {
                progress = 0;
                currentSensor++;
                mCamera.setPreviewCallback(null);
                mSensorManager.registerListener(CameraPreview.this, sensors.get(currentSensor), 15);
            }
        }
    };
    private byte[][] sensorsData;
    private int[] dataCollected;

    public void reset() {
        progress = 0;
        currentSensor = 0;
        for (int a = 0; a < dataCollected.length; a++)
            dataCollected[a] = 0;
        for (int a = 0; a < sensorsData.length; a++)
            for (int b = 0; b < sensorsData[a].length; b++)
                sensorsData[a][b] = 0;
        mCamera.setPreviewCallback(callback);
        ready = false;
    }

    public byte[] getData() {
        byte[] data = new byte[64];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(data);
        for (byte[] sensorData : sensorsData)
            for (int a = 0; a < 64; a++)
                data[a] = (byte) (data[a] ^ sensorData[a]);
        reset();
        return data;
    }

    private static CameraPreview cp;

    public static CameraPreview getCameraPreview() {
        return cp;
    }

    public boolean ready = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        progress++;
        int i = sensors.indexOf(event.sensor);
        dataCollected[i]++;
        if (dataCollected[i] >= 63) {
            mSensorManager.unregisterListener(this);
            if (i + 1 == sensors.size()) {
                ready = true;
            } else {
                progress = 0;
                currentSensor++;
                mSensorManager.registerListener(this, sensors.get(i + 1), 15);
            }
        } else
            sensorsData[i][dataCollected[i]] = (byte) (event.values[0] * 10000.0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
