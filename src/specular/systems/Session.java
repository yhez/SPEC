package specular.systems;

import java.util.Random;

class Session {

    public final static String DIVIDE_SESSIONS="---";
    public final static int UNKNOWN=0,STARTING=1,DONT_TRUST=2,TRUSTED=3,NEW_TRUSTED=4;
    public static int checkAndUpdate(Contact contact, String session) {
        String my[] = contact.getSession().split(DIVIDE_SESSIONS);
        String his[] = session.split(DIVIDE_SESSIONS);
        if(my.length==1){
            if(his.length==1){
                contact.update(-1,null,null,null,contact.getSession()+DIVIDE_SESSIONS+session.replace("my","his"),-1);
                return UNKNOWN;
            }else{
                String mMy[]=my[0].split(" ");
                String hHis[]=his[0].split(" ");
                String hMy[]=his[1].split(" ");
                if(hMy[3].equals(mMy[3])&&hMy[7].equals(mMy[7])){
                    contact.update(-1,null,null,null,contact.getSession()+DIVIDE_SESSIONS+his[0].replace("my","his"),-1);
                    return STARTING;
                }else {
                    return DONT_TRUST;
                }
            }
        }else{
            String mHis[]=my[1].split(" ");
            String hHis[] = his[0].split(" ");
            if(!mHis[3].equals(hHis[3])||!mHis[7].equals(hHis[7])){
                return DONT_TRUST;
            }else{
                if(his.length==2){
                    String mMy[]=my[0].split(" ");
                    String hMy[]=his[1].split(" ");
                    if(!mMy[3].equals(hMy[3])||!mMy[7].equals(hMy[7])){
                        return DONT_TRUST;
                    }else{
                        return TRUSTED;
                    }
                }else{
                    return NEW_TRUSTED;
                }
            }
        }
    }
    private String mySession = "";
    private String mySymbol = "";

    // to create a new session from nothing
    public Session() {
        Random rnd = new Random();
        char f[] = "bcdfghjklmnpqrstwvxz".toCharArray();
        char o[] = "aeiouy".toCharArray();
        char s[] = "~`!12@3#4$5%6^7&8*9(0){[}]|\'.?/,".toCharArray();
        mySession = Character.toString(f[rnd.nextInt(f.length)]).toUpperCase()
                + Character.toString(o[rnd.nextInt(o.length)])
                + Character.toString(f[rnd.nextInt(f.length)]);
        mySymbol = Character.toString(s[rnd.nextInt(s.length)]);
    }

    @Override
    public String toString() {
        return "my session id: "
                + mySession
                + "   my secret sign: "
                + mySymbol;
    }
}
