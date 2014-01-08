package specular.systems;

import android.text.method.KeyListener;

import java.io.File;
import java.util.List;

public class StaticVariables {
    //defines how long does the list have to be to show the last used contacts
    public static final int minContactSize = 10;
    public static boolean NFCMode = false;
    //from splash
    public static Long time;
    public static ContactCard fileContactCard;
    public static String message;
    public final static int MSG_LIMIT_FOR_QR = 141;
    public static KeyListener edit;
    public static String currentText = "";
    //the complete list
    public static List<Contact> fullList;
    public static byte[] fileContent;
    //from fragment
    public static FragmentManagement fragmentManagement;
    public static LastUsedContacts luc;
    public static boolean readyToSend = false;
    //from files management
    public final static int LIMIT_FILE_SIZE = 26214400;
    //restore decrypted msg after pause
    public static Boolean flag_hash;
    public static int flag_replay;
    public static int flag_session;
    public static String hash, timeStamp, friendsPublicKey, name, email;
    public static Boolean flag_msg;
    public static Boolean flag_light_msg;
    //should be delete when goes to background
    public static String msg_content = null, file_name = null, session = null;
    public static long orig_msg_size, encrypted_msg_size;
    public static String encryptedLight;
    public static File path;
}
