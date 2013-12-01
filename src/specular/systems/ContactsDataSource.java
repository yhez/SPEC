package specular.systems;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ContactsDataSource {
    final Main m;
    private final String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_CONTACT_NAME, MySQLiteHelper.COLUMN_EMAIL,
            MySQLiteHelper.COLUMN_PUBLIC_KEY, MySQLiteHelper.COLUMN_SESSION,
            MySQLiteHelper.COLUMN_FIRST};
    private final MySQLiteHelper dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public ContactsDataSource(Main m) {
        this.m = m;
        dbHelper = new MySQLiteHelper(m);
        database = dbHelper.getReadableDatabase();
        dbHelper.close();
    }

    public long createContact(Contact contact) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, contact.getContactName());
        values.put(MySQLiteHelper.COLUMN_EMAIL, contact.getEmail());
        values.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, contact.getPublicKey());
        values.put(MySQLiteHelper.COLUMN_SESSION, contact.getSession());
        values.put(MySQLiteHelper.COLUMN_FIRST,
                "" + contact.getConversationStatus());
        database = dbHelper.getWritableDatabase();
        long l = database.insert(MySQLiteHelper.TABLE_CONTACTS, null,
                values);
        dbHelper.close();
        PublicStaticVariables.adapter.addCont(contact);
        return l;
    }

    public void deleteContact(Contact contact) {
        long id = contact.getId();
        int position = -1;
        for (int a = 0; a < PublicStaticVariables.fullList.size(); a++)
            if (contact.getPublicKey().equals(PublicStaticVariables.fullList.get(a).getPublicKey())) {
                position = a;
                break;
            }
        database = dbHelper.getWritableDatabase();
        database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        dbHelper.close();
        if (!(position < 0)) {
            PublicStaticVariables.adapter.removeCont(position);
        }
    }

    public void updateDB(long id, String contactName, String email,
                         String publicKey, String session, int conversationStatus) {
        ContentValues cv = new ContentValues();
        if (contactName != null)
            cv.put(MySQLiteHelper.COLUMN_CONTACT_NAME, contactName);
        if (email != null)
            cv.put(MySQLiteHelper.COLUMN_EMAIL, email);
        if (publicKey != null)
            cv.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, publicKey);
        if (session != null)
            cv.put(MySQLiteHelper.COLUMN_SESSION, session);
        //flag -1 not changed
        if (conversationStatus > 0)
            cv.put(MySQLiteHelper.COLUMN_FIRST, conversationStatus);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_CONTACTS, cv, "_id " + "=" + id, null);
        database.close();
    }

    public Contact findContact(long id) {
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null,
                null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), Integer.parseInt(cursor.getString(5)));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }

    public Contact findContact(String pbk) {
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_PUBLIC_KEY + " = '" + pbk
                + "' ", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), Integer.parseInt(cursor.getString(5)));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }

    public List<Contact> getAllContacts() {
        database = dbHelper.getReadableDatabase();
        List<Contact> contacts = new ArrayList<Contact>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact = new Contact(cursor.getLong(0),
                    cursor.getString(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4),
                    Integer.parseInt(cursor.getString(5)));
            contacts.add(contact);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        dbHelper.close();
        return contacts;
    }

	/*
     * private Contact cursorToContact(Cursor cursor) { Contact contact = new
	 * Contact(); contact.setId(cursor.getLong(0));
	 * contact.setContactName(cursor.getString(1));
	 * contact.setEmail(cursor.getString(2));
	 * contact.setPublicKey(cursor.getString(3));
	 * contact.setSession(cursor.getString(4)); return contact; }
	 */
}
