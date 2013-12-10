package specular.systems;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
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
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import specular.systems.widget.WidgetContact;

import static android.graphics.Typeface.createFromAsset;

public final class FilesManagement {
    private final static int FRIEND_CONTACT_CARD = R.string.file_name_shared_contact_card;
    private final static int FRIENDS_SHARE_QR = R.string.file_name_friends_qr;
    private final static int FILE_NAME = R.string.file_name_my_public_key;
    private final static int QR_NAME = R.string.file_name_my_qr_key;
    private final static String QR_NAME_T = "PublicKeyQRT.SPEC.png";
    private final static int FILE_NAME_SEND = R.string.file_name_secure_msg;
    private final static int QR_NAME_SEND = R.string.file_name_qr_msg;
    private final static String PUBLIC_KEY = "public_key", PRIVATE_KEY = "private_key", NAME = "name", EMAIL = "email";
    private static final String FILE = "file://";
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
            tfos = createFromAsset(a.getAssets(), "OpenSans-Light.ttf");
        return tfos;
    }

    public static boolean createFileToOpen(Activity a) {
        if (PublicStaticVariables.decryptedMsg.getFileContent() == null)
            return false;
        try {
            File path = Environment.getExternalStorageDirectory();
            File file = new File(path, PublicStaticVariables.decryptedMsg.getFileName());
            OutputStream os = new FileOutputStream(file);
            os.write(PublicStaticVariables.decryptedMsg.getFileContent());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void saveTempDecryptedMSG(Activity a) {
        if (PublicStaticVariables.decryptedMsg == null)
            return;
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        SharedPreferences.Editor edt = srp.edit();
        edt.putString("msg", PublicStaticVariables.decryptedMsg.getMsgContent());
        edt.putString("file_name", PublicStaticVariables.decryptedMsg.getFileName());
        edt.putString("session", PublicStaticVariables.decryptedMsg.getSession());
        PublicStaticVariables.session = null;
        PublicStaticVariables.file_name = null;
        PublicStaticVariables.msg_content = null;
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
        if (PublicStaticVariables.file_name != null)
            try {
                new File(Environment.getExternalStorageDirectory(), PublicStaticVariables.file_name).delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        PublicStaticVariables.flag_msg = false;
        PublicStaticVariables.session = null;
        PublicStaticVariables.file_name = null;
        PublicStaticVariables.msg_content = null;
    }

    public static void getTempDecryptedMSG(Activity a) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a
                .getApplicationContext());
        PublicStaticVariables.msg_content = srp.getString("msg", null);
        PublicStaticVariables.session = srp.getString("session", null);
        PublicStaticVariables.file_name = srp.getString("file_name", null);
    }

    public static Typeface getOld(Activity a) {
        if (tfold == null)
            tfold = createFromAsset(a.getAssets(), "cour.ttf");
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
                PublicStaticVariables.encryptedMsgToSend, BarcodeFormat.QR_CODE
                .toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            FileOutputStream fos2;
            try {
                fos2 = a.openFileOutput(a.getString(QR_NAME_SEND),
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
            FileOutputStream fos = a.openFileOutput(a.getString(
                    FILE_NAME_SEND), Context.MODE_WORLD_READABLE);
            fos.write(PublicStaticVariables.encryptedMsgToSend.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean createFilesToSend(Activity a, boolean qr) {
        boolean qrSuccess = true, fileSuccess;
        if (qr)
            qrSuccess = saveQRToSend(a);
        else
            new File(a.getFilesDir(), a.getString(QR_NAME_SEND)).delete();
        fileSuccess = saveFileToSend(a);
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

    public static Uri getContactCardToShare(Activity a) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        PublicContactCard pcc = new PublicContactCard(a, publicKey, email, name);
        FileOutputStream fos = null;
        try {
            fos = a.openFileOutput(a.getString(FRIEND_CONTACT_CARD),
                    Context.MODE_WORLD_READABLE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (fos != null) {
            try {
                fos.write(pcc.getQRToPublish().getBytes());
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Uri.parse(FILE + new File(a.getFilesDir(), a.getString(FRIEND_CONTACT_CARD)));
    }

    public static Uri getQRFriendToShare(Activity a) {
        String name = ((EditText) a.findViewById(R.id.contact_name).findViewById(R.id.edit_text)).getText().toString();
        String email = ((EditText) a.findViewById(R.id.contact_email).findViewById(R.id.edit_text)).getText().toString();
        String publicKey = ((TextView) a.findViewById(R.id.contact_pb)).getText().toString();
        PublicContactCard pcc = new PublicContactCard(a, publicKey, email, name);
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
        CryptMethods.setPrivate(srp.getString(PRIVATE_KEY, null));
        CryptMethods.setPublic(srp.getString(PUBLIC_KEY, null));
        CryptMethods.setDetails(srp.getString(NAME, null), srp.getString(EMAIL, null));
    }

    public static void save(Activity a) {
        if (a != null) {
            myQRPublicKey = null;
            CryptMethods.moveKeysFromTmp();
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a.getApplicationContext());
            SharedPreferences.Editor edt = srp.edit();
            PublicContactCard qrpk = new PublicContactCard(a);
            FileOutputStream fos = null;
            try {
                fos = a.openFileOutput(a.getString(FILE_NAME),
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
            if (!PublicStaticVariables.NFCMode)
                edt.putString(PRIVATE_KEY, CryptMethods.getPrivateToSave());
            else
                edt.remove(PRIVATE_KEY);
            edt.commit();
        }
    }

    public static void edit(Activity a) {
        if (a != null) {
            myQRPublicKey = null;
            SharedPreferences srp = PreferenceManager
                    .getDefaultSharedPreferences(a.getApplicationContext());
            SharedPreferences.Editor edt = srp.edit();
            PublicContactCard qrpk = new PublicContactCard(a);
            FileOutputStream fos;
            try {
                fos = a.openFileOutput(a.getString(FILE_NAME),
                        Context.MODE_WORLD_READABLE);
                fos.write(qrpk.getQRToPublish().getBytes());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrpk.getQRToPublish(), BarcodeFormat.QR_CODE.toString(), 512);

            Bitmap bitmap = null;
            try {
                bitmap = qrCodeEncoder.encodeAsBitmap();

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
            return PublicStaticVariables.RESULT_ADD_FILE_FAILED;
        } catch (IOException e) {
            e.printStackTrace();
            return PublicStaticVariables.RESULT_ADD_FILE_FAILED;
        }
        if (size > PublicStaticVariables.LIMIT_FILE_SIZE)
            return PublicStaticVariables.RESULT_ADD_FILE_TO_BIG;
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
            return PublicStaticVariables.RESULT_ADD_FILE_FAILED;
        } catch (IOException ex) {
            ex.printStackTrace();
            return PublicStaticVariables.RESULT_ADD_FILE_FAILED;
        }
        if (result.length > 0) {
            PublicStaticVariables.fileContent = result;
            return PublicStaticVariables.RESULT_ADD_FILE_OK;
        }
        return PublicStaticVariables.RESULT_ADD_FILE_EMPTY;
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

    public static void deleteTmp(Activity a) {
        a.deleteFile(a.getString(QR_NAME));
        a.deleteFile(a.getString(QR_NAME_SEND));
        a.deleteFile(QR_NAME_T);
        a.deleteFile(a.getString(FILE_NAME));
        a.deleteFile(a.getString(FILE_NAME_SEND));
        a.deleteFile(a.getString(FRIENDS_SHARE_QR));
        a.deleteFile(a.getString(FRIEND_CONTACT_CARD));
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

    public static void removeWidget(Activity a,long id) {
        SharedPreferences srp = PreferenceManager.getDefaultSharedPreferences(a);
        SharedPreferences.Editor edt = srp.edit();
        //todo the id is not the contacts it's the widget
        String widget = "widget-id-"+id;
        Log.d("remove widget", widget);
        edt.remove(widget);
        edt.commit();
        new File(a.getFilesDir()+"/"+widget).delete();
        Intent i = new Intent(a, WidgetContact.class);
        i.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        AppWidgetManager apa = AppWidgetManager.getInstance(a);
        int[] ids = apa.getAppWidgetIds(a.getComponentName());
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        a.sendBroadcast(i);
    }
}