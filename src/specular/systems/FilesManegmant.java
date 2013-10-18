package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FilesManegmant {
    final static String FILE_NAME = "raw.SPEC";
    final static String QR_NAME = "QR.SPEC.png";
    private static Bitmap myQRPublicKey;
    private static boolean firstUse = true;
    private static Typeface tfos = null;
    private static Typeface tfold = null;
    Activity a;
    public static Bitmap getMyQRPublicKey(Activity a){
        return myQRPublicKey!=null?myQRPublicKey:
                BitmapFactory.decodeFile(a.getFilesDir() + "/" + QR_NAME);
    }
    Thread saveKeys = new Thread(new Runnable() {
        @Override
        public void run() {
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a.getApplicationContext());
            SharedPreferences.Editor edt = srp.edit();
            QRPublicKey qrpk = new QRPublicKey(a);
            FileOutputStream fos = null;
            try {
                fos = a.openFileOutput(FILE_NAME,
                        Context.MODE_WORLD_READABLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                try {
                    fos.write(qrpk.getQRToPublish().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int qrCodeDimention = 500;

            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

            try {
                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                FileOutputStream fos2 = null;
                try {
                    fos2 = a.openFileOutput(QR_NAME,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (fos2 != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos2);
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            edt.putString("public_key", qrpk.getPublicKey());
            edt.putString("email", qrpk.getEmail());
            edt.putString("name", qrpk.getName());
            edt.putString("private_key", CryptMethods.myPrivateKey!=null?CryptMethods.myPrivateKey:"the key is on nfc");
            edt.commit();
        }
    });

    public FilesManegmant(Activity a) {
        this.a = a;
    }

    public static Typeface getOs(Activity a) {
        if (tfos == null)
            tfos = Typeface.createFromAsset(a.getAssets(), "OpenSans-Light.ttf");
        return tfos;
    }

    public static Typeface getOld(Activity a) {
        if (tfold == null)
            tfold = Typeface.createFromAsset(a.getAssets(), "OpenSans-Light.ttf");
        return tfold;
    }

    public static boolean isItNewUser(Activity a) {
        if (!firstUse)
            return firstUse;
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        if(srp.getBoolean("firstUse", true)){
            srp.edit().putBoolean("firstUse",false).commit();
            return true;
        }
        return false;
    }

    public ArrayList<Uri> getFilesToShare() {
        try {
            File root = a.getFilesDir();
            ArrayList<Uri> uris = new ArrayList<Uri>(2);
            uris.add(Uri.parse("file://" + new File(root, QR_NAME)));
            uris.add(Uri.parse("file://" + new File(root, FILE_NAME)));
            return uris;
        } catch (Exception e) {
            return null;
        }
    }

    public void get() {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        CryptMethods.myPublicKey = srp.getString("public_key", null);
        CryptMethods.myPrivateKey = srp.getString("private_key", null);
        CryptMethods.myEmail = srp.getString("email", null);
        if(CryptMethods.myPrivateKey!=null)
            if(!CryptMethods.formatPrivate())
                CryptMethods.myPrivateKey=null;
    }

    public void save() {
        saveKeys.start();
    }
}
