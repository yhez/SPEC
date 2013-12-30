package specular.systems;

import android.app.Activity;
import android.content.ComponentName;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class Contact {

    private String contactName;
    private String email;
    private long id;
    private String publicKey;
    private String session;
    private int added;
    private long last;
    private int sent;
    private int received;
    private ComponentName cn;

    // new contact from scratch
    public Contact(Activity a,String contactName, String email,
                   String publicKey, String session) {
        if (publicKey != null) {
            this.contactName = contactName;
            this.publicKey = publicKey;
            this.email = email;
            String tmp = new Session().toString();
            this.session = session==null?tmp:Session.combineUs(tmp,Session.getHisFromHis(session));
            this.sent = 0;
            this.received = session != null ? 1 : 0;
            this.sent = 0;
            this.last = session != null ? System.currentTimeMillis() : 0;
            this.id = StaticVariables.contactsDataSource.createContact(a,this);
        }
    }

    //create contact after pulling out from db
    public Contact(long id, String contactName, String email, int added, long last, int sent, int received, String publicKey,
                   String session,String app) {
        this.id = id;
        this.contactName = contactName;
        this.publicKey = publicKey;
        this.email = email;
        this.session = session;
        this.added = added;
        this.last = last;
        this.received = received;
        this.sent = sent;
        if(app!=null&&app.length()>0)
            cn=new ComponentName(app.split("\n")[0],app.split("\n")[1]);
    }

    public Bitmap getPhoto() {

        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(publicKey, BarcodeFormat.QR_CODE.toString(), 128);
        Bitmap bitmap = null;
        try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }
        /*int sqrSize;
        for (int x = 0; ; x++)
            if (bitmap.getPixel(x, x) != Color.BLACK) {
                sqrSize = x;
                break;
            }
        int width = bitmap.getWidth() / sqrSize;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < width; y++)
                if (bitmap.getPixel(x * sqrSize, y * sqrSize) == Color.WHITE)
                    for (int z = x * sqrSize; z < sqrSize * (x + 1); z++)
                        for (int q = y * sqrSize; q < sqrSize * (y + 1); q++)
                            bitmap.setPixel(z, q, Color.TRANSPARENT);
                else
                    for (int z = x * sqrSize; z < sqrSize * (x + 1); z++)
                        for (int q = y * sqrSize; q < sqrSize * (y + 1); q++)
                            bitmap.setPixel(z, q, Color.parseColor("#"+color));
                            */
        return bitmap;
    }

    public String getContactName() {
        return contactName;
    }

    public String getEmail() {
        return email;
    }

    public long getId() {
        return id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSession() {
        return session;
    }

    public int getAdded() {
        return added;
    }

    public long getLast() {
        return last;
    }

    public int getSent() {
        return sent;
    }

    public int getReceived() {
        return received;
    }

    public ComponentName getDefaultApp(){
        return cn;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return contactName + "\n" + email + "\n" + publicKey.substring(0, 10)
                + "...." + publicKey.substring(publicKey.length() - 10) + "\n"
                + session;
    }

    public void update(Activity a, String contactName, String email,
                       String publicKey, String session) {
        if (contactName != null)
            this.contactName = contactName;
        if (publicKey != null)
            this.publicKey = publicKey;
        if (email != null)
            this.email = email;
        if (session != null)
            this.session = session;
        StaticVariables.contactsDataSource.updateDB(id,
                contactName, email, publicKey, session);
        StaticVariables.adapter.updateCont(a,this);
    }

    public static final int SENT = 0, RECEIVED = 1;

    public void update(int what,long time) {
        if (what == SENT)
            sent++;
        else if (what == RECEIVED){
            received++;
            this.last = time;
        }
        //todo the contact doesn't get updated in the list may cause small sync issue
        StaticVariables.contactsDataSource.updateDB(id, last, received, sent);
    }

    public void update(String defaultApp,Activity a) {
        if(defaultApp!=null){
            cn=new ComponentName(defaultApp.split("\n")[0],defaultApp.split("\n")[1]);
            StaticVariables.contactsDataSource.updateDB(id,defaultApp);
            StaticVariables.adapter.updateCont(a,this);
        }else{
            cn=null;
            StaticVariables.contactsDataSource.updateDB(id,"");
            StaticVariables.adapter.updateCont(a,this);
        }
    }
}
