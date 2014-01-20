package specular.systems;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Backup {
    public static final String Delimiter = "\n";
    public static final String ContactDelimiter = "||";

    public static byte[] backup(Activity a) {
        List<Contact> contactList = ContactsDataSource.contactsDataSource.getAllContacts();
        String[] myDetails = CryptMethods.getMyDetails(a);
        String details = myDetails[0] + Delimiter + myDetails[1] + Delimiter + myDetails[2] + Delimiter;
        byte[] my_details;
        try {
            my_details = details.getBytes("UTF-8");
        } catch (Exception e) {
            my_details = details.getBytes();
        }
        int size = my_details.length;
        byte[] del = ContactDelimiter.getBytes();
        int delSize = del.length;
        byte[][] contacts = new byte[contactList.size()][];
        for (int b = 0; b < contactList.size(); b++) {
            contacts[b] = contact2Bytes(contactList.get(b));
            size += contacts[b].length;
            size += delSize;
        }
        byte[] privateHash = CryptMethods.getPrivateHash();
        size+=privateHash.length;
        //TODO: add a nonce to encryption - sha256(private key)
        byte[] finalResult = new byte[size+1];
        System.arraycopy(privateHash,0,finalResult,0,privateHash.length);
        System.arraycopy(my_details, 0, finalResult, privateHash.length, my_details.length);
        int location = my_details.length+privateHash.length;
        for (int b = 0; b < contacts.length; b++) {
            System.arraycopy(del, 0, finalResult, location, delSize);
            location += delSize;
            System.arraycopy(contacts[b], 0, finalResult, location, contacts[b].length);
            location += contacts[b].length;
        }
        finalResult[finalResult.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_BACKUP);
        return finalResult;
    }

    //TODO:4. groups support
    public static ArrayList<Contact> restore(byte[] rawData) {
        byte[] del = ContactDelimiter.getBytes();
        byte[] hash = new byte[32];
        System.arraycopy(rawData,0,hash,0,hash.length);
        byte[] myHash = CryptMethods.getPrivateHash();
        for(int a=0;a<hash.length;a++)
            if(hash[a]!=myHash[a])
                return null;
        byte[] data = new byte[rawData.length-32];
        System.arraycopy(rawData,32,data,0,data.length);
        String DataTemp = new String(data);
        boolean myDetails = false;
        ArrayList<Contact> contactList = new ArrayList<Contact>();
        Pattern pattern = Pattern.compile(ContactDelimiter, Pattern.LITERAL);
        String[] contactsString = pattern.split(DataTemp, -1);
        //TODO: add check nonce logic - sha256(private key)
        for(int i = 0; i < contactsString.length; i++){
            Contact c = string2Contact(contactsString[i], myDetails);
            if (c != null){
                contactList.add(i,c);
            }

            // change the loop so only first contact considered as the user
            myDetails = true;
        }
        return contactList;
    }



    private static byte[] contact2Bytes(Contact c) {
        String contact = "";
        contact += c.getContactName() + Delimiter;
        contact += c.getEmail() + Delimiter;
        contact += c.getPublicKey() + Delimiter;
        contact += c.getSession() + Delimiter;
        contact += c.getAdded() + Delimiter;
        contact += c.getReceived() + Delimiter;
        contact += c.getSent() + Delimiter;
        try {
            return contact.getBytes("UTF-8");
        } catch (Exception e) {
            return contact.getBytes();
        }
    }

    private static Contact string2Contact(String c, boolean myDetails){
        Pattern pattern = Pattern.compile(Delimiter, Pattern.LITERAL);
        Contact tempContact;

        String[] details = pattern.split(c, -1);
        if (!myDetails){
            tempContact = new Contact(1,details[0], details[1],0,0,0,0,details[2],"","");
        }
        else{
            tempContact = new Contact(1, details[0], details[1], Integer.parseInt(details[4]),
                    System.currentTimeMillis(), Integer.parseInt(details[6]), Integer.parseInt(details[5]),
                    details[2], details[3], "");
        }
        return tempContact;
    }
}
