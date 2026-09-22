package lv.marmog.androidpuzzlegame.database;

import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_TIMER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// ContentValues' put()/get() are no-ops under the unit-test stub jar, so these tests check
// what's observable instead: which SQLiteDatabase call happens, what TimerDAO returns, and
// that cursors get closed.
@ExtendWith(MockitoExtension.class)
 class TimerDAOTest {

    @Mock
    private SQLiteDatabase database;
    @Mock
    private Cursor cursor;
    @Mock
    private DatabaseProvider dbProvider;

    // built in setUp(), not @InjectMocks: the constructor stores dbProvider.getWritableDatabase(),
    // so it must run after that's stubbed
    private TimerDAO timerDAO;

    @BeforeEach
     void setUp() {
        when(dbProvider.getWritableDatabase()).thenReturn(database);
        timerDAO = new TimerDAO(dbProvider);
    }

    @Test
     void insertResult_returnsTrue_whenInsertSucceeds() {
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(1L);

        assertTrue(timerDAO.insertResult(7, 9, 120));
    }

    @Test
     void insertResult_returnsFalse_whenInsertFails() {
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(-1L);

        assertFalse(timerDAO.insertResult(7, 9, 120));
    }

    @Test
     void insertResult_acceptsEveryOfferedComplexityLevel() {
        when(database.insert(eq(TABLE_TIMER), isNull(), any(ContentValues.class))).thenReturn(1L);

        assertTrue(timerDAO.insertResult(7, 4, 60));
        assertTrue(timerDAO.insertResult(7, 9, 90));
        assertTrue(timerDAO.insertResult(7, 12, 150));
    }

    @Test
     void insertResult_throws_forUnsupportedLevel() {
        assertThrows(IllegalArgumentException.class, () -> timerDAO.insertResult(7, 5, 120));
    }

    @Test
     void getBestResult_returnsStoredTime_whenARowExists() {
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(42);

        assertEquals(42, timerDAO.getBestResult(7, 9));
        verify(cursor).close();
    }

    @Test
     void getBestResult_returnsZero_whenNoRowExists() {
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(false);

        assertEquals(0, timerDAO.getBestResult(7, 9));
        verify(cursor).close();
    }

    @Test
     void getBestResult_throws_forUnsupportedLevel() {
        assertThrows(IllegalArgumentException.class, () -> timerDAO.getBestResult(7, 5));
    }
}
