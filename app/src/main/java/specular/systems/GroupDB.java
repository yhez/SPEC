package specular.systems;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class GroupDB extends SQLiteOpenHelper {
    public static final String COLUMN_GROUP_ADDED_DATE = "date_added";
    public static final String COLUMN_LAST_MSG = "last_msg_date";
    public static final String MSG_I_SEND = "msgs_sent";
    public static final String MSG_RECEIVED = "msgs_received";
    public static final String COLUMN_GROUP_NAME = "contact";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PUBLIC_KEY = "public_key";
    public static final String COLUMN_SESSION = "session";
    public static final String COLUMN_DEFAULT_APP = "default_app";
    public static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_GROUP = "groups";


    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_GROUP + "( " + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_GROUP_NAME + " text not null, "
            + COLUMN_ADDRESS + " text not null, "
            + COLUMN_GROUP_ADDED_DATE + " integer not null, "
            + COLUMN_LAST_MSG + " integer not null, "
            + MSG_I_SEND + " integer not null, "
            + MSG_RECEIVED + " integer not null, "
            + COLUMN_PUBLIC_KEY + " text not null, "
            + COLUMN_SESSION + " text not null, "
            + COLUMN_DEFAULT_APP + " text not null);";


    public GroupDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
         * Log.w(MySQLiteHelper.class.getContactName(),
		 * "Upgrading database from version " + oldVersion + " to " + newVersion
		 * + ", which will destroy all old data");
		 */
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        onCreate(db);
    }
}
