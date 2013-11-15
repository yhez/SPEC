package specular.systems;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ContactsDataSource {
    Activity actvity;
    private final String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_CONTACT_NAME, MySQLiteHelper.COLUMN_EMAIL,
            MySQLiteHelper.COLUMN_PUBLIC_KEY, MySQLiteHelper.COLUMN_SESSION,
            MySQLiteHelper.COLUMN_FIRST};
    private final MySQLiteHelper dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public ContactsDataSource(Activity activity) {
        this.actvity =activity;
        dbHelper = new MySQLiteHelper(activity);
        database = dbHelper.getReadableDatabase();
        Main.adapter.lst = getAllContacts();
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
        Main.adapter.lst.add(contact);
        ListView lv=(ListView)((Activity) actvity).findViewById(R.id.list);
        lv.setAdapter(Main.adapter);
        return l;
    }

    public void deleteContact(Contact contact) {
        long id = contact.getId();
        database=dbHelper.getWritableDatabase();
        database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        dbHelper.close();
        Main.adapter.lst.remove(contact);
        ListView lv=(ListView)((Activity) actvity).findViewById(R.id.list);
        lv.setAdapter(Main.adapter);
    }

    public Contact findContact(long id) {
        database=dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null,
                null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0){
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
        database=dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_PUBLIC_KEY + " = '" + pbk
                + "' ", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0){
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1),
                    cursor.getString(2), cursor.getString(3),
                    cursor.getString(4), Integer.parseInt(cursor.getString(5)));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }

    private List<Contact> getAllContacts() {
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
