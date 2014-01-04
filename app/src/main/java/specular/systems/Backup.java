package specular.systems;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by yehezkelk on 12/28/13.
 */
public class Backup {
    public static final String DIVIDE_FIELDS ="\n";
    public static void backup(Activity a){
        List<Contact> contactList = ContactsDataSource.contactsDataSource.getAllContacts();
        String[] myDetails = CryptMethods.getMyDetails(a);
        String details = myDetails[0]+ DIVIDE_FIELDS+myDetails[1]+DIVIDE_FIELDS+myDetails[2]+DIVIDE_FIELDS;
        byte[] my_details;
        try {
           my_details = details.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            my_details = details.getBytes();
        }
        byte[][] contacts = new byte[contactList.size()][];
        for(int b=0;b<contactList.size();b++){
            contacts[b]= contact2Bytes(contactList.get(b));
        }
        int size=my_details.length;
        for(int b=0;b<contacts.length;b++){
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
    }
    private static byte[] contact2Bytes(Contact c){
        String contacts="";
        contacts += c.getContactName()+ DIVIDE_FIELDS;
        contacts += c.getEmail()+ DIVIDE_FIELDS;
        contacts += c.getPublicKey()+ DIVIDE_FIELDS;
        contacts += c.getSession()+ DIVIDE_FIELDS;
        contacts += c.getAdded()+ DIVIDE_FIELDS;
        contacts += c.getReceived()+ DIVIDE_FIELDS;
        contacts += c.getSent()+ DIVIDE_FIELDS;
        try {
            return contacts.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return contacts.getBytes();
        }
    }
}
