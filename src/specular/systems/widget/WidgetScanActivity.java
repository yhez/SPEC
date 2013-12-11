package specular.systems.widget;

import android.widget.TextView;

import com.google.zxing.Result;

import specular.systems.CryptMethods;
import specular.systems.Dialogs.ProgressDlg;
import specular.systems.FilesManagement;
import specular.systems.PublicContactCard;
import specular.systems.PublicStaticVariables;
import specular.systems.R;
import specular.systems.scanqr.CaptureActivity;

/**
 * Created by yehezkelk on 12/11/13.
 */
public class WidgetScanActivity extends CaptureActivity {
    @Override
    public void handleDecode(final Result rawResult) {
        if(rawResult!=null){
        PublicContactCard pcc = new PublicContactCard(this,rawResult.getText());
        if(pcc.getPublicKey()!=null){
            //todo add contact
        }else {
            final ProgressDlg prgd = new ProgressDlg(this, R.string.decrypting);
            prgd.setCancelable(false);
            prgd.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    CryptMethods.decrypt(rawResult.getText());
                    FilesManagement.deleteTempDecryptedMSG(WidgetScanActivity.this);
                    PublicStaticVariables.message = null;
                    //todo show decrypted msg
                    prgd.cancel();
                }
            }).start();
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
