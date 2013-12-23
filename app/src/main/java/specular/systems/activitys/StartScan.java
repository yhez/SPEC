package specular.systems.activitys;

import android.widget.TextView;

import com.google.zxing.Result;

import specular.systems.R;
import specular.systems.scanqr.CaptureActivity;

public class StartScan extends CaptureActivity {

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
        if (getIntent().getBooleanExtra("decrypt", false))
            ((TextView) findViewById(R.id.status_view)).setText(getString(R.string.decrypt_qr_message_explain));
    }
}
