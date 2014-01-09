package specular.systems.activities;

import android.widget.TextView;

import zxing.Result;

import specular.systems.KeysDeleter;
import specular.systems.R;
import zxing.scanqr.CaptureActivity;

public class StartScan extends CaptureActivity {


    final static int MESSAGE = 0, CONTACT = 1, PRIVATE = 2;

    @Override
    public void handleDecode(Result rawResult) {
        getIntent().putExtra("barcode", rawResult.getText());
        setResult(RESULT_OK, getIntent());
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onStart() {
        super.onStart();
        int type = getIntent().getIntExtra("type", CONTACT);
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
