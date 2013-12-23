package specular.systems;

import android.app.Activity;

public class ContactCard {
    private Activity a;
    private String publicKey = null, email = null, name = null;

    public ContactCard(Activity a) {
        if (a != null) {
            String[] details = CryptMethods.getMyDetails(a);
            name = details[0];
            email=details[1];
            publicKey=details[2];
            this.a = a;
        }
    }

    public ContactCard(Activity a, String publicKey, String email, String name) {
        this.publicKey = publicKey;
        this.email = email;
        this.name = name;
        this.a = a;
    }

    public ContactCard(Activity a, String raw) {
        if (validate(raw)) {
            String data[] = raw.split("\n");
            publicKey = data[3];
            email = data[2];
            name = data[1];
            this.a = a;
        }
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getQRToPublish() {
        String explain = a.getResources().getString(R.string.explain);
        return explain + "\n" + name + "\n" + email + "\n" + publicKey;
    }

    private boolean validate(String raw) {
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
