package specular.systems;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class MySQLiteHelper extends SQLiteOpenHelper
{

    public static final String COLUMN_CONTACT_NAME = "contact";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_FIRST = "first_conv";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PUBLIC_KEY = "public_key";
    public static final String COLUMN_SESSION = "session";
    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_CONTACTS = "contacts";


    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
	+ TABLE_CONTACTS + "( " + COLUMN_ID
	+ " integer primary key autoincrement, " + COLUMN_CONTACT_NAME
	+ " text not null, " + COLUMN_EMAIL + " text not null, "
	+ COLUMN_PUBLIC_KEY + " text not null, " + COLUMN_SESSION
	+ " text not null, " + COLUMN_FIRST + " text not null);";


    public MySQLiteHelper(Context context)
	{
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
	{
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
        /*
		 * Log.w(MySQLiteHelper.class.getName(),
		 * "Upgrading database from version " + oldVersion + " to " + newVersion
		 * + ", which will destroy all old data");
		 */
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
}
