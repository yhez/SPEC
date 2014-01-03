package specular.systems;

import android.app.Activity;

import java.util.Random;

public class Session {

    public static final String FLAG_SESSION_VERIFIED = "!!";
    public static final String FLAG_SESSION_MY_SECRET_SENT = "??";
    public static final String FLAG_SESSION_JUST_ADDED = "--";
    public static final String DIVIDER = "||";
    //new guy sent me a message (we both have different sessions and there is no flag session just added)
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
    //session has just updated
    public final static int UPDATED = 4;
    //trying again send my session
    //still not verified your session
    // not trusted
    // try sending him another message
    public final static int AGAIN = 5;
    //something strange going an
    // it's possible that some one trying to fake his identity
    public final static int RESET_SESSION = 6;
    private String word1 = "";
    private String word2 = "";
    private String sign = "";
    private String flagSession = "";

    private Session(String word1, String word2, String sign, String flag) {
        this.word1 = word1;
        this.word2 = word2;
        this.sign = sign;
        this.flagSession = flag;
    }

    //creates new session from nothing
    public Session() {
        Random rnd = new Random();
        char f[] = "bcdfghjklmnpqrstwvxz".toCharArray();
        char o[] = "aeiouy".toCharArray();
        char s[] = "~`!12@3#4$5%6^7&8*9(0){[}]|\'.?/,".toCharArray();
        word1 = Character.toString(f[rnd.nextInt(f.length)]).toUpperCase()
                + Character.toString(o[rnd.nextInt(o.length)])
                + Character.toString(f[rnd.nextInt(f.length)]);
        word2 = Character.toString(f[rnd.nextInt(f.length)])
                + Character.toString(o[rnd.nextInt(o.length)])
                + Character.toString(f[rnd.nextInt(f.length)]);
        sign = Character.toString(s[rnd.nextInt(s.length)]);
        flagSession = FLAG_SESSION_JUST_ADDED;
    }

    public Session(String session) {
        String s = new Session().toString();
        String r[] = parseSession(s);
        String t[] = parseSession(session);
        Session ss = attachBoth(r, t, "");
        word1 = ss.word1;
        word2 = ss.word2;
        sign = ss.sign;
        flagSession = FLAG_SESSION_JUST_ADDED;
    }

    public static int checkAndUpdate(Activity a, Contact contact, String session) {
        //todo checking for not well formatted sessions
        String[] mySavedSession = parseSession(contact.getSession());
        String[] receivedSession = parseSession(session);
        if (mySavedSession[3].equals(FLAG_SESSION_VERIFIED)) {
            if (equals(mySavedSession, receivedSession)) {
                return KNOWN;
            }
            //he lost my session and i sent him a message
            //so he's verified to me, but he needs to verify me
            if (contains(receivedSession, mySavedSession)) {
                contact.update(a, null, null, null, getHisSession(mySavedSession, receivedSession).toString());
                return UPDATED;
            }

            //prevent an attack by some one who send many sessions (brute force)
            if (isItOne(receivedSession)) {
                //he may have lost my session may not
                //let's try create session again
                String newSession = attachBoth(mySavedSession, receivedSession, FLAG_SESSION_VERIFIED).toString();
                contact.update(a, null, null, null, newSession);
            }
            return DONT_TRUST;
        }
        if (mySavedSession[3].equals(FLAG_SESSION_MY_SECRET_SENT)) {
            if (equals(mySavedSession, receivedSession)) {
                contact.update(a, null, null, null, contact.getSession().replace(FLAG_SESSION_MY_SECRET_SENT, FLAG_SESSION_VERIFIED));
                return JUST_KNOWN;
            }
            if (contains(receivedSession, mySavedSession)) {
                contact.update(a, null, null, null, getHisSession(mySavedSession, receivedSession).toString());
                return JUST_KNOWN;
            }
            //may be he doesn't got my message
            //if (isItOne(receivedSession)) {
            //    return AGAIN;
            //}
            return AGAIN;
        }
        if (mySavedSession[3].equals(FLAG_SESSION_JUST_ADDED)) {
            if (isItOne(receivedSession) && isItOne(mySavedSession)) {
                contact.update(a, null, null, null, attachBoth(mySavedSession, receivedSession, FLAG_SESSION_JUST_ADDED).toString());
                return UNKNOWN;
            }
            contact.update(a, null, null, null, new Session().toString());
            return RESET_SESSION;
        }
        return -1;
    }

    private static Session getHisSession(String[] my, String[] his) {
        String word1 = his[0].replace(my[0], "");
        word1 = word1.replace(DIVIDER, "");
        String word2 = his[1].replace(my[1], "");
        word2 = word2.replace(DIVIDER, "");
        String sign = his[2].replace(my[2], "");
        sign = sign.replace(DIVIDER, "");
        return new Session(word1, word2, sign, FLAG_SESSION_VERIFIED);
    }

    private static String[] parseSession(String session) {
        String[] sessions = session.split(" ");
        return new String[]{sessions[0], sessions[1], sessions[2], sessions.length > 3 ? sessions[3] : ""};
    }

    private static boolean contains(String[] a, String[] b) {
        return a[0].contains(b[0]) && a[1].contains(b[1]) && a[2].contains(b[2]);
    }

    private static boolean equals(String[] a, String[] b) {
        return a[0].equals(b[0]) && a[1].equals(b[1]) && a[2].equals(b[2]);
    }

    private static boolean isItOne(String[] session) {
        return session[0].length() == 3 && session[1].length() == 3 && session[2].length() == 1;
    }

    private static Session attachBoth(String[] a, String[] b, String flag) {
        return new Session(a[0] + DIVIDER + b[0], a[1] + DIVIDER + b[1], a[2] + DIVIDER + b[2], flag);
    }

    public static String toHide() {
        return "session words: "
                + "-xxx xxx-"
                + "  secret sign: "
                + "-x-";
    }

    public static String toShow(String session) {
        String[] ses = session.split(" ");
        return "session words: " + ses[0].replace(DIVIDER, "-") + " " + ses[1].replace(DIVIDER, "-") + "  secret sign: " + ses[2].replace(DIVIDER, "-");
    }

    public static void updateFlag(Activity a, Contact contact) {
        if (contact.getSession().endsWith(FLAG_SESSION_JUST_ADDED)) {
            contact.update(a, null, null, null, contact.getSession().replace(FLAG_SESSION_JUST_ADDED, FLAG_SESSION_MY_SECRET_SENT));
        }
    }

    @Override
    public String toString() {
        return word1 + " " + word2 + " " + sign + " " + flagSession;
    }
}
