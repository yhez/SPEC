package specular.systems;


import java.util.ArrayList;
import java.util.List;

public class Group {
    public static List<Group> list;
    byte[] encryptedGroup;
    boolean noPrivateOnDevice;
    String name;
    String locationForMessages;
    String session;
    String publicKey;
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
    public void encrypt(){

    }
}
