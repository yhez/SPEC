package specular.systems.activities;

import android.widget.TextView;

import specular.systems.CryptMethods;
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
        } else {
            if (rawResult.getText() != null) {
                getIntent().putExtra("barcode", rawResult.getText());
                getIntent().putExtra("id",id);
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
    public void onStart() {
        super.onStart();
        type = getIntent().getIntExtra("type", CONTACT);
        id = getIntent().getLongExtra("id",-1);
        if (type == MESSAGE)
            ((TextView) findViewById(R.id.status_view)).setText(R.string.decrypt_qr_message_explain);
        else if (type == PRIVATE) {
            ((TextView) findViewById(R.id.status_view)).setText(R.string.private_scan_explain);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter();
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
    }
}
