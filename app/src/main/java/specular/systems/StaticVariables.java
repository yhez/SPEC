package specular.systems;

import android.text.method.KeyListener;

public class StaticVariables {
    //defines how long does the list have to be to show the last used contacts
    public static final int minContactSize = 10;
    public static boolean NFCMode = false;
    //from splash
    public static ContactCard fileContactCard;
    public static String message;
    public final static int MSG_LIMIT_FOR_QR = 141;
    public static KeyListener edit;
    public static String currentText = "";
    public static byte[] fileContent;
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
    //should be delete when goes to background
    public static String msg_content = null, file_name = null, session = null;
    public static long orig_msg_size, encrypted_msg_size;
    public static byte[] decryptedGroup;
    public static byte[] decryptedBackup;
    public static String encryptedMsgToSend;
}
