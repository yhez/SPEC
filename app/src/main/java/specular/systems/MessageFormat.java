package specular.systems;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class MessageFormat {
    public static final int NOT_RELEVANT = 0, OK = 1, NOT_LATEST = 2, OLD = 3, FAILED = 4;
    public static MessageFormat decryptedMsg = null;
    private String name, email, msgContent, session, sentTime, fileName;
    private byte[] publicKey,hash,fileContent;

    public MessageFormat(byte[] raw) {
        int loc = 0;
        boolean found = false;
        while (loc < raw.length - 5) {
            if (raw[loc] == 'f' && raw[loc + 1] == 'i' && raw[loc + 2] == 'l' && raw[loc + 3] == 'e' && raw[loc + 4] == '/' && raw[loc + 5] == '/') {
                found = true;
                break;
            }
            loc++;
        }
        if (!found)
            return;
        byte[] b = new byte[loc];
        System.arraycopy(raw, 0, b, 0, b.length);
        String data[];
        try {
            data = new String(b, "UTF-8").split("\n");
        } catch (Exception e) {
            data = new String(b).split("\n");
        }
        if (!(data.length < 6)) {
            name = data[0];
            email = data[1];
            session = data[2];
            sentTime = data[3];
            fileName = data[4];
            msgContent = "";
            for (int a = 5; a < data.length; a++)
                msgContent += data[a] + (a + 1 == data.length ? "" : "\n");
            int keyLen = byteArray2Int(new byte[]{raw[raw.length-5],raw[raw.length-4],raw[raw.length-3],raw[raw.length-2]});
            hash = new byte[]{raw[raw.length-9],raw[raw.length-8],raw[raw.length-7],raw[raw.length-6]};
            publicKey = new byte[keyLen];
            System.arraycopy(raw,raw.length-9-keyLen,publicKey,0,publicKey.length);
            int fileLength = raw.length-(loc+15+keyLen);
            if (raw.length > loc + 15+keyLen) {
                fileContent = new byte[fileLength];
                System.arraycopy(raw,loc+6,fileContent,0,fileContent.length);
            }
        }
    }

    public MessageFormat(byte[] fileContent, String[] myDetails, String fileName, String msgContent, String session) {
        email = myDetails[1];
        this.msgContent = msgContent;
        publicKey = Visual.hex2bin(myDetails[2]);
        sentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar
                .getInstance().getTime());
        this.session = session;
        this.fileName = fileName;
        this.fileContent = fileContent;
        name = myDetails[0];
        byte[] msg = new byte[name.length()+email.length()+msgContent.length()+session.length()+sentTime.length()+publicKey.length+(fileContent!=null?fileContent.length:0)];
        int b=0;
        for(int a=0;a<name.length();a++,b++)
            msg[b] = (byte)name.charAt(a);
        for(int a=0;a<email.length();a++,b++)
            msg[b] = (byte)email.charAt(a);
        for(int a=0;a<msgContent.length();a++,b++)
            msg[b] = (byte)msgContent.charAt(a);
        for(int a=0;a<session.length();a++,b++)
            msg[b] = (byte)session.charAt(a);
        for(int a=0;a<sentTime.length();a++,b++)
            msg[b] = (byte)sentTime.charAt(a);
        System.arraycopy(publicKey,0,msg,b,publicKey.length);
        if(fileContent!=null)
            System.arraycopy(fileContent,0,msg,b+publicKey.length,fileContent.length);
        hash = hashing(msg);
    }

    public String getFileName() {
        return fileName;
    }

    public boolean checkHash() {
        byte[] msg = new byte[name.length()+email.length()+msgContent.length()+session.length()+sentTime.length()+publicKey.length+(fileContent!=null?fileContent.length:0)];
        int b=0;
        for(int a=0;a<name.length();a++,b++)
            msg[b] = (byte)name.charAt(a);
        for(int a=0;a<email.length();a++,b++)
            msg[b] = (byte)email.charAt(a);
        for(int a=0;a<msgContent.length();a++,b++)
            msg[b] = (byte)msgContent.charAt(a);
        for(int a=0;a<session.length();a++,b++)
            msg[b] = (byte)session.charAt(a);
        for(int a=0;a<sentTime.length();a++,b++)
            msg[b] = (byte)sentTime.charAt(a);
        System.arraycopy(publicKey,0,msg,b,publicKey.length);
        if(fileContent!=null)
            System.arraycopy(fileContent,0,msg,b+publicKey.length,fileContent.length);
        return checkHash(hash, hashing(msg));
    }

    public static int checkReplay(Contact c, String sentTime) {
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
            c.update(timeCreated);
            return OK;
        } catch (ParseException e) {
            e.printStackTrace();
            return FAILED;
        }
    }

    private boolean checkHash(byte[] hash, byte[] msg) {
        for(int a=0;a<4;a++)
            if(msg[a]!=hash[a])
                return false;
        return true;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getFormatedMsg() {
        int keyLength = publicKey.length;
        byte[] msgContent;
        byte[] fileName;
        byte[] name;
        try {
            fileName = this.fileName.getBytes("UTF-8");
            msgContent = this.msgContent.getBytes("UTF-8");
            name = this.name.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            fileName = this.fileName.getBytes();
            msgContent = this.msgContent.getBytes();
            name = this.name.getBytes();
        }
        byte[] fm = new byte[keyLength + name.length + email.length()
                + session.length() + sentTime.length() + fileName.length
                + msgContent.length + (fileContent != null ? fileContent.length : 0) + 20/*8 bytes for hash and key size, the others for divider*/];
        int b = 0;
        for (int a = 0; a < name.length; b++, a++)
            fm[b] = name[a];
        fm[b] = '\n';
        b++;
        for (int a = 0; a < email.length(); b++, a++)
            fm[b] = (byte) email.charAt(a);
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
        for(int a=0;a<keyLength;b++,a++)
            fm[b] = publicKey[a];
        for(int a=0;a<4;a++,b++)
            fm[b] = hash[a];
        byte[] key = int2ByteArray(keyLength);
        for(int a=0;a<4;a++,b++)
            fm[b] = key[a];
        fm[fm.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_MSG);
        return fm;
    }

    public String getHash() {
        return byteArray2Int(hash)+"";
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return Visual.bin2hex(publicKey);
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

    private byte[] hashing(byte[] fm) {
        int value = ((fm[0] & 0xFF) << 24) | ((fm[1] & 0xFF) << 16)
                | ((fm[2] & 0xFF) << 8) | (fm[3] & 0xFF);
        for (int a = 1; a < fm.length / 4; a++) {
            value = value ^ ((fm[a * 4] & 0xFF) << 24) | ((fm[a * 4 + 1] & 0xFF) << 16)
                    | ((fm[a * 4 + 2] & 0xFF) << 8) | (fm[a * 4 + 3] & 0xFF);
        }
        return int2ByteArray(value);
    }
    private static byte[] int2ByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
    private static int byteArray2Int(byte[] bytes){
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
