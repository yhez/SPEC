package specular.systems;

import android.app.Activity;

class QRPublicKey
{
    private Activity a;
    private String publicKey = null, email = null, name = null;

    public QRPublicKey(Activity a)
	{
        if (a != null)
		{
            publicKey = CryptMethods.getPublic();
            email = CryptMethods.getEmail();
            name = CryptMethods.getName();
            this.a = a;
        }
    }

 /*   public QRPublicKey(Activity a, Contact contact)
	{
        publicKey = contact.getPublicKey();
        email = contact.getEmail();
        name = contact.getContactName();
        this.a = a;
    }
*/
    public QRPublicKey(Activity a, String raw)
	{
        if (validate(raw))
		{
            String data[] = raw.split("\n");
            publicKey = data[3];
            email = data[2];
            name = data[1];
            this.a = a;
        }
    }

    public String getEmail()
	{
        return email;
    }

    public String getName()
	{
        return name;
    }

    public String getPublicKey()
	{
        return publicKey;
    }

    public String getQRToPublish()
	{
        String explain = a.getResources().getString(R.string.explain);
        return explain + "\n" + name + "\n" + email + "\n" + publicKey;
    }

    boolean validate(String raw)
	{
        if (raw == null)
            return false;
        String data[] = raw.split("\n");
        if (data.length != 4)
            return false;
        String l = data[3].replaceAll("A", "");
        for (char a = '0'; a <= '9'; a++)
            l = l.replaceAll(Character.toString(a), "");
        for (char a = 'B'; a < 'G'; a++)
            l = l.replaceAll(Character.toString(a), "");
        return l.length() == 0;
    }
}
