package specular.systems;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;

class MessageFormat {
    private String name, email, publicKey, msgContent, session, hash, sentTime,fileName;
    private byte[] fileContent=null;

    public MessageFormat(String raw) {
        String r[] = raw.split("file//");
        String data[] = r[0].split("\n");
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
            if (r.length > 1) {
                fileContent = r[1].getBytes();
            }
        }
    }

    public MessageFormat(byte[] fileContentt, String fileName, String msgContentt, String sessiont) {
        email = CryptMethods.getEmail();
        msgContent = msgContentt;
        publicKey = CryptMethods.getPublic();
        sentTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar
                .getInstance().getTime());
        session = sessiont;
        this.fileName=fileName;
        fileContent = fileContentt;
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

    public String getFormatedMsg() {
        return name + "\n" + email + "\n" + publicKey + "\n" + hash + "\n"
                + session + "\n" + sentTime + "\n"+fileName+"\n"+ msgContent+ (fileContent!=null?"file//" + new String(fileContent):"");
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
