package specular.systems.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.print.PrintHelper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

import specular.systems.CryptMethods;
import specular.systems.Dialogs.NotImplemented;
import specular.systems.Dialogs.TurnNFCOn;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.R;
import specular.systems.Visual;
import zxing.QRCodeEncoder;


public class PrivateKeyManager extends Activity {
    final int NO_CHOICE = 0, ERASE = 1, MOVE_TO_NFC = 2, GET_FROM_NFC = 3;
    int status = NO_CHOICE;
    Button bt1;
    Button bt2;
    Button bt3;
    Button bt4;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.private_key_manager);
        bt1 = (Button) findViewById(R.id.p_button1);
        bt2 = (Button) findViewById(R.id.p_button2);
        bt3 = (Button) findViewById(R.id.p_button3);
        bt4 = (Button) findViewById(R.id.p_button4);
        updateViews();
        Visual.setAllFonts(this, (ViewGroup) findViewById(android.R.id.content));
    }


    private void updateViews() {
        if (status == NO_CHOICE)
            if (!CryptMethods.privateExist()) {
                disableButton(bt1, R.string.cant_find_key_on_device);
                disableButton(bt4, R.string.cant_find_private_key);
            }
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            disableButton(bt1, R.string.cant_connect_nfc_adapter);
            disableButton(bt2, R.string.cant_connect_nfc_adapter);
        } else if (nfcAdapter.isEnabled()) {
            disabledNFC(bt1);
            disabledNFC(bt2);
        }
        if (!PrintHelper.systemSupportsPrint()) {
            disableButton(bt4, R.string.no_support_print);
        }
    }

    private void disabledNFC(Button b) {
        b.setTextColor(getResources().getColor(R.color.spec_gray));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TurnNFCOn tno = new TurnNFCOn();
                tno.show(getFragmentManager(), "nfc");
            }
        });
    }

    private void disableButton(Button b, final int msg) {
        b.setTextColor(getResources().getColor(R.color.spec_gray));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast t = Toast.makeText(PrivateKeyManager.this, msg, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        });
    }

    @Override
    public void onNewIntent(Intent i) {
        super.onNewIntent(i);
        Toast t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER, 0, 0);
        switch (status) {
            case ERASE:
                break;
            case GET_FROM_NFC:
                Parcelable raw[] = i.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (raw != null) {
                    NdefMessage msg = (NdefMessage) raw[0];
                    NdefRecord pvk = msg.getRecords()[0];
                    if (CryptMethods.setPrivate(pvk
                            .getPayload())) {
                        FilesManagement.savePrivate(this);
                        t.setText(R.string.keys_moved_to_nfc);
                        t.show();
                        finish();
                    } else {
                        t.setText(R.string.cant_find_private_key);
                        t.show();
                    }
                } else {
                    t.setText(R.string.cant_find_data);
                    t.show();
                }
                break;
            case MOVE_TO_NFC:
                Tag tag = i.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    byte[] bin = CryptMethods.getPrivateToSave();
                    // record to launch Play Store if app is not installed
                    NdefRecord appRecord = NdefRecord
                            .createApplicationRecord(this.getPackageName());
                    byte[] mimeBytes = ("application/" + this.getPackageName())
                            .getBytes(Charset.forName("US-ASCII"));
                    NdefRecord cardRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                            mimeBytes, new byte[0], bin);
                    NdefMessage message = new NdefMessage(new NdefRecord[]{cardRecord,
                            appRecord});
                    Ndef ndef = null;
                    try {
                        // see if tag is already NDEF formatted
                        ndef = Ndef.get(tag);
                        if (ndef != null) {
                            ndef.connect();
                            if (!ndef.isWritable()) {
                                t.setText(R.string.failed_read_only);
                            } else {
                                // work out how much space we need for the data
                                int size = message.toByteArray().length;
                                if (ndef.getMaxSize() < size) {
                                    ndef.close();
                                    t.setText(R.string.too_small_tag);
                                } else {
                                    ndef.writeNdefMessage(message);
                                    t.setText(R.string.tag_written);
                                    FilesManagement.removePrivate(this);
                                    finish();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (ndef != null && ndef.isConnected())
                            try {
                                ndef.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        NdefFormatable format = NdefFormatable.get(tag);
                        if (format != null) {
                            try {
                                format.connect();
                                format.format(message);
                                t.setText(R.string.tag_formatted);
                            } catch (IOException ew) {
                                t.setText(getString(R.string.io_exception_format));
                            } catch (FormatException e1) {
                                t.setText(R.string.cant_format);
                            }
                        } else {
                            t.setText(R.string.tag_not_supported);
                        }
                    }
                    t.show();
                }
                break;
            case NO_CHOICE:
                raw = i.getParcelableArrayExtra(
                        NfcAdapter.EXTRA_NDEF_MESSAGES);
                if (raw != null) {
                    NdefMessage msg = (NdefMessage) raw[0];
                    NdefRecord pvk = msg.getRecords()[0];
                    if (CryptMethods.setPrivate(pvk
                            .getPayload())) {
                        updateViews();
                    } else {
                        t.setText(R.string.cant_find_private_key);
                        t.show();
                    }
                } else {
                    t.setText(R.string.cant_find_data);
                    t.show();
                }
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter();
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
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            PendingIntent pi = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass())
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            );
            IntentFilter tagDetected = new IntentFilter(
                    NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter[] filters = new IntentFilter[]{tagDetected};
            nfcAdapter.enableForegroundDispatch(this, pi, filters, null);
        }
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
                    NotImplemented nimp = new NotImplemented();
                    nimp.show(getFragmentManager(), "nimp");
            }
        }
    }
    @Override
    public void onActivityResult(int req, int res, Intent i) {
        super.onActivityResult(req, res, i);
        if (res == RESULT_OK) {
            if (CryptMethods.privateExist()) {
                FilesManagement.savePrivate(this);
                Toast t = Toast.makeText(this, R.string.private_key_loaded_from_qr, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        }
    }
}
