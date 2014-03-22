package specular.systems.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.print.PrintHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import specular.systems.CryptMethods;
import specular.systems.Dialogs.NotImplemented;
import specular.systems.Dialogs.TurnNFCOn;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.NfcStuff;
import specular.systems.R;
import specular.systems.Visual;
import zxing.QRCodeEncoder;


public class PrivateKeyManager extends Activity {
    final int NO_CHOICE = 0, MOVE_TO_NFC = 2, GET_FROM_NFC = 3;
    int status = NO_CHOICE;
    Button[] bt;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.private_key_manager);
        bt = new Button[4];
        bt[0] = (Button) findViewById(R.id.p_button1);
        bt[1] = (Button) findViewById(R.id.p_button2);
        bt[2] = (Button) findViewById(R.id.p_button3);
        bt[3] = (Button) findViewById(R.id.p_button4);
        updateViews();
        Visual.setAllFonts(this, (ViewGroup) findViewById(android.R.id.content));
    }


    private void updateViews() {
        if (status == NO_CHOICE) {
            for (Button b : bt) {
                b.setTextColor(getResources().getColor(R.color.spec_black));
            }
            if (!CryptMethods.privateExist()) {
                disableButton(bt[0], R.string.cant_find_key_on_device);
                disableButton(bt[3], R.string.cant_find_private_key);
            }
            if (NfcStuff.nfcIsntAvailable(this)) {
                disableButton(bt[0], R.string.cant_connect_nfc_adapter);
                disableButton(bt[1], R.string.cant_connect_nfc_adapter);
            } else if (NfcStuff.nfcIsOff(this)) {
                disabledNFC(bt[0]);
                disabledNFC(bt[1]);
            }
            if (!PrintHelper.systemSupportsPrint()) {
                disableButton(bt[3], R.string.no_support_print);
            }
        }
    }

    private void disabledNFC(Button b) {
        b.setTextColor(getResources().getColor(R.color.spec_gray));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TurnNFCOn(getFragmentManager());
            }
        });
    }

    private void disableButton(Button b, final int msg) {
        b.setTextColor(getResources().getColor(R.color.spec_gray));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Visual.toast(PrivateKeyManager.this, msg);
            }
        });
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        switch (status) {
            case GET_FROM_NFC:
                byte[] raw = NfcStuff.getData(i);
                if (raw != null) {
                    if (CryptMethods.setPrivate(raw)) {
                        FilesManagement.savePrivate(this);
                        Visual.toast(this,R.string.keys_moved_to_nfc);
                        finish();
                    } else {
                        Visual.toast(this,R.string.cant_find_private_key);
                    }
                } else {
                    Visual.toast(this,R.string.cant_find_data);
                }
                break;
            case MOVE_TO_NFC:
                int result = NfcStuff.write(i,CryptMethods.getPrivateToSave());
                Visual.toast(this,result);
                if(result==R.string.tag_written) {
                    FilesManagement.removePrivate(this);
                    finish();
                }
                break;
            case NO_CHOICE:
                raw = NfcStuff.getData(i);
                if (raw != null) {
                    if (CryptMethods.setPrivate(raw)) {
                        updateViews();
                    } else {
                        Visual.toast(this,R.string.cant_find_private_key);
                    }
                } else {
                    Visual.toast(this,R.string.cant_find_data);
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter(this);
    }

    @Override
    public void onBackPressed() {
        if (status == NO_CHOICE) {
            super.onBackPressed();
        } else {
            Visual.showAllChildes(this, (ViewGroup) findViewById(android.R.id.content));
            findViewById(R.id.text_view_divide).setVisibility(View.GONE);
            findViewById(R.id.p_text).setVisibility(View.GONE);
            status = NO_CHOICE;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
        NfcStuff.listen(this,getClass());
        updateViews();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void onClick(View v) {
        final TextView tv = (TextView) findViewById(R.id.p_text);
        final View divider = findViewById(R.id.text_view_divide);
        if (status != NO_CHOICE) {
            Visual.showAllChildes(this, (ViewGroup) findViewById(android.R.id.content));
            divider.setVisibility(View.GONE);
            tv.setVisibility(View.GONE);
            status = NO_CHOICE;
        } else {
            switch (v.getId()) {
                case R.id.p_button1:
                    status = MOVE_TO_NFC;
                    Visual.hideAllChildes((ViewGroup) findViewById(android.R.id.content));
                    tv.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                    tv.setText(R.string.tab_nfc_move_to_nfc);
                    v.setVisibility(View.VISIBLE);
                    break;
                case R.id.p_button2:
                    status = GET_FROM_NFC;
                    Visual.hideAllChildes((ViewGroup) findViewById(android.R.id.content));
                    tv.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                    tv.setText(R.string.tab_nfc_get_from_nfc);
                    v.setVisibility(View.VISIBLE);
                    break;
                case R.id.p_button3:
                    Intent i = new Intent(this, StartScan.class);
                    i.putExtra("type", StartScan.PRIVATE);
                    startActivityForResult(i, Main.SCAN_PRIVATE);
                    break;
                case R.id.p_button4:
                    try {
                        byte[] p = CryptMethods.getPrivateToSave();
                        String key = Visual.bin2hex(p);
                        QRCodeEncoder qr = new QRCodeEncoder(key, 512);
                        Bitmap b = qr.encodeAsBitmap();
                        PrintHelper ph = new PrintHelper(this);
                        ph.setColorMode(PrintHelper.COLOR_MODE_MONOCHROME);
                        ph.setOrientation(PrintHelper.ORIENTATION_PORTRAIT);
                        ph.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                        ph.printBitmap("PK-backup-", b);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    new NotImplemented(getFragmentManager());
            }
        }
    }

    @Override
    public void onActivityResult(int req, int res, Intent i) {
        super.onActivityResult(req, res, i);
        if (res == RESULT_OK) {
            if (CryptMethods.privateExist()) {
                FilesManagement.savePrivate(this);
                Visual.toast(this, R.string.private_key_loaded_from_qr);
            }
        }
    }
}
