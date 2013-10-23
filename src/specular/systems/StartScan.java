package specular.systems;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.zxing.Result;

import specular.systems.scanqr.CaptureActivity;

public class StartScan extends CaptureActivity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode) {
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
