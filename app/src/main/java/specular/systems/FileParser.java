package specular.systems;


public class FileParser {
    public static final int
            ENCRYPTED_GROUP=0,
            ENCRYPTED_MSG=1,
            ENCRYPTED_QR_MSG=2,
            ENCRYPTED_BACKUP=3,
            CONTACT_CARD=4;
    public static final char[] flags = "bgMm".toCharArray();
    public static char getFlag(int type){
        switch (type){
            case ENCRYPTED_BACKUP:
                return flags[0];
            case ENCRYPTED_GROUP:
                return flags[1];
            case ENCRYPTED_MSG:
                return flags[2];
            case ENCRYPTED_QR_MSG:
                return flags[3];
        }
        return 0;
    }
    public static int getType(byte[] raw){
        char c = (char)raw[raw.length-1];
        if(c==flags[0])
            return ENCRYPTED_BACKUP;
        if(c==flags[1])
            return ENCRYPTED_GROUP;
        if(c==flags[2])
            return ENCRYPTED_MSG;
        if(c==flags[3])
            return ENCRYPTED_QR_MSG;
        return -1;
    }
    public static int getType(String raw){
        char c = raw.charAt(raw.length()-1);
        if(c==flags[0])
            return ENCRYPTED_BACKUP;
        if(c==flags[1])
            return ENCRYPTED_GROUP;
        if(c==flags[2])
            return ENCRYPTED_MSG;
        if(c==flags[3])
            return ENCRYPTED_QR_MSG;
        if(raw.contains("\n"))
            return CONTACT_CARD;
        return -1;
    }
}
