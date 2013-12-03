package specular.systems;

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

    // new contact from scratch
    public Contact(String contactName, String email,
                   String publicKey,String session) {
        if (publicKey != null) {
            this.contactName = contactName;
            this.publicKey = publicKey;
            this.email = email;
            this.session = new Session().toString()+(session!=null?"---"+session.replace("my","his"):"");
            this.sent=0;
            this.received=session!=null?1:0;
            this.sent=0;
            this.last=session!=null?System.currentTimeMillis():0;
            this.id = PublicStaticVariables.contactsDataSource.createContact(this);
        }
    }
    //create contact after pulling out from db
    public Contact(long id, String contactName, String email,int added,int last,int sent,int received, String publicKey,
                   String session) {
        this.id = id;
        this.contactName = contactName;
        this.publicKey = publicKey;
        this.email = email;
        this.session = session;
        this.added=added;
        this.last= last;
        this.received=received;
        this.sent=sent;
    }

    public static Bitmap getPhoto(String publicKey) {

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

    public int getAdded(){return added;}

    public long getLast(){return last;}

    public int getSent(){return sent;}

    public int getReceived(){return received;}
    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return contactName + "\n" + email + "\n" + publicKey.substring(0, 10)
                + "...." + publicKey.substring(publicKey.length() - 10) + "\n"
                + session;
    }

    public void update(int index, String contactName, String email,
                       String publicKey, String session) {
        if (contactName != null)
            this.contactName = contactName;
        if (publicKey != null)
            this.publicKey = publicKey;
        if (email != null)
            this.email = email;
        if (session != null)
            this.session = session;
        PublicStaticVariables.contactsDataSource.updateDB(id,
                contactName, email, publicKey, session);
        PublicStaticVariables.adapter.updateCont(this,index);
    }
    public static final int SENT=0,RECEIVED=1;
    public void update(int what){
        this.last=System.currentTimeMillis();
        if(what==SENT)
            sent++;
        else if(what==RECEIVED)
            received++;
        PublicStaticVariables.contactsDataSource.updateDB(id,last,received,sent);
    }
}
