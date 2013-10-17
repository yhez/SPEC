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
    private
    final Thread getKeys = new Thread(new Runnable() {
        @Override
        public void run() {
            new FilesManegmant(Splash.this).get();
            if (CryptMethods.myPrivateKey != null)
                privateKey = true;
        }
    });
    Thread waitForSplash = new Thread(new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                try {
                    wait(TIME_FOR_SPLASH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(Splash.this, Wmain.class);
            if (message != null)
                intent.putExtra("message", message);
            startActivity(intent);
            finish();
        }
    });
    private String message = null;
    private boolean privateKey = false;
    boolean newUser;
    void go() {
        newUser=FilesManegmant.isItNewUser(this);
        if (newUser) {
            setContentView(R.layout.splash);
            ((TextView) findViewById(R.id.company)).setTypeface(FilesManegmant.getOs(this));
            findViewById(R.id.splash).animate().setDuration(TIME_FOR_SPLASH).alpha(1);
            waitForSplash.start();
        }
        CryptMethods.addProviders();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (getKeys.isAlive()) {
                    synchronized (this) {
                        try {
                            wait(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!privateKey && getIntent().getType() != null) {
                    Parcelable raw[] = getIntent().getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES);
                    if (raw != null) {
                        NdefMessage msg = (NdefMessage) raw[0];
                        NdefRecord pvk = msg.getRecords()[0];
                        CryptMethods.myPrivateKey = Visual.bin2hex(pvk
                                .getPayload());
                        privateKey = true;
                    }
                }
                if (privateKey)
                    if (!CryptMethods.formatPrivate()) {
                        privateKey = false;
                        CryptMethods.myPrivateKey = null;
                    }
                if (!newUser) {
                    Intent intent = new Intent(Splash.this, Wmain.class);
                    if (message != null)
                        intent.putExtra("message", message);
                    startActivity(intent);
                    finish();
                }
            }
        }).start();
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        /*Intent inte = new Intent(Splash.this,StartScan.class);
		startActivity(inte);*/
        Intent thisIntent = getIntent();
        getKeys.start();
        if (thisIntent != null && thisIntent.getType() != null
                && thisIntent.getType().equals("application/octet-stream")
                && thisIntent.getData() != null) {
            QRPublicKey qrp;
            Uri uri = getIntent().getData();
            if (uri != null) {
                String data;
                ContentResolver cr = getBaseContext().getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
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
                qrp = new QRPublicKey(this, data);
                if (qrp.getPublicKey() != null) {
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
                    // TODO delete the private if he already loaded
                    finish();
                } else {
                    message = data;
                    go();
                }
            } else
                Toast.makeText(getBaseContext(), R.string.failed,
                        Toast.LENGTH_LONG).show();
        } else
            go();
    }
}
