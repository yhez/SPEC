package specular.systems;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactsDataSource {
    //form main
    public static ContactsDataSource contactsDataSource;
    private final String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_CONTACT_NAME, MySQLiteHelper.COLUMN_EMAIL,
            MySQLiteHelper.COLUMN_CONTACT_ADDED_DATE, MySQLiteHelper.COLUMN_LAST_MSG,
            MySQLiteHelper.MSG_I_SEND, MySQLiteHelper.MSG_RECEIVED,
            MySQLiteHelper.COLUMN_PUBLIC_KEY, MySQLiteHelper.COLUMN_SESSION,
            MySQLiteHelper.COLUMN_DEFAULT_APP};
    private final MySQLiteHelper dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public ContactsDataSource(Activity a) {
        dbHelper = new MySQLiteHelper(a);
        database = dbHelper.getReadableDatabase();
        dbHelper.close();
    }

    public long createContact(Activity a, Contact contact) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_CONTACT_NAME, contact.getContactName());
        values.put(MySQLiteHelper.COLUMN_EMAIL, contact.getEmail());
        values.put(MySQLiteHelper.COLUMN_CONTACT_ADDED_DATE, contact.getAdded());
        values.put(MySQLiteHelper.COLUMN_LAST_MSG, contact.getLast());
        values.put(MySQLiteHelper.MSG_I_SEND, contact.getSent());
        values.put(MySQLiteHelper.MSG_RECEIVED, contact.getReceived());
        values.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, contact.getPublicKey());
        values.put(MySQLiteHelper.COLUMN_SESSION, contact.getSession());
        values.put(MySQLiteHelper.COLUMN_DEFAULT_APP, "");

        database = dbHelper.getWritableDatabase();
        long l = database.insert(MySQLiteHelper.TABLE_CONTACTS, null,
                values);
        dbHelper.close();
        MySimpleArrayAdapter.addCont(a, contact);
        return l;
    }

    public void deleteContact(Activity aa, Contact contact) {
        long id = contact.getId();
        int position = -1;
        for (int a = 0; a < StaticVariables.fullList.size(); a++)
            if (contact.getPublicKey().equals(StaticVariables.fullList.get(a).getPublicKey())) {
                position = a;
                break;
            }
        database = dbHelper.getWritableDatabase();
        database.delete(MySQLiteHelper.TABLE_CONTACTS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        dbHelper.close();
        if (!(position < 0)) {
            MySimpleArrayAdapter.removeCont(aa, position);
        }
    }

    public void updateDB(long id, String contactName, String email,
                         String publicKey, String session) {
        ContentValues cv = new ContentValues();
        if (contactName != null)
            cv.put(MySQLiteHelper.COLUMN_CONTACT_NAME, contactName);
        if (email != null)
            cv.put(MySQLiteHelper.COLUMN_EMAIL, email);
        if (publicKey != null)
            cv.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, publicKey);
        if (session != null)
            cv.put(MySQLiteHelper.COLUMN_SESSION, session);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_CONTACTS, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, long last, int received) {
        ContentValues cv = new ContentValues();
        if (last > 0)
            cv.put(MySQLiteHelper.COLUMN_LAST_MSG, last);
        if (received > 0)
            cv.put(MySQLiteHelper.MSG_RECEIVED, received);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_CONTACTS, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, int sent) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.MSG_I_SEND, sent);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_CONTACTS, cv, "_id " + "=" + id, null);
        database.close();
    }

    public Contact findContact(long id) {
        //trying find on lost before going to db
        if (StaticVariables.fullList != null)
            for (Contact c : StaticVariables.fullList)
                if (c.getId() == id)
                    return c;
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null,
                null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getInt(3)
                    , cursor.getLong(4), cursor.getInt(5)
                    , cursor.getInt(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }

    public Contact findContactByEmail(String email) {
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_EMAIL + " = '" + email
                + "' ", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getInt(3)
                    , cursor.getLong(4), cursor.getInt(5)
                    , cursor.getInt(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }

    public Contact findContactByKey(String pbk) {
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONTACTS,
                allColumns, MySQLiteHelper.COLUMN_PUBLIC_KEY + " = '" + pbk
                + "' ", null, null, null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Contact c = new Contact(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getInt(3)
                    , cursor.getLong(4), cursor.getInt(5)
                    , cursor.getInt(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9));
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
            Contact contact = new Contact(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getInt(3)
                    , cursor.getLong(4), cursor.getInt(5)
                    , cursor.getInt(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9));
            contacts.add(contact);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        dbHelper.close();
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return (contact.getContactName().toLowerCase() + contact.getEmail().toLowerCase())
                        .compareTo((contact2.getContactName().toLowerCase() + contact2.getEmail().toLowerCase()));
            }
        });
        return contacts;
    }

    public void updateDB(long id, String defaultApp) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN_DEFAULT_APP, defaultApp);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_CONTACTS, cv, "_id " + "=" + id, null);
        database.close();
    }
}
