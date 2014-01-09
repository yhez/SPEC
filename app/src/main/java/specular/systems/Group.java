package specular.systems;


import android.app.Activity;

import java.util.List;

public class Group {
    long id;
    public static List<Group> list;
    byte[] encryptedGroup;
    String ownerName;
    String ownerEmail;
    String ownerPublicKey;
    boolean noPrivateOnDevice;
    String name;
    String locationForMessages;
    String session;
    String publicKey;
    boolean dontAllowNewMembers;
    String encryptedPrivateFileNameForGroup;
    byte[] privateKey;

    //creates new from nothing
    public Group(Activity a, String name, String locationForMessages, String groupMentor, String publicKey,
                 byte[] privateKey, boolean noPrivateOnDevice, boolean dontAllowNewMembers) {
        this.noPrivateOnDevice = noPrivateOnDevice;
        this.name = name;
        this.locationForMessages = locationForMessages;
        this.session = groupMentor;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.dontAllowNewMembers = dontAllowNewMembers;
        String[] details = CryptMethods.getMyDetails(a);
        ownerName = details[0];
        ownerEmail = details[1];
        ownerPublicKey = details[2];
        this.id = GroupDataSource.groupDataSource.createGroup(a, this);
    }

    //getting a group from db
    public Group(long id, String name, String locationForMessages, String groupMentor, String publicKey,
                 boolean noPrivateOnDevice, boolean dontAllowNewMembers,
                 String ownerName, String ownerEmail, String ownerPublicKey) {
        this.noPrivateOnDevice = noPrivateOnDevice;
        this.dontAllowNewMembers = dontAllowNewMembers;
        this.name = name;
        this.locationForMessages = locationForMessages;
        this.session = groupMentor;
        this.publicKey = publicKey;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.ownerPublicKey = ownerPublicKey;
        this.id = id;
    }

    //getting a group from an encrypted file
    public Group(String rawData) {

    }
    public byte[] getLightGroupToShare(){
        String s = name+"\n"+session+"\n"+locationForMessages+"\n"+
                (dontAllowNewMembers?"private_group\n":"")+(noPrivateOnDevice?"nfc_required\n":""+"private_key");
        byte[] strLength;
        try {
            strLength = s.getBytes("UTF-8");
        } catch (Exception e) {
            strLength = s.getBytes();
        }
        byte[] data = new byte[strLength.length+privateKey.length];
        int a;
        for(a=0;a<strLength.length;a++){
            data[a]=strLength[a];
        }
        for(;a<data.length;a++){
            data[a]=privateKey[a-strLength.length];
        }
        return data;
    }

    public void encrypt() {

    }

    public String getGroupName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getEmail() {
        return locationForMessages;
    }

    public long getId() {
        return id;
    }

    public String getMentor() {
        return session;
    }

    public Contact getOwnerContact() {
        Contact c = ContactsDataSource.contactsDataSource.findContactByKey(ownerPublicKey);
        return c;
    }

    public String[] getOwnerDetails() {
        return new String[]{ownerName, ownerEmail, ownerPublicKey};
    }
}
