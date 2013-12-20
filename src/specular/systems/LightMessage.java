package specular.systems;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by yehezkelk on 12/17/13.
 */
public class LightMessage {

    private String msgContent, sentTime;
    private String hash;
    public LightMessage(byte[] raw){
        try {
            String[] data = new String(raw, "UTF-8").split("\n");
            sentTime=data[0];
            hash=data[1];
            msgContent = "";
            for (int a = 2; a < data.length; a++)
                msgContent += data[a] + (a + 1 == data.length ? "" : "\n");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public boolean checkHash(){
        return hash.equals(myHash(getFormatedMsg()));
    }
    public LightMessage(String msgContent){
        this.msgContent=msgContent;
        sentTime=new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar
                .getInstance().getTime());
        hash=myHash(getFormatedMsg());
    }
    public String getMsgContent(){
        return msgContent;
    }
    public String getSentTime(){
        return sentTime;
    }
    public String getHash(){
        return hash;
    }
    public byte[] getFormatedMsg(){
        byte[] msgContent = null;
        try {
            msgContent = this.msgContent.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //light message contains short time stamp + msg + short hash
        byte[] fm = new byte[sentTime.length() + msgContent.length+2+(hash!=null?(hash.length()+1):0)];
        int index=0;
        for(int a=0;a<sentTime.length();a++)
            fm[index++]=(byte)sentTime.charAt(a);
        if(hash!=null){
            fm[index++]='\n';
            for(int a = 0;a<hash.length();a++)
                fm[index++]=(byte)hash.charAt(a);
        }
        fm[index++]='\n';
        for(int a=0;a<msgContent.length;a++)
            fm[index++]=msgContent[a];
        return fm;
    }
    private String myHash(byte[] fm){
        int value = ((fm[0] & 0xFF) << 24) | ((fm[1] & 0xFF) << 16)
                | ((fm[2] & 0xFF) << 8) | (fm[3] & 0xFF);
        for(int a=1;a<fm.length/4;a++){
            value =value ^ ((fm[a*4] & 0xFF) << 24) | ((fm[a*4+1] & 0xFF) << 16)
                    | ((fm[a*4+2] & 0xFF) << 8) | (fm[a*4+3] & 0xFF);
        }
        return value+"";
    }
}
