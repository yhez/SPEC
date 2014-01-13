package specular.systems;


import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Bitmap;

import java.util.List;

import zxing.QRCodeEncoder;
import zxing.WriterException;

public class Group {
    private long id;
    public static List<Group> list;
    private byte[] encryptedGroup;
    private String defaultAPP;
    private String ownerName;
    private String ownerEmail;
    private String ownerPublicKey;
    private boolean noPrivateOnDevice;
    private String name;
    private String locationForMessages;
    private String session;
    private String publicKey;
    private boolean dontAllowNewMembers;
    private String encryptedPrivateFileNameForGroup;
    private byte[] privateKey;

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
        defaultAPP="";
        if(GroupDataSource.groupDataSource==null)
            GroupDataSource.groupDataSource = new GroupDataSource(a);
        this.id = GroupDataSource.groupDataSource.createGroup(a, this);
        StaticVariables.fullListG.add(this);
    }

    //getting a group from db
    public Group(long id, String name, String locationForMessages, String groupMentor, String publicKey,byte[] privatek,
                 boolean noPrivateOnDevice, boolean dontAllowNewMembers,
                 String ownerName, String ownerEmail, String ownerPublicKey,String defaultApp) {
        this.noPrivateOnDevice = noPrivateOnDevice;
        this.dontAllowNewMembers = dontAllowNewMembers;
        this.name = name;
        this.locationForMessages = locationForMessages;
        this.session = groupMentor;
        this.privateKey=privatek;
        this.publicKey = publicKey;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.ownerPublicKey = ownerPublicKey;
        this.id = id;
        this.defaultAPP=defaultApp;
    }

    //getting a group from an encrypted file
    public Group(String rawData) {

    }
    public byte[] getGroupToShare(){
        String s = name+"\n"+session+"\n"+locationForMessages+"\n"+publicKey+"\nowner_details\n"
                +ownerName+"\n"+ownerEmail+"\n"+ownerPublicKey+"\n"+
                (dontAllowNewMembers?"private_group\n":"")+(noPrivateOnDevice?"nfc_required\n":""+"private_key");
        byte[] strLength;
        try {
            strLength = s.getBytes("UTF-8");
        } catch (Exception e) {
            strLength = s.getBytes();
        }
        byte[] data = new byte[strLength.length+privateKey.length+1];
        int a;
        for(a=0;a<strLength.length;a++){
            data[a]=strLength[a];
        }
        for(;a<privateKey.length;a++){
            data[a]=privateKey[a-strLength.length];
        }
        data[data.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_GROUP);
        return data;
    }
    public boolean getLimitNFC(){
        return noPrivateOnDevice;
    }
    public boolean getLimitInvite(){
        return dontAllowNewMembers;
    }
    public String getGroupName() {
        return name;
    }
    public byte[] getPrivateKey(){return privateKey;}
    public String getPublicKey() {
        return publicKey;
    }

    public String getEmail() {
        return locationForMessages;
    }

    public long getId() {
        return id;
    }

    public ComponentName getDefaultApp() {
        if(defaultAPP.contains("\n"))
            return new ComponentName(defaultAPP.split("\n")[0], defaultAPP.split("\n")[1]);
        return null;
    }
    public void update(String defaultApp, Activity a) {
        if (defaultApp != null) {
            this.defaultAPP=defaultApp;
            GroupDataSource.groupDataSource.updateDB(id, defaultApp);
            GroupsAdapter.updateCont(a, this);
        } else {
            this.defaultAPP = null;
            ContactsDataSource.contactsDataSource.updateDB(id, "");
            GroupsAdapter.updateCont(a, this);
        }
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
    public Bitmap getPhoto() {
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(publicKey, 128);
        Bitmap bitmap = null;
        try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    public void update(Activity a, String contactName, String email,
                       String publicKey, String session) {
        if (contactName != null)
            this.name = contactName;
        if (publicKey != null)
            this.publicKey = publicKey;
        if (email != null)
            this.locationForMessages = email;
        if (session != null)
            this.session = session;
        ContactsDataSource.contactsDataSource.updateDB(id,
                contactName, email, publicKey, session);
        GroupsAdapter.updateCont(a, this);
    }
}
