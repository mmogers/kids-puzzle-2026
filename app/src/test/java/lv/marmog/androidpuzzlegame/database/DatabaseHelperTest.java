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
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Verifies the SQL against a mocked SQLiteDatabase (no real SQLite engine, no emulator).
@ExtendWith(MockitoExtension.class)
 class DatabaseHelperTest {

    // CALLS_REAL_METHODS skips the real constructor (needs a real Context) while still running
    // the actual onCreate()/onUpgrade() bodies
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DatabaseHelper helper;
    @Mock
    private SQLiteDatabase db;

    @Test
     void onCreate_createsUsersTableBeforeTimerTable() {
        helper.onCreate(db);

        InOrder inOrder = inOrder(db);
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_TIMER));
        verify(db, times(2)).execSQL(anyString());
    }

    @Test
     void onCreate_usersTableHasPrimaryKeyIdAndUniqueUsername() {
        helper.onCreate(db);

        verify(db).execSQL(contains(COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"));
        verify(db).execSQL(contains(COLUMN_USERNAME + " TEXT UNIQUE"));
    }

    @Test
     void onCreate_timerTableHasAResultColumnPerLevelAndUserForeignKey() {
        helper.onCreate(db);

        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_4));
        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_9));
        verify(db).execSQL(contains(COLUMN_TIMER_RESULT_FOR_12));
        verify(db).execSQL(contains(
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + " (" + COLUMN_ID + ")"));
    }

    @Test
     void onUpgrade_dropsTimerBeforeUsers_thenRecreatesUsersBeforeTimer() {
        helper.onUpgrade(db, 1, 2);

        InOrder inOrder = inOrder(db);
        inOrder.verify(db).execSQL(contains("DROP TABLE IF EXISTS " + TABLE_TIMER));
        inOrder.verify(db).execSQL(contains("DROP TABLE IF EXISTS " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_USERS));
        inOrder.verify(db).execSQL(contains("CREATE TABLE " + TABLE_TIMER));
        verify(db, times(4)).execSQL(anyString());
    }
}
