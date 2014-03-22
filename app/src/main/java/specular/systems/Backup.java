package specular.systems;

import android.app.Activity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Backup {
    public static final String Delimiter = Visual.strings.NEW_LINE;
    public static final String ContactDelimiter = Visual.strings.TAB;

    public static byte[] backup(Activity a) {
        List<Contact> contactList = ContactsDataSource.contactsDataSource.getAllContacts();
        String[] myDetails = CryptMethods.getMyDetails(a);
        String details = myDetails[0] + Delimiter + myDetails[1] + Delimiter + myDetails[2] + Delimiter;
        byte[] my_details;
        try {
            my_details = details.getBytes(Visual.strings.UTF);
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

        byte[] finalResult = new byte[size+1];
        System.arraycopy(privateHash,0,finalResult,0,privateHash.length);
        System.arraycopy(my_details, 0, finalResult, privateHash.length, my_details.length);
        int location = my_details.length+privateHash.length;
        for (byte[] contact : contacts) {
            System.arraycopy(del, 0, finalResult, location, delSize);
            location += delSize;
            System.arraycopy(contact, 0, finalResult, location, contact.length);
            location += contact.length;
        }
        finalResult[finalResult.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_BACKUP);
        byte[] hash;
        try {
            MessageDigest md = MessageDigest.getInstance(Visual.strings.SHA);
            hash =  md.digest(finalResult);
        } catch (NoSuchAlgorithmException e) {
            hash = new byte[0];
        }
        byte[] finalFinalResult = new byte[hash.length+finalResult.length];
        System.arraycopy(hash,0,finalFinalResult,0,hash.length);
        System.arraycopy(finalResult,0,finalFinalResult,hash.length,finalResult.length);
        return finalFinalResult;
    }

    //TODO:4. groups support
    public static ArrayList<Contact> restore() {
        //byte[] del = ContactDelimiter.getBytes();
        byte[] currHash = new byte[32];
        System.arraycopy(StaticVariables.decryptedBackup,0,currHash,0,currHash.length);
        byte[] dataToHash = new byte[StaticVariables.decryptedBackup.length-32];
        System.arraycopy(StaticVariables.decryptedBackup,currHash.length,dataToHash,0,dataToHash.length);
        byte[] newHash;
        try {
            MessageDigest md = MessageDigest.getInstance(Visual.strings.SHA);
            newHash =  md.digest(dataToHash);
        } catch (NoSuchAlgorithmException e) {
            newHash = new byte[0];
        }
        for(int a=0;a<currHash.length;a++){
            if(currHash[a]!=newHash[a])
                return null;
        }
        byte[] hash = new byte[32];
        System.arraycopy(dataToHash,0,hash,0,hash.length);
        byte[] myHash = CryptMethods.getPrivateHash();
        for(int a=0;a<hash.length;a++)
            if(hash[a]!=myHash[a])
                return null;
        byte[] data = new byte[dataToHash.length-32];
        System.arraycopy(dataToHash,32,data,0,data.length);
        String DataTemp = new String(data);
        //boolean myDetails = false;
        ArrayList<Contact> contactList = new ArrayList<Contact>();
        Pattern pattern = Pattern.compile(ContactDelimiter, Pattern.LITERAL);
        String[] contactsString = pattern.split(DataTemp, -1);
        for(int i = 0; i < contactsString.length; i++){
            Contact c = string2Contact(contactsString[i]);
            if (c != null){
                contactList.add(i,c);
            }
        }
        StaticVariables.decryptedBackup=null;
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
            return contact.getBytes(Visual.strings.UTF);
        } catch (Exception e) {
            return contact.getBytes();
        }
    }

    private static Contact string2Contact(String c){
        Pattern pattern = Pattern.compile(Delimiter, Pattern.LITERAL);
        Contact tempContact;

        String[] details = pattern.split(c, -1);
        if (details.length==4){
            tempContact = new Contact(1,details[0], details[1],0,0,0,0,details[2],"","");
        }
        else{
            tempContact = new Contact(1, details[0], details[1], Integer.parseInt(details[4]),
                    0, Integer.parseInt(details[6]), Integer.parseInt(details[5]),
                    details[2], details[3], "");
        }
        return tempContact;
    }
}
