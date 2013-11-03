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
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

final class FilesManegmant {
    private final static String FILE_NAME = "PublicKey.SPEC";
    private final static String QR_NAME = "PublicKeyQR.SPEC.png";
    private final static String QR_NAME_T = "PublicKeyQRT.SPEC.png";
    private final static String FILE_NAME_SEND = "SecureMessage.SPEC";
    private final static String QR_NAME_SEND = "SecureQRMessage.SPEC.png";
    private static Bitmap myQRPublicKey;
    private static Typeface tfos = null;
    private static Typeface tfold = null;


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

    public static boolean createFileToOpen(Activity a){
        if(CryptMethods.decryptedMsg.getFileContent()==null)
            return false;
        try {
            FileOutputStream fos = a.openFileOutput("File", Context.MODE_WORLD_READABLE);
            fos.write(CryptMethods.decryptedMsg.getFileContent());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static Typeface getOld(Activity a) {
        if (tfold == null)
            tfold = Typeface.createFromAsset(a.getAssets(), "cour.ttf");
        return tfold;
    }

    public static boolean isItNewUser(Activity a) {
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
                CryptMethods.encryptedMsgToSend, BarcodeFormat.QR_CODE
                .toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            FileOutputStream fos2;
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

    public static void getKeysFromSdcard(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        CryptMethods.setPrivate(srp.getString("private_key", null));
        CryptMethods.setPublic(srp.getString("public_key", null));
        CryptMethods.setDetails(srp.getString("name", null),srp.getString("email", null));
    }

    public static void save(Activity a) {
        CryptMethods.moveKeysFromTmp();
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
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), BarcodeFormat.QR_CODE.toString(), 512);

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
        if(!CryptMethods.NFCMode)
            edt.putString("private_key", CryptMethods.getPrivateToSave());
        else
            edt.remove("private_key");
        edt.commit();
    }

    public static byte[] addFile(Activity a, Uri uri) {
        ContentResolver cr = a.getContentResolver();
        int size=0;
        try {
            InputStream ind = cr.openInputStream(uri);
            size=ind.available();
            ind.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] result = new byte[size];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(cr.openInputStream(uri));
                while(totalBytesRead < result.length){
                    int bytesRemaining = result.length - totalBytesRead;
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0){
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
            }
            finally {
                input.close();
            }
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        Log.d("size",result.length+"");
        return result;
    }
    private static Bitmap crop(Bitmap bitmap){
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