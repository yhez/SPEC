package specular.systems;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by yehezkelk on 12/17/13.
 */
public class LightMessage {

    public static LightMessage decryptedLightMsg = null;
    private String msgContent, sentTime;
    private String hash;

    public LightMessage(byte[] raw) {
        try {
            String[] data = new String(raw, "UTF-8").split("\n");
            sentTime = data[0];
            hash = data[1];
            msgContent = "";
            for (int a = 2; a < data.length; a++)
                msgContent += data[a] + (a + 1 == data.length ? "" : "\n");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean checkHash() {
        String tmp = hash;
        hash = null;
        hash = myHash(getFormatedMsg());
        Log.w("orig", tmp);
        Log.w("new", hash);
        return hash.equals(tmp);
    }

    public LightMessage(String msgContent) {
        this.msgContent = msgContent;
        sentTime = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar
                .getInstance().getTime());
        hash = myHash(getFormatedMsg());
    }

    public String getMsgContent() {
        return msgContent;
    }

    public String getSentTime() {
        return sentTime;
    }

    public String getHash() {
        return hash;
    }

    public byte[] getFormatedMsg() {
        byte[] msgContent = null;
        try {
            msgContent = this.msgContent.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //light message contains short time stamp + msg + short hash
        byte[] fm = new byte[sentTime.length() + msgContent.length + 2 + (hash != null ? (hash.length() + 2) : 0)];
        int index = 0;
        for (int a = 0; a < sentTime.length(); a++)
            fm[index++] = (byte) sentTime.charAt(a);
        if (hash != null) {
            fm[index++] = '\n';
            for (int a = 0; a < hash.length(); a++)
                fm[index++] = (byte) hash.charAt(a);
        }
        fm[index++] = '\n';
        for (byte aMsgContent : msgContent) fm[index++] = aMsgContent;
        if(hash!=null){
            fm[fm.length-1]=(byte)FileParser.getFlag(FileParser.ENCRYPTED_QR_MSG);
        }
        return fm;
    }

    private String myHash(byte[] fm) {
        int value = ((fm[0] & 0xFF) << 24) | ((fm[1] & 0xFF) << 16)
                | ((fm[2] & 0xFF) << 8) | (fm[3] & 0xFF);
        for (int a = 1; a < fm.length / 4; a++) {
            value = value ^ ((fm[a * 4] & 0xFF) << 24) | ((fm[a * 4 + 1] & 0xFF) << 16)
                    | ((fm[a * 4 + 2] & 0xFF) << 8) | (fm[a * 4 + 3] & 0xFF);
        }
        return value + "";
    }

    public static final int NEW = 0, WEEK = 1, TWO_WEEKS = 2, MONTH = 3;

    public static int checkReplay(String time) {
        SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        parser.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            long timeCreated = parser.parse(time).getTime();
            long now = System.currentTimeMillis();
            long gap = now - timeCreated;
            long day = 1000 * 60 * 60 * 24;
            long week = day * 7;
            long two_weeks = week * 2;
            long month = two_weeks * 2;
            if (gap < day)
                return NEW;
            if (gap < week)
                return WEEK;
            if (gap < two_weeks)
                return TWO_WEEKS;
            return MONTH;
        } catch (ParseException e) {
            e.printStackTrace();
            return MONTH;
        }
    }
}
