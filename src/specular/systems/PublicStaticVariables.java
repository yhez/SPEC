package specular.systems;

import android.text.method.KeyListener;

import java.util.List;

import specular.systems.activitys.Main;

public class PublicStaticVariables {
    //defines how long does the list have to be to show the last used contacts
    public static final int minContactSize = 10;
    //from CryptoMethods
    public static MessageFormat decryptedMsg = null;
    public static String encryptedMsgToSend = null;
    public static boolean NFCMode = false;
    //from splash
    public static Long time;
    public static ContactCard fileContactCard;
    public static String message;
    public final static int MSG_LIMIT_FOR_QR = 141;
    //form main
    public static Main main;
    public static ContactsDataSource contactsDataSource;
    public static MySimpleArrayAdapter adapter;
    public static KeyListener edit;
    public static String currentText = "";
    public static int currentLayout;
    //the list that the user see
    public static List<Contact> currentList;
    //the complete list
    public static List<Contact> fullList;
    public static byte[] fileContent;
    public static boolean handleByOnActivityResult = false;
    //from fragment
    public static FragmentManagement fragmentManagement;
    public static LastUsedContacts luc;
    public static boolean readyToSend = false;
    //from files management
    public final static int LIMIT_FILE_SIZE = 26214400;
    //restore decrypted msg after pause
    public static Boolean flag_hash, flag_replay;
    public static int flag_session;
    public static String hash, timeStamp, friendsPublicKey, name, email;
    public static Boolean flag_msg;
    //should be delete when goes to background
    public static String msg_content = null, file_name = null, session = null;
}
