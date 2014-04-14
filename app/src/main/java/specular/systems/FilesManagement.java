package specular.systems;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import specular.systems.activities.FilesOpener;
import zxing.QRCodeEncoder;
import zxing.WriterException;

import static android.graphics.Typeface.createFromAsset;
import static android.support.v4.content.FileProvider.getUriForFile;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class FilesManagement {
    public final static int RESULT_ADD_FILE_FAILED = 5, RESULT_ADD_FILE_TO_BIG = 10, RESULT_ADD_FILE_EMPTY = 20, RESULT_ADD_FILE_OK = 40;
    private final static int FRIEND_CONTACT_CARD = R.string.file_name_shared_contact_card;
    private final static int FRIENDS_SHARE_QR = R.string.file_name_friends_qr;
    private final static int FILE_NAME = R.string.file_name_my_public_key;
    private final static int QR_NAME = R.string.file_name_my_qr_key;
    private final static int FILE_NAME_SEND = R.string.file_name_secure_msg;
    private final static String QR_NAME_T = "PublicKeyQRT.SPEC.png";
    private final static int FILE_NAME_BACKUP = R.string.file_name_Backup_msg;
    private final static int FILE_NAME_GROUP = R.string.file_name_group;
    public final static String ATTACHMENTS = "/attachments", MESSAGES = "/messages", TEMP = "/temp", SAFE = "/safe", NOTES = "/notes", REPORTS = "/reports";
    private final static String PUBLIC_KEY = "public_key", PRIVATE_KEY = "private_key", NAME = "name", EMAIL = "email";
    private static Bitmap myQRPublicKey;
    private static Typeface tfos = null;

    public static class id_picture {

        private final static String SECRET_PICTURE = "obscure";

        public static boolean pictureExist(Activity a) {
            return new File(a.getFilesDir(), SECRET_PICTURE).exists();
        }

        public static Drawable getPicture(Activity a) {
            Drawable d = Drawable.createFromPath(a.getFilesDir() + "/" + SECRET_PICTURE);
            d.setAlpha(128);
            return d;
        }

        public static Intent createIntent(Activity a) {
            File path = new File(a.getFilesDir()+TEMP);
            if(!path.exists())
                path.mkdir();
            Uri uri = getUriForFile(a, a.getPackageName(), new File(path, "t"));
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
            ResolveInfo lk = a.getPackageManager().resolveActivity(i, 0);
            a.grantUriPermission(lk.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            return i;
        }
        public static void save(Activity a){
            File t = new File(a.getFilesDir()+TEMP,"t");
            File d = new File(a.getFilesDir(),SECRET_PICTURE);
            t.renameTo(d);
            try {
                Bitmap b = BitmapFactory.decodeFile(d.getPath());
                Point p = new Point();
                a.getWindow().getWindowManager().getDefaultDisplay().getSize(p);
                Bitmap bb = Bitmap.createScaledBitmap(b, p.x, p.y, false);
                FileOutputStream fos;
                fos = new FileOutputStream(d);
                bb.compress(Bitmap.CompressFormat.PNG,25,fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Bitmap getMyQRPublicKey(Activity a) {
        if (myQRPublicKey == null)
            myQRPublicKey = BitmapFactory.decodeFile(a.getFilesDir() + "/" + QR_NAME_T);
        return myQRPublicKey;
    }

    public static Typeface getOs(Context a) {
        if (tfos == null)
            tfos = createFromAsset(a.getAssets(), "OpenSans-Light.ttf");
        return tfos;
    }

    public static void createFileToOpen(Activity a, byte[] file, String name) {
        FilesOpener.saveFileToOpen(a, file, name);
    }

    public static void saveTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor edt = srp.edit();
        edt.putString("msg", StaticVariables.msg_content);
        edt.putString("file_name", StaticVariables.file_name);
        edt.putString("session", StaticVariables.session);
        StaticVariables.session = null;
        StaticVariables.file_name = null;
        StaticVariables.msg_content = null;
        edt.commit();
    }

    public static void deleteTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor edt = srp.edit();
        edt.remove("msg");
        edt.remove("file_name");
        edt.remove("session");
        edt.commit();
        try {
            File[] files = new File(a.getFilesDir() + ATTACHMENTS).listFiles();
            if (files != null)
                for (File f : files)
                    f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StaticVariables.flag_msg = false;
        StaticVariables.session = null;
        if (StaticVariables.file_name != null) {
            a.deleteFile(StaticVariables.file_name);
            StaticVariables.file_name = null;
        }
        StaticVariables.msg_content = null;
    }

    public static void getTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        StaticVariables.msg_content = srp.getString("msg", null);
        StaticVariables.session = srp.getString("session", null);
        StaticVariables.file_name = srp.getString("file_name", null);
    }

    public static boolean isItNewUser(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        if (srp.getBoolean("firstUse", true)) {
            srp.edit().putBoolean("firstUse", false).commit();
            return true;
        }
        return false;
    }

    public static boolean isItOnPauseForALongTime(Activity a) {
        long limit = 900000;//15 minutes
        long current = System.currentTimeMillis();
        long oldTime = PreferenceManager.getDefaultSharedPreferences(a).getLong("onPause", current);
        return current - limit > oldTime;
    }

    private static boolean saveFileToSend(Activity a, String fileName, byte[] fileData) {
        try {
            File path = new File(a.getFilesDir() + MESSAGES);
            if (!path.exists())
                path.mkdirs();
            File file = new File(path, fileName);
            OutputStream os = new FileOutputStream(file);
            os.write(fileData);
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void cleanUp(Activity a) {
        File root = new File(a.getFilesDir() + MESSAGES);
        if (root.exists()) {
            File[] fl = root.listFiles();
            if(fl!=null)
                for (File f : fl)
                    f.delete();
        }
    }

    public static boolean createBackupFileToSend(Activity a, byte[] fileData) {
        cleanUp(a);
        return saveFileToSend(a, a.getString(FILE_NAME_BACKUP), fileData);
    }

    public static void createGroupFileToSend(Activity a, byte[] fileData) {
        cleanUp(a);
        saveFileToSend(a, a.getString(FILE_NAME_GROUP), fileData);
    }

    public static boolean createFilesToSend(Activity a, byte[] data) {
        cleanUp(a);
        boolean fileSuccess = saveFileToSend(a, a.getString(FILE_NAME_SEND), data);
        //todo delete it
        StaticVariables.dataRaw = data;
        return fileSuccess;
    }

    public static Uri getFilesToSend(Activity a) {
        try {
            File root = new File(a.getFilesDir() + MESSAGES);
            if (new File(root, a.getString(FILE_NAME_SEND)).exists()) {
                return getUriForFile(a, a.getPackageName(), new File(root, a.getString(FILE_NAME_SEND)));
            } else if (new File(root, a.getString(FILE_NAME_BACKUP)).exists()) {
                return getUriForFile(a, a.getPackageName(), new File(root, a.getString(FILE_NAME_BACKUP)));
            } else {
                return getUriForFile(a, a.getPackageName(), new File(root, a.getString(FILE_NAME_GROUP)));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static File getFileToShare(Activity a) {
        try {
            File f = new File(a.getFilesDir() + TEMP, a.getString(FILE_NAME));
            if (!f.exists()) {
                FileInputStream fis = a.openFileInput(a.getString(FILE_NAME));
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                File path = new File(a.getFilesDir() + TEMP);
                if (!path.exists())
                    path.mkdir();
                OutputStream os = new FileOutputStream(f);
                os.write(b);
                os.close();
            }
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    public static File getContactCardToShare(Activity a) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        ContactCard pcc = new ContactCard(a, publicKey, email, name);
        try {
            File path = new File(a.getFilesDir() + MESSAGES);
            if (!path.exists())
                path.mkdirs();
            File file = new File(path, a.getString(FRIEND_CONTACT_CARD));
            OutputStream os = new FileOutputStream(file);
            os.write(pcc.getQRToPublish().getBytes(Visual.strings.UTF));
            os.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Uri getQRFriendToShare(Activity a, ComponentName cn) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        ContactCard pcc = new ContactCard(a, publicKey, email, name);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pcc.getQRToPublish(), 512);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            //Bitmap crop = crop(bitmap);
            File path = new File(a.getFilesDir() + MESSAGES);
            if (!path.exists())
                path.mkdirs();
            File file = new File(path, a.getString(FRIENDS_SHARE_QR));
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
            os.close();
            Uri uri  = getUriForFile(a, a.getPackageName(), file);
            a.grantUriPermission(cn.getPackageName(),uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getQRToShare(Activity a) {
        try {
            File f = new File(a.getFilesDir() + TEMP, a.getString(QR_NAME));
            if (!f.exists()) {
                FileInputStream fis = a.openFileInput(a.getString(QR_NAME));
                byte[] b = new byte[fis.available()];
                fis.read(b);
                fis.close();
                File path = new File(a.getFilesDir() + TEMP);
                if (!path.exists())
                    path.mkdir();
                OutputStream os = new FileOutputStream(f);
                os.write(b);
                os.close();
            }
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    public static void getKeysFromSDCard(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        CryptMethods.setPrivate(getPrivate(a));
        CryptMethods.setPublic(srp.getString(PUBLIC_KEY, null));
        CryptMethods.setDetails(srp.getString(NAME, null), srp.getString(EMAIL, null));
    }

    private static byte[] getPrivate(Activity a) {

        File f = new File(a.getFilesDir(), PRIVATE_KEY);
        if (f.exists()) {
            try {
                FileInputStream is = a.openFileInput(PRIVATE_KEY);
                byte[] b = new byte[(int) new File(a.getFilesDir(), PRIVATE_KEY).length()];
                is.read(b);
                return b;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else return null;
    }

    public static void savePrivate(Activity a) {
        try {
            FileOutputStream fos = a.openFileOutput(PRIVATE_KEY, Context.MODE_PRIVATE);
            fos.write(CryptMethods.getPrivateToSave());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removePrivate(Activity a) {
        new File(a.getFilesDir(), PRIVATE_KEY).delete();
    }

    public static void save(Activity a,boolean nfcMode) {
        if (a != null) {
            myQRPublicKey = null;
            if (!nfcMode)
                CryptMethods.moveKeysFromTmp();
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a);
            SharedPreferences.Editor edt = srp.edit();
            ContactCard qrpk = new ContactCard(a);
            File path = new File(a.getFilesDir() + TEMP);
            if (path.exists())
                for (String name : path.list())
                    new File(path, name).delete();
            try {
                FileOutputStream fos = a.openFileOutput(a.getString(FILE_NAME),
                        Context.MODE_PRIVATE);
                fos.write(qrpk.getQRToPublish().getBytes(Visual.strings.UTF));
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), 512);
                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
                Bitmap crop = crop(bitmap);
                try {
                    FileOutputStream fos = a.openFileOutput(a.getString(QR_NAME),
                            Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fos = a.openFileOutput(QR_NAME_T,
                            Context.MODE_PRIVATE);
                    crop.compress(Bitmap.CompressFormat.PNG, 90, fos);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            edt.putString(PUBLIC_KEY, qrpk.getPublicKey());
            edt.putString(EMAIL, qrpk.getEmail());
            edt.putString(NAME, qrpk.getName());
            if (!nfcMode)
                savePrivate(a);
            else
                removePrivate(a);
            edt.commit();
        }
    }

    public static void saveToSafe(Activity a, String name) {
        try {
            File path = new File(a.getFilesDir() + SAFE);
            if (!path.exists())
                path.mkdir();
            File file = new File(path, name);
            OutputStream os = new FileOutputStream(file);
            os.write(CryptMethods.encrypt(MessageFormat.decryptedMsg.getFileContent(), CryptMethods.getPublic()));
            os.close();
            a
                    .getSharedPreferences("saved_files", Context.MODE_PRIVATE)
                    .edit()
                    .putString(System.currentTimeMillis() + "", name)
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveNoteToSafe(Activity a, String data) {
        try {
            String fileName = System.currentTimeMillis() + "";
            File path = new File(a.getFilesDir() + NOTES);
            if (!path.exists())
                path.mkdir();
            File file = new File(path, fileName);
            OutputStream os = new FileOutputStream(file);
            byte[] dat = data.getBytes(Visual.strings.UTF);
            os.write(CryptMethods.encrypt(dat, CryptMethods.getPublic()));
            os.close();
            a.getSharedPreferences("notes", Context.MODE_PRIVATE)
                    .edit().putString(fileName, fileName).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNoteFromSafe(Activity a, String name) throws Exception {
        InputStream is = new FileInputStream(new File(a.getFilesDir() + NOTES, name));
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        byte[] b = CryptMethods.decrypt(data);
        return new String(b, Visual.strings.UTF);
    }

    public static void getFromSafe(Activity a, String name) throws Exception {
        InputStream is = new FileInputStream(new File(a.getFilesDir() + SAFE, name));
        byte[] data = new byte[is.available()];
        is.read(data);
        is.close();
        byte[] b = CryptMethods.decrypt(data);
        FilesManagement.createFileToOpen(a, b, name);
    }

    public static void edit(Activity a) {
        if (a != null) {
            myQRPublicKey = null;
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a);
            SharedPreferences.Editor edt = srp.edit();
            ContactCard qrpk = new ContactCard(a);
            FileOutputStream fos;
            try {
                fos = a.openFileOutput(a.getString(FILE_NAME),
                        Context.MODE_PRIVATE);
                fos.write(qrpk.getQRToPublish().getBytes(Visual.strings.UTF));
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), 512);
            try {
                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();

                Bitmap crop = crop(bitmap);
                FileOutputStream fos2;
                try {
                    fos2 = a.openFileOutput(a.getString(QR_NAME),
                            Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileOutputStream fos3;
                try {
                    fos3 = a.openFileOutput(QR_NAME_T,
                            Context.MODE_PRIVATE);
                    crop.compress(Bitmap.CompressFormat.PNG, 90, fos3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
            edt.putString(EMAIL, qrpk.getEmail());
            edt.putString(NAME, qrpk.getName());
            edt.commit();
        }
    }

    public static int addFile(Activity a, Uri uri) {
        ContentResolver cr = a.getContentResolver();
        int size = 0;
        try {
            InputStream ind = cr.openInputStream(uri);
            if (ind != null) {
                size = ind.available();
                ind.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RESULT_ADD_FILE_FAILED;
        }
        if (size > StaticVariables.LIMIT_FILE_SIZE) {
            return RESULT_ADD_FILE_TO_BIG;
        }
        byte[] result = new byte[size];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(cr.openInputStream(uri));
                while (totalBytesRead < result.length) {
                    int bytesRemaining = result.length - totalBytesRead;
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0) {
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return RESULT_ADD_FILE_FAILED;
        }
        if (result.length > 0) {
            StaticVariables.fileContent = result;
            return RESULT_ADD_FILE_OK;
        }
        return RESULT_ADD_FILE_EMPTY;
    }

    private static Bitmap crop(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int border;
        for (int x = 0; ; x++)
            if (bitmap.getPixel(x, x) != Color.WHITE) {
                border = x;
                break;
            }
        return Bitmap.createBitmap(bitmap, border, border, width - (border * 2), width - (border * 2));
    }
/*
    public static void deleteKeys(Activity a) {
        SharedPreferences srp = PreferenceManager
                .getDefaultSharedPreferences(a);
        SharedPreferences.Editor edt = srp.edit();
        edt.clear();
        edt.commit();
    }

    public static void deleteContacts(Activity a) {
        a.deleteDatabase(MySQLiteHelper.DATABASE_NAME);
    }*/

    public static String getlasts(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        return srp.getString("lasts", null);
    }

    public static void updateLasts(Activity a, String s) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor edt = srp.edit();
        edt.putString("lasts", s);
        edt.commit();
    }

    public static void getMyDetails(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        CryptMethods.setDetails(srp.getString(NAME, null), srp.getString(EMAIL, null));
        CryptMethods.setPublic(srp.getString(PUBLIC_KEY, null));
    }

    public static boolean motNFCMod(Activity a) {
        byte[] p = getPrivate(a);
        if (p == null)
            return true;
        byte[] cp = CryptMethods.getPrivateToSave();
        for (int aa = 0; aa < p.length; aa++)
            if (p[aa] != cp[aa])
                return false;
        return true;
    }

    public static boolean saveFileForOpen(Activity a, byte[] file, String name) {
        File path = new File(a.getFilesDir() + ATTACHMENTS);
        if (!path.exists())
            path.mkdir();
        for (String f : path.list()) {
            new File(path, f).delete();
        }
        File f = new File(path, name);
        try {
            OutputStream os = new FileOutputStream(f);
            os.write(file);
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }


    }
}