package specular.systems;

import com.google.zxing.Result;

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
}
