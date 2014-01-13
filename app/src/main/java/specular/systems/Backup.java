package specular.systems;

import android.app.Activity;

import java.util.List;


public class Backup {
    public static final String Delimiter = "\n";

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
        byte[][] contacts = new byte[contactList.size()][];
        for (int b = 0; b < contactList.size(); b++) {
            contacts[b] = contact2Bytes(contactList.get(b));
            size += contacts[b].length;
        }

        byte[] finalResult = new byte[size+1];
        System.arraycopy(my_details, 0, finalResult, 0, my_details.length);
        int location = my_details.length;
        for (int b = 0; b < contacts.length; b++) {
            System.arraycopy(contacts[b], 0, finalResult, location, contacts[b].length);
            location += contacts[b].length;
        }
        finalResult[finalResult.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_BACKUP);
        return finalResult;
    }

    public static void restore(Activity a) {

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
}
