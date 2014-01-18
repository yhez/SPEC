package specular.systems;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class Backup {
    public static final String Delimiter = "\n";
    public static final String ContactDelimiter = "||";
    private static int conflicts = 0;

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

        //TODO: add a nonce to encryption - sha256(private key)
        byte[] finalResult = new byte[size+1];
        System.arraycopy(my_details, 0, finalResult, 0, my_details.length);
        int location = my_details.length;
        for (int b = 0; b < contacts.length; b++) {
            System.arraycopy(del, 0, finalResult, location, delSize);
            location += delSize;
            System.arraycopy(contacts[b], 0, finalResult, location, contacts[b].length);
            location += contacts[b].length;
        }

        finalResult[finalResult.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_BACKUP);
        //TODO: encrypt finalResult
        return finalResult;
    }

    //TODO:4. groups support
    public static ArrayList<Contact> restore(byte[] data) {
        //return ByteBuffer.allocate(4).putInt(yourInt).array();
        byte[] del = ContactDelimiter.getBytes();
        int delLength = del.length, endPos = data.length, startPos = 0, i=0;
        byte[] temp = new byte[endPos];
        boolean myDetails = false;
        ArrayList<Contact> contactList = new ArrayList<Contact>();

        conflicts = 0;
        while(endPos > startPos){
            Contact c;
            //TODO: add check nonce logic - sha256(private key)
            int pos = java.util.Arrays.asList(data).indexOf(del);
            c = bytes2Contact(java.util.Arrays.copyOfRange(data,startPos,pos-1), myDetails);
            if (c != null){
                contactList.add(i,c);
                i++;
                // TODO: add enum for the search
                if (conflicts < 3){
                    // searching for existing contact by email
                    if ((conflicts == 0 || conflicts == 2)& checkEmail(c)){
                        conflicts+=1;
                    }
                    // searching for existing contact by public key
                    if ((conflicts == 0 || conflicts == 1) && checkPK(c)){
                        conflicts+=2;
                    }
                    //if oth found will be 3
                }
            }

            // new start point for the copy
            startPos+= (pos+delLength-1);
            // help to change the current instance of the delimiter to go for the next one
            data[pos] = 'a';

            // change the loop so only first contact considered as the user
            myDetails = true;
        }
        return contactList;
    }

    // return value false - ok, true - issue with key
    public static boolean checkMyPublicKey(Activity a, Contact c){
        String[] myDetails = CryptMethods.getMyDetails(a);
        boolean publicKeyNotExist = false;

        if ((myDetails[2] != null) && !myDetails[2].equalsIgnoreCase(c.getPublicKey()))
            publicKeyNotExist = true;
        return publicKeyNotExist;
    }

    private static boolean checkEmail(Contact c){
        boolean email = false;
        Contact temp = null;

        temp = ContactsDataSource.contactsDataSource.findContactByEmail(c.getEmail());
        if (temp != null) email = true;

        return email;
    }

    private static boolean checkPK(Contact c){
        boolean pk = false;
        Contact temp = null;

        temp = ContactsDataSource.contactsDataSource.findContactByKey(c.getPublicKey());
        if (temp != null) pk = true;
        return  pk;
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

    private static Contact bytes2Contact(byte[] c, boolean myDetails){
        String str = new String(c);
        Pattern pattern = Pattern.compile(Delimiter, Pattern.LITERAL);
        Contact tempContact;

        String[] details = pattern.split(str, -1);
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

    //enum status

    // return value 0 - ok, 1 - issue with email, 2 - issue with key, 3 - both
    public static int getConflicts() {
        return conflicts;
    }

    // TODO: add enum
    public ArrayList<Contact> cleanList(ArrayList<Contact> cList , int type){
        int i = 0;
        Contact temp = cList.get(i);
        ArrayList<Contact> cleanList = new ArrayList<Contact>();

        while (temp != null){
            // searching for existing contact by email
            if ((conflicts == 1 || conflicts == 3)& checkEmail(temp)){
                continue;
            }
            // searching for existing contact by public key
            if ((conflicts == 2 || conflicts == 3) && checkPK(temp)){
                continue;
            }
            cleanList.add(temp);
            i++;
            temp = cList.get(i);
        }
        return cleanList;
    }
}
