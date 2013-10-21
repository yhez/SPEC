package specular.systems;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FilesManegmant {
    final static String FILE_NAME = "PublicKey.SPEC";
    final static String QR_NAME = "PublicKeyQR.SPEC.png";
    final static String QR_NAME_T = "PublicKeyQRT.SPEC.png";
    final static String FILE_NAME_SEND = "SecureMessage.SPEC";
    final static String QR_NAME_SEND = "SecureQRMessage.SPEC.png";
    private static Bitmap myQRPublicKey;
    private static boolean firstUse = true;
    private static Typeface tfos = null;
    private static Typeface tfold = null;
    Activity a;
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
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 512);

            try {
                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                Bitmap crop = crop(bitmap);
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
                FileOutputStream fos3 = null;
                try {
                    fos3 = a.openFileOutput(QR_NAME_T,
                            Context.MODE_WORLD_READABLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (fos3 != null) {
                    crop.compress(Bitmap.CompressFormat.PNG, 90, fos3);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            edt.putString("public_key", qrpk.getPublicKey());
            edt.putString("email", qrpk.getEmail());
            edt.putString("name", qrpk.getName());
            edt.putString("private_key", CryptMethods.myPrivateKey != null ? CryptMethods.myPrivateKey : "the key is on nfc");
            edt.commit();
        }
    });

    public FilesManegmant(Activity a) {
        this.a = a;
    }

    public static Bitmap getMyQRPublicKey(Activity a) {
        if (myQRPublicKey == null)
            myQRPublicKey = BitmapFactory.decodeFile(a.getFilesDir() + "/" + QR_NAME_T);
        return myQRPublicKey;
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
        if (srp.getBoolean("firstUse", true)) {
            srp.edit().putBoolean("firstUse", false).commit();
            return true;
        }
        return false;
    }

    private static boolean saveQRToSend(Activity a) {
        int qrCodeDimention = 500;
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(
                CryptMethods.encryptedMsgToSend, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE
                .toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            FileOutputStream fos2 = null;
            try {
                fos2 = a.openFileOutput(QR_NAME_SEND,
                        Context.MODE_WORLD_READABLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            if (fos2 != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90,
                        fos2);
                try {
                    fos2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (WriterException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean saveFileToSend(Activity a) {
        try {
            FileOutputStream fos = a.openFileOutput(
                    FILE_NAME_SEND, Context.MODE_WORLD_READABLE);
            fos.write(CryptMethods.encryptedMsgToSend.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean createFilesToSend(Activity a, boolean qr) {
        boolean qrSucces = true, fileSucces;
        if (qr)
            qrSucces = saveQRToSend(a);
        else
            new File(a.getFilesDir(), QR_NAME_SEND).delete();
        fileSucces = saveFileToSend(a);
        return qrSucces || fileSucces;
    }

    public static ArrayList<Uri> getFilesToSend(Activity a) {
        try {
            File root = a.getFilesDir();
            ArrayList<Uri> uris = new ArrayList<Uri>(2);
            uris.add(Uri.parse("file://" + new File(root, FILE_NAME_SEND)));
            File f = new File(root, QR_NAME_SEND);
            if (f.exists())
                uris.add(Uri.parse("file://" + f));
            return uris;
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri getFileToShare(Activity a) {
        try {
            return Uri.parse("file://" + new File(a.getFilesDir(), FILE_NAME));
        } catch (Exception e) {
            return null;
        }
    }
    public static Uri getQRToShare(Activity a) {
        try {
            return Uri.parse("file://" + new File(a.getFilesDir(), QR_NAME));
        } catch (Exception e) {
            return null;
        }
    }

    public void getKeysFromSdcard() {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        CryptMethods.myPublicKey = srp.getString("public_key", null);
        CryptMethods.myPrivateKey = srp.getString("private_key", null);
        CryptMethods.myEmail = srp.getString("email", null);
        if (CryptMethods.myPrivateKey != null)
            if (!CryptMethods.formatPrivate())
                CryptMethods.myPrivateKey = null;
    }

    public void save() {
        saveKeys.start();
    }

    public static String addFile(Activity a, Uri uri) {
        ContentResolver cr = a.getContentResolver();
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
        return buf.toString();
    }
    private Bitmap crop(Bitmap bitmap){
        int width = bitmap.getWidth();
        int border;
        for (int x = 0; ; x++)
            if (bitmap.getPixel(x, x) != Color.WHITE) {
                border = x;
                break;
            }
        return Bitmap.createBitmap(bitmap, border, border, width - (border * 2), width - (border * 2));
    }
}