package specular.systems;

import android.app.Activity;

import java.util.Random;

public class Session {

    public static final String FLAG_SESSION_VERIFIED = "!!";

    //new guy sent me a message (we both have different sessions and there is no flag session verified)
    //I'll send him my + his.
    //for now he's a stranger
    public final static int UNKNOWN = 0;

    //known guy sent me a message (his session is the same that i have
    //no need to change session
    //session is trusted
    public final static int KNOWN = 1;

    //just verified that he owned the public key (the other guy sent double session with my in it)
    //from now on we'll use the other guy session
    //session is trusted and add flag session verified
    public final static int JUST_KNOWN = 2;

    //can't create session (the other guy sent double session with my not in it,
    //  or if we have different session and flag session verified is marked)
    //trying to send my session again
    //session suspicious
    public final static int DONT_TRUST = 3;

    public static int checkAndUpdate(Activity a,Contact contact, String session) {
        String[] sessions = contact.getSession().split(" ");
        String[] words = new String[]{sessions[2],sessions[3],sessions[6]};
        String his[] = session.split(DIVIDE_SESSIONS);
        if (my.length == 1) {
            if (his.length == 1) {
                contact.update(a, null, null, null, contact.getSession() + DIVIDE_SESSIONS + session.replace(MY, HIS));
                return UNKNOWN;
            } else {
                String mMy[] = my[0].split(" ");
                //String hHis[]=his[0].split(" ");
                String hMy[] = his[1].split(" ");
                if (hMy[3].equals(mMy[3]) && hMy[7].equals(mMy[7])) {
                    contact.update(a, null, null, null, contact.getSession() + DIVIDE_SESSIONS + his[0].replace(MY, HIS));
                    return STARTING;
                } else {
                    return DONT_TRUST;
                }
            }
        } else {
            String mHis[] = my[1].split(" ");
            String hHis[] = his[0].split(" ");
            if (!mHis[3].equals(hHis[3]) || !mHis[7].equals(hHis[7])) {
                return DONT_TRUST;
            } else {
                if (his.length == 2) {
                    String mMy[] = my[0].split(" ");
                    String hMy[] = his[1].split(" ");
                    if (!mMy[3].equals(hMy[3]) || !mMy[7].equals(hMy[7])) {
                        return DONT_TRUST;
                    } else {
                        return TRUSTED;
                    }
                } else {
                    return NEW_TRUSTED;
                }
            }
        }
    }

    private String words = "";
    private String sign = "";

    //creates new session from nothing
    public Session() {
        Random rnd = new Random();
        char f[] = "bcdfghjklmnpqrstwvxz".toCharArray();
        char o[] = "aeiouy".toCharArray();
        char s[] = "~`!12@3#4$5%6^7&8*9(0){[}]|\'.?/,".toCharArray();
        words = Character.toString(f[rnd.nextInt(f.length)]).toUpperCase()
                + Character.toString(o[rnd.nextInt(o.length)])
                + Character.toString(f[rnd.nextInt(f.length)])
                +" "+Character.toString(f[rnd.nextInt(f.length)])
                + Character.toString(o[rnd.nextInt(o.length)])
                + Character.toString(f[rnd.nextInt(f.length)]);
        sign = Character.toString(s[rnd.nextInt(s.length)]);
    }
    @Override
    public String toString() {
        return "session words: "
                + words
                + "  secret sign: "
                + sign;
    }

    public static String toHide() {
        return "session words: "
                + "-xxx xxx-"
                + "  secret sign: "
                + "-x-";
    }
}
