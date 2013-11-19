package specular.systems;

import java.util.Random;

class Session
{

    public static int check(Contact contact, String session)
	{
        String my[] = contact.getSession().split(" ");
        String his[] = session.split(" ");
        String mMySess = my[3] + my[7];
        // String mHisSess = my.length >= 16 ? my[11] + my[15] : "";
        String hHisSess = his.length >= 8 ? his[3] + my[7] : "";
        String hMySess = his.length >= 16 ? his[11] + my[15] : "";
        if (contact.getConversationStatus() == PublicStaticVariables.WE_FRIENDS)
		{
            if (hMySess.equals("") || !hHisSess.equals(mMySess)
				|| hHisSess.equals(""))
                return PublicStaticVariables.FAILED;
            return PublicStaticVariables.VERIFIED;
        }
        return PublicStaticVariables.DONT_TRUST;
        /*
		 * if(Contact.WE_STRANGERS==contact.getConversationStatus()){
		 * if(hMySess.equals("")||hHisSess.equals("")) return DONT_TRUST;
		 * contact.update(a, null, null, null, new Session(mMySess, hHisSess) +
		 * "", null); return } if
		 * (Contact.HE_KNOW_MY_SESS==contact.getConversationStatus()) return ;
		 * else return UNKNOWN;
		 */
    }

    private String hisSession = "";
    private String hisSymbol = "";
    // VERIFIED_BUT_DONT_TRUST
    // = 4;
    private String mySession = "";

    private String mySymbol = "";

    // to create a new session from nothing
    public Session()
	{
        Random rnd = new Random();
        char f[] = "bcdfghjklmnpqrstwvxz".toCharArray();
        char o[] = "aeiouy".toCharArray();
        char s[] = "~`!12@3#4$5%6^7&8*9(0){[}]|\'.?/,".toCharArray();
        mySession = Character.toString(f[rnd.nextInt(f.length)]).toUpperCase()
			+ Character.toString(o[rnd.nextInt(o.length)])
			+ Character.toString(f[rnd.nextInt(f.length)]);
        mySymbol = Character.toString(s[rnd.nextInt(s.length)]);
    }

    /*
     * private Session(String my, String his) { this.mySession = my.substring(0,
     * my.length() - 1); this.hisSession = his.substring(0, his.length() - 1);
     * this.hisSymbol = his.substring(his.length() - 1); this.mySymbol =
     * my.substring(my.length() - 1); }
     */
    public Session(String his)
	{
        String[] data = his.split(" ");
        hisSession = data[3];
        hisSymbol = data[7];
        Session s = new Session();
        mySession = s.mySession;
        mySymbol = s.mySymbol;
    }

    @Override
    public String toString()
	{
        return "my session id: "
			+ mySession
			+ "   my secret sign: "
			+ mySymbol
			+ " "
			+ (hisSession.equals("") ? "" : "\nYour session id: "
			+ hisSession + " ;    Your secret sign: " + hisSymbol);
    }
}
