package specular.systems;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Splash extends Activity {
    final private static int TIME_FOR_SPLASH = 3500;
    private final Thread waitForSplash = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                try {
                    wait(TIME_FOR_SPLASH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(Splash.this, Main.class);
            if (message != null)
                intent.putExtra("message", message);
            startActivity(intent);
            finish();
        }
    });
    private String message = null;

    void go() {
        boolean newUser = FilesManegmant.isItNewUser(this);
        if (newUser) {
            setContentView(R.layout.splash);
            ((TextView) findViewById(R.id.company)).setTypeface(FilesManegmant.getOs(this));
            findViewById(R.id.splash).animate().setDuration(TIME_FOR_SPLASH).alpha(1).start();
            waitForSplash.start();
        }
        FilesManegmant.getKeysFromSdcard(this);
        if (!CryptMethods.privateExist() && getIntent().getType() != null) {
            Parcelable raw[] = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (raw != null) {
                NdefMessage msg = (NdefMessage) raw[0];
                NdefRecord pvk = msg.getRecords()[0];
                CryptMethods.setPrivate(Visual.bin2hex(pvk
                        .getPayload()));
            }
        }
        if (!newUser) {
            Intent intent = new Intent(Splash.this, Main.class);
            if (message != null) {
                intent.putExtra("message", message);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Intent thisIntent = getIntent();
        if (thisIntent != null && thisIntent.getType() != null
                && thisIntent.getType().equals("application/octet-stream")
                && thisIntent.getData() != null) {
            QRPublicKey qrp;
            Uri uri = getIntent().getData();
            if (uri != null) {
                String data = null;
                ContentResolver cr = getBaseContext().getContentResolver();
                InputStream is;
                try {
                    is = cr.openInputStream(uri);
                    StringBuilder buf = new StringBuilder();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(is));
                    String str;
                    try {
                        while ((str = reader.readLine()) != null) {
                            buf.append(str).append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    buf.deleteCharAt(buf.length() - 1);
                    data = buf.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (data != null) {
                    qrp = new QRPublicKey(this, data);
                    if (qrp.getPublicKey() != null) {
                        //TODO create dialog before save
                        ContactsDataSource cds = new ContactsDataSource(this);
                        cds.open();
                        Contact c = cds.findContact(qrp.getPublicKey());
                        cds.close();
                        if (c == null) {
                            c = new Contact(this, qrp.getName(), qrp.getEmail(),
                                    qrp.getPublicKey());
                            Toast.makeText(
                                    getBaseContext(),
                                    c
                                            + "\n"
                                            + getResources().getString(
                                            R.string.contact_saved),
                                    Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getBaseContext(),
                                    R.string.contact_exist, Toast.LENGTH_LONG)
                                    .show();
                        getIntent().setData(null);
                        go();
                    } else {
                        message = data;
                        go();
                    }
                }
                else
                    Toast.makeText(getBaseContext(), R.string.failed,
                            Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getBaseContext(), R.string.failed,
                        Toast.LENGTH_LONG).show();
        } else
            go();
    }
}
