package specular.systems;

import java.util.List;

public class PublicStaticVariables {
    //defines how long does the list have to be to show the last used contacts
    public static final int minContactSize = 10;
    //from CryptoMethods
    public static MessageFormat decryptedMsg = null;
    public static String encryptedMsgToSend = null;
    public static boolean NFCMode = false;
    //from contact
    public final static int VERIFIED = 0, FAILED = 1, DONT_TRUST = 2;// ,
    //from session
    public static final int WE_STRANGERS = 0, I_KNOW_HIS_SESS = 1,
            HE_KNOW_MY_SESS = 2, WE_FRIENDS = 3;
    //from splash
    public static Long time;
    public static PublicContactCard fileContactCard;
    public static String message;
    public final static int MSG_LIMIT_FOR_QR = 141;
    //form main
    public static Main main;
    public static ContactsDataSource contactsDataSource;
    public static MySimpleArrayAdapter adapter;
    public static String currentText = "";
    public static int currentLayout;
    public static boolean changed;
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
    public final static int LIMIT_FILE_SIZE = 52428800;
    public final static int RESULT_ADD_FILE_FAILED = 5, RESULT_ADD_FILE_TO_BIG = 10, RESULT_ADD_FILE_EMPTY = 20, RESULT_ADD_FILE_OK = 40;
    public static Boolean flag_hash, flag_session, flag_replay;

}
