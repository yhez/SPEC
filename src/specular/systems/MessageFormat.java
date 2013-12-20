package specular.systems;

import android.app.Activity;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class MessageFormat {
    public static final int NOT_RELEVANT = 0, OK = 1, NOT_LATEST = 2, OLD = 3, FAILED = 4;
    private String name, email, publicKey, msgContent, session, hash, sentTime, fileName;
    private byte[] fileContent = null;

    public MessageFormat(byte[] raw) {
        int loc = 0;
        boolean found=false;
        while (loc < raw.length - 6) {
            if (raw[loc] == 'f' && raw[loc + 1] == 'i' && raw[loc + 2] == 'l' && raw[loc + 3] == 'e' && raw[loc + 4] == '/' && raw[loc + 5] == '/'){
                found=true;
                break;
            }
            loc++;
        }
        if(!found)
            return;
        byte[] b = new byte[loc];
        System.arraycopy(raw, 0, b, 0, b.length);
        String data[] = new String[0];
        try {
            data = new String(b, "UTF-8").split("\n");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!(data.length < 6)) {
            name = data[0];
            email = data[1];
            publicKey = data[2];
            hash = data[3];
            session = data[4];
            sentTime = data[5];
            fileName = data[6];
            msgContent = "";
            for (int a = 7; a < data.length; a++)
                msgContent += data[a] + (a + 1 == data.length ? "" : "\n");
            if (raw.length > loc + 6) {
                fileContent = new byte[raw.length - (loc + 6)];
                for (int c = loc + 6, d = 0; c < raw.length; c++, d++) {
                    fileContent[d] = raw[c];
                }
            }
        }
    }

    public MessageFormat(byte[] fileContent,String[] myDetails, String fileName, String msgContent, String session) {
        email = myDetails[1];
        this.msgContent = msgContent;
        publicKey = myDetails[2];
        sentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar
                .getInstance().getTime());
        this.session = session;
        this.fileName = fileName;
        this.fileContent = fileContent;
        name = myDetails[0];
        hash = hashing(name + email + publicKey + msgContent + (fileContent != null ? new String(fileContent) : "") + session + sentTime);
    }

    public MessageFormat() {
        email = StaticVariables.decryptedMsg.getEmail();
        publicKey = StaticVariables.decryptedMsg.getPublicKey();
        sentTime = StaticVariables.decryptedMsg.getSentTime();
        session = StaticVariables.decryptedMsg.getSession();
        fileName = StaticVariables.decryptedMsg.getFileName();
        name = StaticVariables.decryptedMsg.getName();
        msgContent = StaticVariables.decryptedMsg.msgContent;
        hash = "";
    }

    public String getFileName() {
        return fileName;
    }

    public boolean checkHash() {
        return checkHash(hash, name + email + publicKey + msgContent + (fileContent != null ? new String(fileContent) : "") + session
                + sentTime);
    }

    public int checkReplay(Contact c,Activity a) {
        if (c == null)
            return NOT_RELEVANT;
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        parser.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            long timeCreated = parser.parse(sentTime).getTime();
            if (timeCreated < c.getLast())
                return NOT_LATEST;
            long now = System.currentTimeMillis();
            long gap = now - timeCreated;
            //60 hours is the limit for old messages
            if (gap / 1000 / 60 / 60 / 60 > 0)
                return OLD;
            c.update(Contact.RECEIVED, timeCreated,a);
            return OK;
        } catch (ParseException e) {
            e.printStackTrace();
            return FAILED;
        }
    }

    private boolean checkHash(String hash, String msg) {
        String sig = hashing(msg);
        return sig.equals(hash);
    }

    public String getEmail() {
        return email;
    }

    public byte[] getFormatedMsg() {
        byte[] msgContent = null;
        byte[] fileName = null;
        try {
            fileName = this.fileName.getBytes("UTF-8");
            msgContent = this.msgContent.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] fm = new byte[name.length() + email.length() + publicKey.length()
                + hash.length() + session.length() + sentTime.length() + fileName.length
                + msgContent.length + (fileContent != null ? fileContent.length : 0) + 13];
        int b = 0;
        for (int a = 0; a < name.length(); b++, a++)
            fm[b] = (byte) name.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < email.length(); b++, a++)
            fm[b] = (byte) email.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < publicKey.length(); b++, a++)
            fm[b] = (byte) publicKey.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < hash.length(); b++, a++)
            fm[b] = (byte) hash.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < session.length(); b++, a++)
            fm[b] = (byte) session.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < sentTime.length(); b++, a++)
            fm[b] = (byte) sentTime.charAt(a);
        fm[b] = '\n';
        b++;
        for (int a = 0; a < fileName.length; b++, a++)
            fm[b] = fileName[a];
        fm[b] = '\n';
        b++;
        for (int a = 0; a < msgContent.length; b++, a++)
            fm[b] = msgContent[a];
        for (int a = 0; a < "file//".length(); b++, a++)
            fm[b] = (byte) "file//".charAt(a);
        if (fileContent != null)
            for (int a = 0; a < fileContent.length; b++, a++)
                fm[b] = fileContent[a];
        return fm;
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
            ignore.printStackTrace();
        }
        return null;
    }
}
