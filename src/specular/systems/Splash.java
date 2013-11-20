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
    private final static int TIME_FOR_SPLASH = 3500;
    private final static long TIME_FOR_CLEAR_TASK = 15;//in minute
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
            startActivity(intent);
            finish();
        }
    });

    @Override
    public void onBackPressed() {

    }

    void go() {
        boolean newUser = FilesManagement.isItNewUser(this);
        if (newUser) {
            setContentView(R.layout.splash);
            ((TextView) findViewById(R.id.company)).setTypeface(FilesManagement.getOs(this));
            findViewById(R.id.splash).animate().setDuration(TIME_FOR_SPLASH).alpha(1).start();
            waitForSplash.start();
        }
        FilesManagement.getKeysFromSDCard(this);
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
            if (PublicStaticVariables.message != null || PublicStaticVariables.fileContactCard != null
                    || PublicStaticVariables.time == null || (System.currentTimeMillis() - PublicStaticVariables.time) > (1000 * 60 * TIME_FOR_CLEAR_TASK)) {
                FilesManagement.deleteTempDecryptedMSG(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            if (getIntent().getParcelableExtra(Intent.EXTRA_STREAM) != null)
                intent.putExtra("attach", getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        Intent thisIntent = getIntent();
        if (thisIntent.getType() != null) {
            if (thisIntent.getAction().equals(Intent.ACTION_SEND)) {
                String s = thisIntent.getStringExtra(Intent.EXTRA_TEXT);
                if (s != null){
                    PublicStaticVariables.currentText = s;
                }
                go();
            } else if (thisIntent.getType().equals("application/octet-stream")
                    && thisIntent.getData() != null) {
                PublicContactCard qrp;
                Uri uri = getIntent().getData();
                if (uri != null) {
                    String data = null;
                    try {
                        ContentResolver cr = getBaseContext().getContentResolver();
                        InputStream is;
                        is = cr.openInputStream(uri);
                        StringBuilder buf = new StringBuilder();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is));
                        String str;
                        try {
                            while ((str = reader.readLine()) != null) {
                                buf.append(str).append("\n");
                            }

                            is.close();
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        buf.deleteCharAt(buf.length() - 1);
                        data = buf.toString();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (data != null) {
                        qrp = new PublicContactCard(this, data);
                        if (qrp.getPublicKey() != null) {
                            PublicStaticVariables.fileContactCard = qrp;
                        } else {
                            PublicStaticVariables.message = data;
                        }
                        go();
                    } else {
                        Toast.makeText(getBaseContext(), R.string.failed,
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(getBaseContext(), R.string.failed,
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            } else
                go();
        } else go();
    }
}
