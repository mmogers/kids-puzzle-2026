package lv.marmog.androidpuzzlegame.database;

import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_ID;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_12;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_4;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_9;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_USERNAME;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_USER_ID;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_TIMER;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_USERS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.mockito.InOrder;

// Plain JVM unit tests (no device/emulator, no Robolectric): they verify the SQL DatabaseHelper
// issues against a mocked SQLiteDatabase - table/column names and statement ordering - rather
// than actually executing it against a real SQLite engine. Real-engine behavior (does this SQL
// really create the schema we expect) would need an instrumented/androidTest, deliberately
// skipped for now since no device or emulator is set up in this environment.
public class DatabaseHelperTest {

    private DatabaseHelper newHelperWithoutRunningItsConstructor() {
        // DatabaseHelper's own constructor needs a real Context. Mockito.mock() instantiates
        // via Objenesis without calling it, and CALLS_REAL_METHODS then runs the actual
        // onCreate()/onUpgrade() bodies (including their private helper methods) against
        // whichever mocked SQLiteDatabase the test passes in.
        return mock(DatabaseHelper.class, CALLS_REAL_METHODS);
    }

    @Test
    public void onCreate_createsUsersTableBeforeTimerTable() {
        DatabaseHelper helper = newHelperWithoutRunningItsConstructor();
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        InOrder inOrder = inOrder(db);
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_TIMER));
        verify(db, times(2)).execSQL(anyString());
    }

    @Test
    public void onCreate_usersTableHasPrimaryKeyIdAndUniqueUsername() {
        DatabaseHelper helper = newHelperWithoutRunningItsConstructor();
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        verify(db).execSQL(contains(COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"));
        verify(db).execSQL(contains(COLUMN_USERNAME + " TEXT UNIQUE"));
    }

    @Test
    public void onCreate_timerTableHasAResultColumnPerLevelAndUserForeignKey() {
        DatabaseHelper helper = newHelperWithoutRunningItsConstructor();
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onCreate(db);

        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_4));
        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_9));
        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_12));
        verify(db).execSQL(contains(
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + " (" + COLUMN_ID + ")"));
    }

    @Test
    public void onUpgrade_dropsTimerBeforeUsers_thenRecreatesUsersBeforeTimer() {
        DatabaseHelper helper = newHelperWithoutRunningItsConstructor();
        SQLiteDatabase db = mock(SQLiteDatabase.class);

        helper.onUpgrade(db, 1, 2);

        InOrder inOrder = inOrder(db);
        inOrder.verify(db).execSQL(contains("DROP TABLE IF EXISTS " + TABLE_TIMER));
        inOrder.verify(db).execSQL(contains("DROP TABLE IF EXISTS " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_TIMER));
        verify(db, times(4)).execSQL(anyString());
    }
}
