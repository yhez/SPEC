package specular.systems.widget;

import android.content.Intent;
import android.net.Uri;
import android.widget.TextView;

import com.google.zxing.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import specular.systems.R;
import specular.systems.activitys.Splash;
import specular.systems.scanqr.CaptureActivity;

/**
 * Created by yehezkelk on 12/11/13.
 */
public class WidgetScanActivity extends CaptureActivity {

    @Override
    public void handleDecode(Result rawResult) {
        if (rawResult != null) {
            try {
                File path = getFilesDir();
                File file = new File(path, "temp_open_by_widget");
                OutputStream os = new FileOutputStream(file);
                os.write(rawResult.getText().getBytes());
                os.close();
                Intent i = new Intent(this, Splash.class);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.fromFile(file));
                finish();
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((TextView) findViewById(R.id.status_view)).setText(R.string.scan_any_qr_widget);
    }
}
