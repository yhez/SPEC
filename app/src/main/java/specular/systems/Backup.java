package specular.systems;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by yehezkelk on 12/28/13.
 */
public class Backup {
    public static final String Delimiter ="\n";
    public static boolean backup(Activity a){
        List<Contact> contactList = StaticVariables.contactsDataSource.getAllContacts();
        String[] myDetails = CryptMethods.getMyDetails(a);
        String details = myDetails[0]+ Delimiter+myDetails[1]+Delimiter+myDetails[2]+Delimiter;
        byte[] my_details;
        try {
            my_details = details.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            my_details = details.getBytes();
        }
        int size=my_details.length;
        byte[][] contacts = new byte[contactList.size()][];
        for(int b=0;b<contactList.size();b++){
            contacts[b]= contact2Bytes(contactList.get(b));
            size+=contacts[b].length;
        }

        byte[] finalResult= new byte[size];
        System.arraycopy(my_details,0,finalResult,0,my_details.length);
        int location = my_details.length;
        for(int b =0;b<contacts.length;b++){
            System.arraycopy(contacts[b],0,finalResult,location,contacts[b].length);
            location+=contacts[b].length;
        }
        //byte[] encrypytedData = CryptMethods.encrypt(finalResult,null,CryptMethods.getPublic());
        //FilesManagement.saveBackupFile(encrypytedData);
        //show dialog with the option to upload the file to some place
        return FilesManagement.createBackupFileToSend(a, finalResult);
    }

    public static void restore(Activity a){

    }

    private static byte[] contact2Bytes(Contact c){
        String contact="";
        contact += c.getContactName()+ Delimiter;
        contact += c.getEmail()+ Delimiter;
        contact += c.getPublicKey()+ Delimiter;
        contact += c.getSession()+ Delimiter;
        contact += c.getAdded()+ Delimiter;
        contact += c.getReceived()+ Delimiter;
        contact += c.getSent()+ Delimiter;
        try {
            return contact.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return contact.getBytes();
        }
    }
}
