package specular.systems.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import specular.systems.ContactCard;
import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.FileParser;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
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
                    ((Object) this).wait(TIME_FOR_SPLASH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent(Splash.this, Main.class);
            startActivity(intent);
        }
    });
    private final Handler hndl = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 500) {
                setContentView(R.layout.splash);
                ((TextView) findViewById(R.id.company)).setTypeface(FilesManagement.getOs(Splash.this));
                findViewById(R.id.splash).animate().setDuration(TIME_FOR_SPLASH).alpha(1).start();
                waitForSplash.start();
            } else {
                Toast t = Toast.makeText(Splash.this, R.string.failed, Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            }
        }
    };

    @Override
    public void onBackPressed() {
        //do not response to back pressed
    }

    void go() {
        boolean newUser = FilesManagement.isItNewUser(this);
        if (newUser) {
            hndl.sendEmptyMessage(500);
            return;
        }
        FilesManagement.getKeysFromSDCard(this);
        if (!CryptMethods.privateExist() && getIntent().getType() != null) {
            Parcelable raw[] = getIntent().getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (raw != null) {
                NdefMessage msg = (NdefMessage) raw[0];
                NdefRecord pvk = msg.getRecords()[0];
                CryptMethods.setPrivate(pvk
                        .getPayload());
            }
        }

        Intent intent = new Intent(Splash.this, Main.class);
        if (getIntent().getParcelableExtra(Intent.EXTRA_STREAM) != null && getIntent().getAction().equals(Intent.ACTION_SEND))
            intent.putExtra("specattach", getIntent().getParcelableExtra(Intent.EXTRA_STREAM));
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        if (getIntent() == null) {
            go();
        } else if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_SEND)) {
            String s = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (s != null) {
                StaticVariables.currentText = s;
            }
            go();
        } else if (getIntent().getData() == null) {
            go();
        } else {
            //todo how is it possible? but some how it works for big google drive files
            Uri uri = getIntent().getData();
            if (uri == null) {
                uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            }
            if (uri == null) {
                hndl.sendEmptyMessage(0);
                finish();
                return;
            }
            if (uri.getScheme() == null || !uri.getScheme().equals("specular.systems")) {
                String fileName = Visual.getFileName(this, uri);
                if(fileName==null){
                    Toast t  = Toast.makeText(this,R.string.failed_loading,Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.CENTER,0,0);
                    t.show();
                    finish();
                    return;
                }
                String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
                if (!ext.toLowerCase().equals("spec")) {
                    File f = new File(uri.getPath(), fileName);
                    MimeTypeMap mtm = MimeTypeMap.getSingleton();
                    String type = mtm.getMimeTypeFromExtension(ext);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    uri = Uri.fromFile(f);
                    intent.setData(uri);
                    if (type != null)
                        intent.setType(type);
                    else
                        intent.setType("*/*");
                    finish();
                    startActivity(intent);
                    return;
                }
            }
            final Uri ur = uri;
            final ProgressDialog pd = new ProgressDialog(this, R.style.dialogTransparent);
            pd.setTitle(R.string.loading_title);
            pd.setMessage(getString(R.string.loading_msg));
            pd.setCancelable(false);
            pd.show();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String data = null;
                    if (ur.getScheme() != null && ur.getScheme().equals("specular.systems")){
                        data = ur.getQueryParameter("message");
                        if(data==null){
                            data = "\n"+ur.getQueryParameter("name")+"\n"+ur.getQueryParameter("email")+"\n"+ur.getQueryParameter("key");
                        }
                    }
                    else
                        try {
                            ContentResolver cr = getBaseContext().getContentResolver();
                            InputStream is = cr.openInputStream(ur);
                            try {
                                StringBuilder buf = new StringBuilder();
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(is));
                                String str;
                                while ((str = reader.readLine()) != null) {
                                    buf.append(str).append("\n");
                                }
                                buf.deleteCharAt(buf.length() - 1);
                                data = buf.toString();
                                pd.cancel();
                                reader.close();
                                is.close();
                            } catch (IOException ignored) {
                            }
                        } catch (FileNotFoundException ignored) {
                        }
                    if (data == null) {
                        hndl.sendEmptyMessage(0);
                        finish();
                    } else {
                        int typeFile = FileParser.getType(data);
                        if (typeFile == FileParser.CONTACT_CARD)
                            StaticVariables.fileContactCard = new ContactCard(Splash.this, data);
                        else if (typeFile == -1) {
                            StaticVariables.message = data;
                        }
                        go();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new KeysDeleter();
    }

    @Override
    public void onResume() {
        super.onResume();
        KeysDeleter.stop();
    }
}
