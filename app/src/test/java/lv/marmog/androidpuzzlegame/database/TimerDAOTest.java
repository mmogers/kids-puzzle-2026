package lv.marmog.androidpuzzlegame.database;

import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_TIMER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;

// ContentValues' actual contents can't be inspected here (its real put()/get() are no-ops
// under the unit-test Android stub jar), so these tests check what IS observable: which
// SQLiteDatabase call happens, what TimerDAO returns for it, and that cursors get closed -
// not which exact column a given level maps to internally.
public class TimerDAOTest {

    private TimerDAO newTimerDAO(SQLiteDatabase database) {
        DatabaseProvider dbProvider = mock(DatabaseProvider.class);
        when(dbProvider.getWritableDatabase()).thenReturn(database);
        return new TimerDAO(dbProvider);
    }

    @Test
    public void insertResult_returnsTrue_whenInsertSucceeds() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(1L);
        TimerDAO timerDAO = newTimerDAO(database);

        assertTrue(timerDAO.insertResult(7, 9, 120));
    }

    @Test
    public void insertResult_returnsFalse_whenInsertFails() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(-1L);
        TimerDAO timerDAO = newTimerDAO(database);

        assertFalse(timerDAO.insertResult(7, 9, 120));
    }

    @Test
    public void insertResult_acceptsEveryOfferedComplexityLevel() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(1L);
        TimerDAO timerDAO = newTimerDAO(database);

        assertTrue(timerDAO.insertResult(7, 4, 60));
        assertTrue(timerDAO.insertResult(7, 9, 90));
        assertTrue(timerDAO.insertResult(7, 12, 150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertResult_throws_forUnsupportedLevel() {
        TimerDAO timerDAO = newTimerDAO(mock(SQLiteDatabase.class));

        timerDAO.insertResult(7, 5, 120);
    }

    @Test
    public void getBestResult_returnsStoredTime_whenARowExists() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(42);
        TimerDAO timerDAO = newTimerDAO(database);

        assertEquals(42, timerDAO.getBestResult(7, 9));
        verify(cursor).close();
    }

    @Test
    public void getBestResult_returnsZero_whenNoRowExists() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(false);
        TimerDAO timerDAO = newTimerDAO(database);

        assertEquals(0, timerDAO.getBestResult(7, 9));
        verify(cursor).close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBestResult_throws_forUnsupportedLevel() {
        TimerDAO timerDAO = newTimerDAO(mock(SQLiteDatabase.class));

        timerDAO.getBestResult(7, 5);
    }
}
