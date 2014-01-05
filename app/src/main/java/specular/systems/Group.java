package specular.systems;


import java.util.ArrayList;
import java.util.List;

public class Group {
    int id;
    public static List<Group> list;
    byte[] encryptedGroup;
    boolean noPrivateOnDevice;
    String name;
    String locationForMessages;
    String session;
    String publicKey;
    String encryptedFileNameForGroup;
    byte[] privateKey;
    ArrayList<Contact> knownMembers;
    public Group(ArrayList<Contact> knownMembers,String name,String locationForMessages,String groupMentor,String publicKey,byte[] privateKey,boolean noPrivateOnDevice){
        this.noPrivateOnDevice=noPrivateOnDevice;
        this.name=name;
        this.locationForMessages=locationForMessages;
        this.session=groupMentor;
        this.privateKey=privateKey;
        this.publicKey=publicKey;
        this.knownMembers=knownMembers;
    }
    public Group(long id,String name,String locationForMessages,String groupMentor,String publicKey,boolean noPrivateOnDevice){
        this.noPrivateOnDevice=noPrivateOnDevice;
        this.name=name;
        this.locationForMessages=locationForMessages;
        this.session=groupMentor;
        this.privateKey=privateKey;
        this.publicKey=publicKey;
        this.knownMembers=knownMembers;
    }
    public void encrypt(){

    }
    public String getGroupName(){
        return name;
    }
    public String getPublicKey(){
        return publicKey;
    }
    public String getEmail(){
        return locationForMessages;
    }
    public int getId(){
        return id;
    }
    public String getMentor(){
        return session;
    }
}
