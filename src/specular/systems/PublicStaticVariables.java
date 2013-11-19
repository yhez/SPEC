package specular.systems;

import java.util.List;

/**
 * Created by yehezkelk on 11/19/13.
 */
public class PublicStaticVariables {
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
    public static ContactsDataSource contactsDataSource;
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
    public static FragmentManagement f;
    public static LastUsedContacts luc;
    //from files management
    public final static int RESULT_ADD_FILE_FAILED=5,RESULT_ADD_FILE_TO_BIG=10,RESULT_ADD_FILE_EMPTY=20,RESULT_ADD_FILE_OK=40;
    public static boolean flag_hash=false,flag_session=false,flag_replay=false;

}
