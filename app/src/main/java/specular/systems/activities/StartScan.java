package specular.systems.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.KeysDeleter;
import specular.systems.R;
import specular.systems.Visual;
import zxing.Result;
import zxing.scanqr.CaptureActivity;

public class StartScan extends CaptureActivity {
    public final static int MESSAGE = 0, CONTACT = 1, PRIVATE = 2;

    @Override
    public void handleDecode(Result rawResult) {
        if (type == PRIVATE) {
            if (!CryptMethods.setPrivate(Visual.hex2bin(rawResult.getText())))
                setResult(RESULT_CANCELED);
            else
                setResult(RESULT_OK);
        } else if (type == -1) {
            try {
                File path = getFilesDir();
                File file = new File(path, "temp_open_by_widget.spec");
                OutputStream os = new FileOutputStream(file);
                os.write(rawResult.getText().getBytes());
                os.close();
                Intent i = new Intent(this, Splash.class);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.fromFile(file));
                finish();
                startActivity(i);
            } catch (Exception ignore) {
            }
        } else {
            if (rawResult.getText() != null) {
                getIntent().putExtra("barcode", rawResult.getText());
                getIntent().putExtra("id", id);
                setResult(RESULT_OK, getIntent());
            } else {
                setResult(RESULT_CANCELED);
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    int type;
    long id;
    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        type = getIntent().getIntExtra("type", -1);
        id = getIntent().getLongExtra("id", -1);
        if (id != -1)
            ((TextView) findViewById(R.id.status_view)).setText(R.string.decrypt_qr_message_explain);
        else if (type != PRIVATE && CryptMethods.publicExist())
            ((TextView) findViewById(R.id.status_view)).setText(R.string.scan_any_qr_widget);
        else if (!CryptMethods.publicExist()) {
            ((TextView) findViewById(R.id.status_view)).setText(R.string.decrypt_qr_message_explain);
        } else {
            ((TextView) findViewById(R.id.status_view)).setText(R.string.private_scan_explain);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
    }
}
