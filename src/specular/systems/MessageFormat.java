package specular.systems;

import android.util.Log;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageFormat {
    private String name, email, publicKey, msgContent, session, hash, sentTime,fileName;
    private byte[] fileContent=null;

    public MessageFormat(byte[] raw) {
        int loc = 0;
        String r="";
        while (true){
            if(raw[loc]=='f'&&raw[loc+1]=='i'&&raw[loc+2]=='l'&&raw[loc+3]=='e'&&raw[loc+4]=='/'&&raw[loc+5]=='/'){
                break;
            }
            loc++;
        }
        //String r[] = raw.split("file//");
        String data[] =new String(raw,0,loc).split("\n"); //r[0].split("\n");
        if (!(data.length < 6)) {
            name = data[0];
            email = data[1];
            publicKey = data[2];
            hash = data[3];
            session = data[4];
            sentTime = data[5];
            fileName=data[6];
            msgContent = "";
            for (int a = 7; a < data.length; a++)
                msgContent += data[a] + (a + 1 == data.length ? "" : "\n");
            if (raw.length > loc+6) {
                fileContent = new byte[raw.length-(loc+6)];
                for(int c=loc+6,d=0;c<raw.length;c++,d++){
                    fileContent[d]=raw[c];
                }
            }
        }
    }
public String getFileName(){
    return fileName;
}
    public MessageFormat(byte[] fileContent, String fileName, String msgContent, String session) {
        email = CryptMethods.getEmail();
        this.msgContent = msgContent;
        publicKey = CryptMethods.getPublic();
        sentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar
                .getInstance().getTime());
        this.session = session;
        this.fileName=fileName;
        this.fileContent = fileContent;
        name = CryptMethods.getName();
        hash = hashing(name + email + publicKey + msgContent + (fileContent!=null?new String(fileContent):"") + session + sentTime);
    }

    public boolean checkHash() {
        return checkHash(hash, name + email + publicKey + msgContent + (fileContent!=null?new String(fileContent):"") + session
                + sentTime);
    }
    public boolean checkReplay(){
        String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar
                .getInstance().getTime());
        return now.substring(4,6).equals(sentTime.substring(4,6));
    }

    private boolean checkHash(String hash, String msg) {
        String sig = hashing(msg);
        return sig.equals(hash);
    }

    public String getEmail() {
        return email;
    }

    public byte[] getFormatedMsg() {
        byte[] fm = new byte[name.length()+email.length()+publicKey.length()
                +hash.length()+session.length()+sentTime.length()+fileName.length()
                +msgContent.length()+(fileContent!=null?fileContent.length:0)+13];
        Log.d("array length",fm.length+" ");
        int b=0;
        for(int a=0;a<name.length();b++,a++)
            fm[b]=(byte)name.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<email.length();b++,a++)
            fm[b]=(byte)email.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<publicKey.length();b++,a++)
            fm[b]=(byte)publicKey.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<hash.length();b++,a++)
            fm[b]=(byte)hash.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<session.length();b++,a++)
            fm[b]=(byte)session.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<sentTime.length();b++,a++)
            fm[b]=(byte)sentTime.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<fileName.length();b++,a++)
            fm[b]=(byte)fileName.charAt(a);
        fm[b]='\n';b++;
        for(int a=0;a<msgContent.length();b++,a++)
            fm[b]=(byte)msgContent.charAt(a);
        for(int a=0;a<"file//".length();b++,a++)
            fm[b]=(byte)"file//".charAt(a);
        if(fileContent!=null)
        for(int a=0;a<fileContent.length;b++,a++)
            fm[b]=fileContent[a];
        Log.d("current pos",b+" ");
        return fm;
        //return name + "\n" + email + "\n" + publicKey + "\n" + hash + "\n"
        //        + session + "\n" + sentTime + "\n"+fileName+"\n"+ msgContent+
    }

    public String getHash() {
        return hash;
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSentTime() {
        return sentTime;
    }

    public String getSession() {
        return session;
    }

    public byte[] getFileContent() {
        return fileContent;
    }


    String hashing(String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Visual.bin2hex(md.digest(msg.getBytes()));
        } catch (Exception ignore) {
        }
        return null;
    }
}
