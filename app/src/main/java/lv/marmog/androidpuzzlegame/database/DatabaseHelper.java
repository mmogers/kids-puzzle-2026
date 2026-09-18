package lv.marmog.androidpuzzlegame.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Schema is intentionally not public: nothing outside this package should build raw SQL
// against these tables directly. Go through UserDAO / TimerDAO instead.
public class DatabaseHelper extends SQLiteOpenHelper implements DatabaseProvider {
    static final String DATABASE_NAME = "users.db";
    static final int DATABASE_VERSION = 2;
    // Users table
    static final String TABLE_USERS = "users";
    static final String COLUMN_ID = "id";
    static final String COLUMN_USERNAME = "username";
    // Timer table
    static final String TABLE_TIMER = "timer";
    static final String COLUMN_TIMER_RESULT_FOR_4 = "timer_result_for_4";
    static final String COLUMN_TIMER_RESULT_FOR_9 = "timer_result_for_9";
    static final String COLUMN_TIMER_RESULT_FOR_12 = "timer_result_for_12";
    static final String COLUMN_USER_ID = "user_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createTimerTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTimerTable(db);
        dropUsersTable(db);

        onCreate(db);

        Log.w(DatabaseHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + " , which will destroy all old data");
    }

    private void createUsersTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_USERS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE" + ")";

        db.execSQL(sql);
    }

    private void createTimerTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_TIMER + " (" + COLUMN_TIMER_RESULT_FOR_4 + " INTEGER, "
                + COLUMN_TIMER_RESULT_FOR_9 + " INTEGER, " + COLUMN_TIMER_RESULT_FOR_12 + " INTEGER, " + COLUMN_USER_ID
                + " INTEGER, " + "FOREIGN KEY (" + COLUMN_USER_ID + ") " + "REFERENCES " + TABLE_USERS + " ("
                + COLUMN_ID + ")" + ")";

        db.execSQL(sql);
    }

    private void dropUsersTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
    }

    private void dropTimerTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TIMER);
    }
}
