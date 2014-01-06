package specular.systems;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import static android.graphics.Typeface.createFromAsset;

public final class FilesManagement {
    public final static int RESULT_ADD_FILE_FAILED = 5, RESULT_ADD_FILE_TO_BIG = 10, RESULT_ADD_FILE_EMPTY = 20, RESULT_ADD_FILE_OK = 40;
    private final static int FRIEND_CONTACT_CARD = R.string.file_name_shared_contact_card;
    private final static int FRIENDS_SHARE_QR = R.string.file_name_friends_qr;
    private final static int FILE_NAME = R.string.file_name_my_public_key;
    private final static int QR_NAME = R.string.file_name_my_qr_key;
    private final static int FILE_NAME_SEND = R.string.file_name_secure_msg;
    private final static int QR_NAME_SEND = R.string.file_name_qr_msg;
    private final static String QR_NAME_T = "PublicKeyQRT.SPEC.png";
    private final static int FILE_NAME_BACKUP = R.string.file_name_Backup_msg;
    private final static String PUBLIC_KEY = "public_key", PRIVATE_KEY = "private_key", NAME = "name", EMAIL = "email";
    private static final String FILE = "file://";
    private static Bitmap myQRPublicKey;
    private static Typeface tfos = null;

    public static Bitmap getMyQRPublicKey(Activity a) {
        if (myQRPublicKey == null)
            myQRPublicKey = BitmapFactory.decodeFile(a.getFilesDir() + "/" + QR_NAME_T);
        return myQRPublicKey;
    }

    public static Typeface getOs(Activity a) {
        if (tfos == null)
            tfos = createFromAsset(a.getAssets(), "OpenSans-Light.ttf");
        return tfos;
    }

    public static boolean createFileToOpen(Activity a) {
        if (MessageFormat.decryptedMsg.getFileContent() == null)
            return false;
        try {
            File path = new File(Environment.getExternalStorageDirectory() + "/SPEC/attachments");
            if (!path.exists())
                path.mkdirs();
            File file = new File(path, MessageFormat.decryptedMsg.getFileName());
            OutputStream os = new FileOutputStream(file);
            os.write(MessageFormat.decryptedMsg.getFileContent());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Intent openFile(String fileName) {
        File path = new File(Environment.getExternalStorageDirectory() + "/SPEC/attachments");
        File f = new File(path, fileName);
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        MimeTypeMap mtm = MimeTypeMap.getSingleton();
        String type = mtm.getMimeTypeFromExtension(ext);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(f);
        intent.setDataAndType(uri, type);
        return intent;
    }

    public static void saveTempDecryptedMSG(Activity a) {
        if (MessageFormat.decryptedMsg == null)
            return;
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        SharedPreferences.Editor edt = srp.edit();
        edt.putString("msg", MessageFormat.decryptedMsg.getMsgContent());
        edt.putString("file_name", MessageFormat.decryptedMsg.getFileName());
        edt.putString("session", MessageFormat.decryptedMsg.getSession());
        StaticVariables.session = null;
        StaticVariables.file_name = null;
        StaticVariables.msg_content = null;
        edt.commit();
    }

    public static void deleteTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        SharedPreferences.Editor edt = srp.edit();
        edt.remove("msg");
        edt.remove("file_name");
        edt.remove("session");
        edt.commit();
        if (StaticVariables.file_name != null)
            try {
                new File(Environment.getExternalStorageDirectory(), StaticVariables.file_name).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        StaticVariables.flag_msg = false;
        StaticVariables.session = null;
        StaticVariables.file_name = null;
        StaticVariables.msg_content = null;
    }

    public static void getTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        StaticVariables.msg_content = srp.getString("msg", null);
        StaticVariables.session = srp.getString("session", null);
        StaticVariables.file_name = srp.getString("file_name", null);
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
                StaticVariables.encryptedLight, BarcodeFormat.QR_CODE
                .toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            try {
                FileOutputStream fos2;
                fos2 = a.openFileOutput(a.getString(QR_NAME_SEND),
                        Context.MODE_WORLD_READABLE);
                //todo add this lines to add the contact card to the qr png
                //we should check does facebook sends the original pictures? does whatsup? does gmail?
                //todo ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //todo bitmap.compress(Bitmap.CompressFormat.PNG, 90,stream);
                //todo stream.write(StaticVariables.encryptedMsgToSend.getBytes());
                //todo byte[] test = stream.toByteArray();
                //todo fos2.write(test);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos2);
                fos2.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } catch (WriterException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean saveFileToSend(Activity a,byte[] data) {
        try {
            FileOutputStream fos = a.openFileOutput(a.getString(
                    FILE_NAME_SEND), Context.MODE_WORLD_READABLE);
            fos.write(data);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean saveFileToSend(Activity a, String fileName, byte[] fileData) {
        try {
            FileOutputStream fos = a.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            fos.write(fileData);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ORI : added new function to reuse old code
    public static boolean createBackupFileToSend(Activity a, byte[] fileData) {
        return saveFileToSend(a, a.getString(FILE_NAME_BACKUP), fileData);
    }

    // ORI : took out the createfiletosend logic to savefiletosend function
    public static boolean createFilesToSend(Activity a, boolean qr,byte[] data) {
        boolean qrSuccess = true, fileSuccess;
        if (qr)
            qrSuccess = saveQRToSend(a);
        else
            new File(a.getFilesDir(), a.getString(QR_NAME_SEND)).delete();
        fileSuccess = saveFileToSend(a, a.getString(FILE_NAME_SEND), data);
        return qrSuccess || fileSuccess;
    }

    public static ArrayList<Uri> getFilesToSend(Activity a) {
        try {
            File root = a.getFilesDir();
            ArrayList<Uri> uris = new ArrayList<Uri>(2);
            uris.add(Uri.parse(FILE + new File(root, a.getString(FILE_NAME_SEND))));
            File f = new File(root, a.getString(QR_NAME_SEND));
            if (f.exists())
                uris.add(Uri.parse(FILE + f));
            else
                uris.add(null);
            return uris;
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri getFileToShare(Activity a) {
        try {
            return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(FILE_NAME)));
        } catch (Exception e) {
            return null;
        }
    }

    // TODO : ORI merge getFileToShare and getFileToShare
    public static Uri getBackupFileToShare(Activity a) {
        try {
            return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(FILE_NAME_BACKUP)));
        } catch (Exception e) {
            return null;
        }
    }

    public static Uri getContactCardToShare(Activity a) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        ContactCard pcc = new ContactCard(a, publicKey, email, name);
        FileOutputStream fos = null;
        try {
            fos = a.openFileOutput(a.getString(FRIEND_CONTACT_CARD),
                    Context.MODE_WORLD_READABLE);
            fos.write(pcc.getQRToPublish().getBytes("UTF-8"));
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(FRIEND_CONTACT_CARD)));
    }

    public static Uri getQRFriendToShare(Activity a) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        ContactCard pcc = new ContactCard(a, publicKey, email, name);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pcc.getQRToPublish(), BarcodeFormat.QR_CODE.toString(), 512);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            //Bitmap crop = crop(bitmap);
            FileOutputStream fos = null;
            try {
                fos = a.openFileOutput(a.getString(FRIENDS_SHARE_QR),
                        Context.MODE_WORLD_READABLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(FRIENDS_SHARE_QR)));
    }

    public static Uri getQRToShare(Activity a) {
        try {
            return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(QR_NAME)));
        } catch (Exception e) {
            return null;
        }
    }

    public static void getKeysFromSDCard(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removePrivate(Activity a) {
        new File(a.getFilesDir(), PRIVATE_KEY).delete();
    }

    public static void save(Activity a) {
        if (a != null) {
            myQRPublicKey = null;
            if (!StaticVariables.NFCMode)
                CryptMethods.moveKeysFromTmp();
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a.getApplicationContext());
            SharedPreferences.Editor edt = srp.edit();
            ContactCard qrpk = new ContactCard(a);
            FileOutputStream fos = null;
            try {
                fos = a.openFileOutput(a.getString(FILE_NAME),
                        Context.MODE_WORLD_READABLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (fos != null) {
                try {
                    fos.write(qrpk.getQRToPublish().getBytes("UTF-8"));
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
                    fos2 = a.openFileOutput(a.getString(QR_NAME),
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
            edt.putString(PUBLIC_KEY, qrpk.getPublicKey());
            edt.putString(EMAIL, qrpk.getEmail());
            edt.putString(NAME, qrpk.getName());
            if (!StaticVariables.NFCMode)
                savePrivate(a);
            else
                removePrivate(a);
            edt.commit();
        }
    }

    public static void edit(Activity a) {
        if (a != null) {
            myQRPublicKey = null;
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a.getApplicationContext());
            SharedPreferences.Editor edt = srp.edit();
            ContactCard qrpk = new ContactCard(a);
            FileOutputStream fos;
            try {
                fos = a.openFileOutput(a.getString(FILE_NAME),
                        Context.MODE_WORLD_READABLE);
                fos.write(qrpk.getQRToPublish().getBytes("UTF-8"));
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), BarcodeFormat.QR_CODE.toString(), 512);
            try {
                Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();

                Bitmap crop = crop(bitmap);
                FileOutputStream fos2;
                try {
                    fos2 = a.openFileOutput(a.getString(QR_NAME),
                            Context.MODE_WORLD_READABLE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos3;
                try {
                    fos3 = a.openFileOutput(QR_NAME_T,
                            Context.MODE_WORLD_READABLE);
                    crop.compress(Bitmap.CompressFormat.PNG, 90, fos3);
                } catch (FileNotFoundException e) {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return RESULT_ADD_FILE_FAILED;
        } catch (IOException e) {
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
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return RESULT_ADD_FILE_FAILED;
        } catch (IOException ex) {
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

    public static void deleteKeys(Activity a) {
        SharedPreferences srp = PreferenceManager
                .getDefaultSharedPreferences(a.getApplicationContext());
        SharedPreferences.Editor edt = srp.edit();
        edt.clear();
        edt.commit();
    }

    public static void deleteContacts(Activity a) {
        a.deleteDatabase(MySQLiteHelper.DATABASE_NAME);
    }

    public static String getlasts(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
        return srp.getString("lasts", null);
    }

    public static void updateLasts(Activity a, String s) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a.getApplicationContext());
        SharedPreferences.Editor edt = srp.edit();
        edt.putString("lasts", s);
        edt.commit();
    }

    public static void getMyDetails(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        CryptMethods.setDetails(srp.getString(NAME, null), srp.getString(EMAIL, null));
        CryptMethods.setPublic(srp.getString(PUBLIC_KEY, null));
    }
}