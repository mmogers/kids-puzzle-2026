package lv.marmog.androidpuzzlegame.database;

import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_12;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_4;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_TIMER_RESULT_FOR_9;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_USER_ID;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_TIMER;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

// Owns all SQL for the TIMER table (best-time results per puzzle complexity level),
// should never build raw SQL against TABLE_TIMER themselves.
public class TimerDAO {
    private final SQLiteDatabase database;

    public TimerDAO(Context context) {
        this(new DatabaseHelper(context));
    }

    // test-only seam: lets tests inject a fake/mocked DatabaseProvider without a real Context
    TimerDAO(DatabaseProvider dbProvider) {
        this.database = dbProvider.getWritableDatabase();
    }

    // saves a played game's result for the given complexity level (4/9/12 pieces)
    public boolean insertResult(int userId, int level, int time) {
        String column = columnForLevel(level);

        ContentValues contentValues = new ContentValues();
        contentValues.put(column, time);
        contentValues.put(COLUMN_USER_ID, userId);
        long insertId = database.insert(TABLE_TIMER, null, contentValues);

        if (insertId == -1) {
            Log.e(TimerDAO.class.getSimpleName(), "Results are not saved");
            return false;
        }
        Log.i(TimerDAO.class.getSimpleName(),
                "Id: " + userId + " and timer result: " + time + " inserted into column " + level);
        return true;
    }

    // lowest (best) recorded time for this user at this complexity level, or 0 if none yet
    public int getBestResult(int userId, int level) {
        String column = columnForLevel(level);

        int result = 0;
        Cursor cursor = database.rawQuery(
                "SELECT " + column + " FROM " + TABLE_TIMER + " WHERE " + COLUMN_USER_ID + " = ? AND " + column
                        + " IS NOT NULL" + " ORDER BY " + column + " LIMIT 1", new String[] { String.valueOf(userId) });
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        return result;
    }

    private String columnForLevel(int level) {
        switch (level) {
        case 4:
            return COLUMN_TIMER_RESULT_FOR_4;
        case 9:
            return COLUMN_TIMER_RESULT_FOR_9;
        case 12:
            return COLUMN_TIMER_RESULT_FOR_12;
        default:
            throw new IllegalArgumentException("Unsupported puzzle complexity level: " + level);
        }
    }
}
