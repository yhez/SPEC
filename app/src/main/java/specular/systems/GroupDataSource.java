package specular.systems;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupDataSource {
    public static GroupDataSource groupDataSource;
    private final String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_GROUP_NAME, MySQLiteHelper.COLUMN_ADDRESS,
            MySQLiteHelper.COLUMN_SESSION, MySQLiteHelper.COLUMN_PUBLIC_KEY,
            MySQLiteHelper.COLUMN_PRIVATE_KEY, MySQLiteHelper.FORCE_NFC,
            MySQLiteHelper.PRIVATE_GROUP, MySQLiteHelper.OWNER_NAME,MySQLiteHelper.OWNER_EMAIL,
            MySQLiteHelper.OWNER_PUBLIC, MySQLiteHelper.COLUMN_DEFAULT_APP};
    private final MySQLiteHelper dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public GroupDataSource(Activity a) {
        dbHelper = new MySQLiteHelper(a);
        database = dbHelper.getReadableDatabase();
        dbHelper.close();
    }

    public long createGroup(Activity a, Group group) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_GROUP_NAME, group.getGroupName());
        values.put(MySQLiteHelper.COLUMN_ADDRESS, group.getEmail());
        values.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, group.getPublicKey());
        values.put(MySQLiteHelper.COLUMN_SESSION, group.getMentor());
        values.put(MySQLiteHelper.COLUMN_DEFAULT_APP, "");
        values.put(MySQLiteHelper.OWNER_NAME,group.ownerName);
        values.put(MySQLiteHelper.OWNER_EMAIL,group.ownerEmail);
        values.put(MySQLiteHelper.OWNER_PUBLIC,group.ownerPublicKey);
        values.put(MySQLiteHelper.FORCE_NFC,group.noPrivateOnDevice);
        values.put(MySQLiteHelper.PRIVATE_GROUP, group.dontAllowNewMembers);

        database = dbHelper.getWritableDatabase();
        long l = database.insert(MySQLiteHelper.TABLE_GROUP, null,
                values);
        dbHelper.close();
        //todo update user list
        return l;
    }

    public void deleteGroup(Activity aa, Group group) {
        long id = group.getId();
        int position = -1;
        for (int a = 0; a < StaticVariables.fullList.size(); a++)
            if (group.getPublicKey().equals(StaticVariables.fullList.get(a).getPublicKey())) {
                position = a;
                break;
            }
        database = dbHelper.getWritableDatabase();
        database.delete(MySQLiteHelper.TABLE_GROUP, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
        dbHelper.close();
        if (!(position < 0)) {
            MySimpleArrayAdapter.removeCont(aa, position);
        }
    }

    public void updateDB(long id, String groupName, String email,
                         String publicKey, String session) {
        ContentValues cv = new ContentValues();
        if (groupName != null)
            cv.put(MySQLiteHelper.COLUMN_GROUP_NAME, groupName);
        if (email != null)
            cv.put(MySQLiteHelper.COLUMN_ADDRESS, email);
        if (publicKey != null)
            cv.put(MySQLiteHelper.COLUMN_PUBLIC_KEY, publicKey);
        if (session != null)
            cv.put(MySQLiteHelper.COLUMN_SESSION, session);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, long last, int received) {
        ContentValues cv = new ContentValues();
        if (last > 0)
            cv.put(MySQLiteHelper.COLUMN_LAST_MSG, last);
        if (received > 0)
            cv.put(MySQLiteHelper.MSG_RECEIVED, received);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, int sent) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.MSG_I_SEND, sent);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }


    public List<Group> getAllGroups() {
        database = dbHelper.getReadableDatabase();
        List<Group> groups = new ArrayList<Group>();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUP,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Group group = new Group(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getString(3)
                    , cursor.getString(4),cursor.getBlob(5)
                    , cursor.getInt(6)==0, cursor.getInt(7)==0,cursor.getString(8)
                    ,cursor.getString(9),cursor.getString(10),cursor.getString(11));
            groups.add(group);
            cursor.moveToNext();
        }
        cursor.close();
        dbHelper.close();
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group group, Group group2) {
                return (group.getGroupName().toLowerCase() + group.getEmail().toLowerCase())
                        .compareTo((group2.getGroupName().toLowerCase() + group2.getEmail().toLowerCase()));
            }
        });
        return groups;
    }

    public void updateDB(long id, String defaultApp) {
        ContentValues cv = new ContentValues();
        cv.put(MySQLiteHelper.COLUMN_DEFAULT_APP, defaultApp);
        database = dbHelper.getWritableDatabase();
        database.update(MySQLiteHelper.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }
    public Group findGroup(long id) {
        //trying find on lost before going to db
        if (StaticVariables.fullList != null)
            for (Group c : StaticVariables.fullListG)
                if (c.getId() == id)
                    return c;
        database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(MySQLiteHelper.TABLE_GROUP,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + id, null, null,
                null, null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Group c = new Group(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getString(3)
                    , cursor.getString(4),cursor.getBlob(5)
                    , cursor.getInt(6)==0, cursor.getInt(7)==0,cursor.getString(8)
                    ,cursor.getString(9),cursor.getString(10),cursor.getString(11));
            dbHelper.close();
            return c;
        }
        dbHelper.close();
        return null;
    }
}