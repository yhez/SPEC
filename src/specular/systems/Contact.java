package specular.systems;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class Contact {

    private String contactName;
    private int conversationStatus;
    private String email;
    private long id;
    private String publicKey;
    private String session;

    // new contact from scratch
    public Contact(String contactName, String email,
                   String publicKey,String session) {
        if (publicKey != null) {
            this.contactName = contactName;
            this.publicKey = publicKey;
            this.email = email;
            this.session = new Session().toString()+(session!=null?"---"+session.replace("my","his"):"");
            this.conversationStatus = PublicStaticVariables.WE_STRANGERS;
            this.id = PublicStaticVariables.contactsDataSource.createContact(this);
        }
    }
    //create contact after pulling out from db
    public Contact(long id, String contactName, String email, String publicKey,
                   String session, int conversationStatus) {
        this.conversationStatus = conversationStatus;
        this.id = id;
        this.contactName = contactName;
        this.publicKey = publicKey;
        this.email = email;
        this.session = session;
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

    public int getConversationStatus() {
        return conversationStatus;
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

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return contactName + "\n" + email + "\n" + publicKey.substring(0, 10)
                + "...." + publicKey.substring(publicKey.length() - 10) + "\n"
                + session + "\nConversation status: " + conversationStatus;
    }

    public void update(int index, String contactName, String email,
                       String publicKey, String session, int conversationStatus) {
        if (contactName != null)
            this.contactName = contactName;
        if (publicKey != null)
            this.publicKey = publicKey;
        if (email != null)
            this.email = email;
        if (session != null)
            this.session = session;
        //flag -1 not changed
        if (!(conversationStatus < 0))
            this.conversationStatus = conversationStatus;
        PublicStaticVariables.contactsDataSource.updateDB(id,
                contactName, email, publicKey, session, conversationStatus);
        PublicStaticVariables.adapter.updateCont(this,index);
    }
}
