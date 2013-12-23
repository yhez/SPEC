package specular.systems.activitys;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import specular.systems.ContactCard;
import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.StaticVariables;
import specular.systems.Visual;

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
        }
    });
    private Toast t = null;

    @Override
    public void onBackPressed() {
        //do not response to back pressed
    }

    void go() {
        boolean newUser = FilesManagement.isItNewUser(this);
        if (newUser) {
            setContentView(R.layout.splash);
            ((TextView) findViewById(R.id.company)).setTypeface(FilesManagement.getOs(this));
            findViewById(R.id.splash).animate().setDuration(TIME_FOR_SPLASH).alpha(1).start();
            waitForSplash.start();
        } else {
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

            Intent intent = new Intent(Splash.this, Main.class);
            if (StaticVariables.time == null || (System.currentTimeMillis() - StaticVariables.time) > (1000 * 60 * TIME_FOR_CLEAR_TASK)) {
                FilesManagement.deleteTempDecryptedMSG(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
            if (getIntent().getParcelableExtra(Intent.EXTRA_STREAM) != null && getIntent().getAction().equals(Intent.ACTION_SEND))
                intent.putExtra("specattach", getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
            startActivity(intent);
        }
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        t = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        Intent thisIntent = getIntent();
        if (thisIntent == null) {
            go();
        } else if (thisIntent.getAction() != null && thisIntent.getAction().equals(Intent.ACTION_SEND)) {
            String s = thisIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (s != null) {
                StaticVariables.currentText = s;
            }
            go();
        } else if (thisIntent.getData() == null) {
            go();
        } else {
            ContactCard qrp;
            //todo how is it possible? but some how it works for big google drive files
            Uri uri = getIntent().getData();
            if (uri == null) {
                uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            }
            if (uri == null) {
                t.setText(R.string.failed);
                t.show();
                finish();
                return;
            }
            String fileName = Visual.getFileName(this, uri);
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
            if (!ext.toLowerCase().equals("spec")) {
                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                String type = mtm.getMimeTypeFromExtension(ext);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (type != null)
                    intent.setType(type);
                else
                    intent.setType("*/*");
                startActivity(intent);
                finish();
                return;
            }
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
            if (data == null) {
                t.setText(R.string.failed);
                t.show();
                finish();
            } else {
                qrp = new ContactCard(this, data);
                if (qrp.getPublicKey() != null) {
                    StaticVariables.fileContactCard = qrp;
                } else {
                    StaticVariables.message = data;
                }
                go();
            }

        }
    }
}
