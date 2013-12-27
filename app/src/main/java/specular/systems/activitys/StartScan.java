package specular.systems.activitys;

import android.widget.TextView;

import com.google.zxing.Result;

import specular.systems.CryptMethods;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.scanqr.CaptureActivity;

public class StartScan extends CaptureActivity {
    boolean end = false;

    @Override
    public void handleDecode(Result rawResult) {
        getIntent().putExtra("barcode", rawResult.getText());
        setResult(RESULT_OK, getIntent());
        end = true;
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

    @Override
    public void onPause() {
        super.onPause();
        if (!end) {
            StaticVariables.currentKeys = CryptMethods.privateExist() && CryptMethods.publicExist() ? 0 : CryptMethods.publicExist() ? 1 : CryptMethods.privateExist() ? 2 : 3;
            CryptMethods.deleteKeys();
        }
    }
}
