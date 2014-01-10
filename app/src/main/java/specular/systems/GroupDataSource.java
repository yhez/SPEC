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
    //form main
    public static GroupDataSource groupDataSource;
    private final String[] allColumns = {GroupDB.COLUMN_ID,
            GroupDB.COLUMN_GROUP_NAME, GroupDB.COLUMN_ADDRESS,
            GroupDB.COLUMN_GROUP_ADDED_DATE, GroupDB.COLUMN_LAST_MSG,
            GroupDB.MSG_I_SEND, GroupDB.MSG_RECEIVED,
            GroupDB.COLUMN_PUBLIC_KEY, GroupDB.COLUMN_SESSION,
            GroupDB.COLUMN_DEFAULT_APP};
    private final GroupDB dbHelper;
    // Database fields
    private SQLiteDatabase database;

    public GroupDataSource(Activity a) {
        dbHelper = new GroupDB(a);
        database = dbHelper.getReadableDatabase();
        dbHelper.close();
    }

    public long createGroup(Activity a, Group group) {
        ContentValues values = new ContentValues();
        values.put(GroupDB.COLUMN_GROUP_NAME, group.getGroupName());
        values.put(GroupDB.COLUMN_ADDRESS, group.getEmail());
        values.put(GroupDB.COLUMN_PUBLIC_KEY, group.getPublicKey());
        values.put(GroupDB.COLUMN_SESSION, group.getMentor());
        values.put(GroupDB.COLUMN_DEFAULT_APP, "");

        database = dbHelper.getWritableDatabase();
        long l = database.insert(GroupDB.TABLE_GROUP, null,
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
        database.delete(GroupDB.TABLE_GROUP, GroupDB.COLUMN_ID
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
            cv.put(GroupDB.COLUMN_GROUP_NAME, groupName);
        if (email != null)
            cv.put(GroupDB.COLUMN_ADDRESS, email);
        if (publicKey != null)
            cv.put(GroupDB.COLUMN_PUBLIC_KEY, publicKey);
        if (session != null)
            cv.put(GroupDB.COLUMN_SESSION, session);
        database = dbHelper.getWritableDatabase();
        database.update(GroupDB.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, long last, int received) {
        ContentValues cv = new ContentValues();
        if (last > 0)
            cv.put(GroupDB.COLUMN_LAST_MSG, last);
        if (received > 0)
            cv.put(GroupDB.MSG_RECEIVED, received);
        database = dbHelper.getWritableDatabase();
        database.update(GroupDB.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }

    public void updateDB(long id, int sent) {
        ContentValues cv = new ContentValues();
        cv.put(GroupDB.MSG_I_SEND, sent);
        database = dbHelper.getWritableDatabase();
        database.update(GroupDB.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }


    public List<Group> getAllGroups() {
        database = dbHelper.getReadableDatabase();
        List<Group> groups = new ArrayList<Group>();
        Cursor cursor = database.query(GroupDB.TABLE_GROUP,
                allColumns, null, null, null, null, null);
        cursor.moveToFirst();
       /* while (!cursor.isAfterLast()) {
            Group group = new Group(cursor.getLong(0), cursor.getString(1)
                    , cursor.getString(2), cursor.getInt(3)
                    , cursor.getLong(4), cursor.getInt(5)
                    , cursor.getInt(6), cursor.getString(7),
                    cursor.getString(8), cursor.getString(9));
            groups.add(group);
            cursor.moveToNext();
        }*/
        // Make sure to close the cursor
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
        cv.put(GroupDB.COLUMN_DEFAULT_APP, defaultApp);
        database = dbHelper.getWritableDatabase();
        database.update(GroupDB.TABLE_GROUP, cv, "_id " + "=" + id, null);
        database.close();
    }
}