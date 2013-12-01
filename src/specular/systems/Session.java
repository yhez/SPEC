package specular.systems;

import java.util.Random;

class Session {
    public final static int WE_KNOW_EACH_OTHER=0,I_KNOW_HIM=1,HE_KNOWS_ME=2,WE_STRANGERS=3;
    public final static int UNKNOWN=0,STARTING=1,DONT_TRUST=2,TRUSTED=3,NEW_TRUSTED=4;
    public static int checkAndUpdate(Contact contact, String session) {
        String my[] = contact.getSession().split("\n");
        String his[] = session.split("\n");
        if(my.length==1){
            if(his.length==1){
                PublicStaticVariables.contactsDataSource.updateDB(contact.getId(),null,null,null,contact.getSession()+"\n"+session.replace("my","his"),-1);
                return UNKNOWN;
            }else{
                String mMy[]=my[0].split(" ");
                String hHis[]=his[0].split(" ");
                String hMy[]=his[1].split(" ");
                if(hMy[3].equals(mMy[3])&&hMy[7].equals(mMy[7])){
                    PublicStaticVariables.contactsDataSource.updateDB(contact.getId(),null,null,null,contact.getSession()+"\n"+session.replace("my","his"),-1);
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

    private String hisSession = "";
    private String hisSymbol = "";
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
                + mySymbol
                + " "
                + (hisSession.equals("") ? "" : "\nhis session id: "
                + hisSession + " ;    his secret sign: " + hisSymbol);
    }
}
