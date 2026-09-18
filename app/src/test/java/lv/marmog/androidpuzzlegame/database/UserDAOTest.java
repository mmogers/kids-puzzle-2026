package lv.marmog.androidpuzzlegame.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import junit.framework.TestCase;

import java.util.List;

import lv.marmog.androidpuzzlegame.model.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserDAOTest extends TestCase {

    private UserDAO newUserDAO(SQLiteDatabase database) {
        DatabaseProvider dbProvider = mock(DatabaseProvider.class);
        when(dbProvider.getWritableDatabase()).thenReturn(database);
        return new UserDAO(dbProvider);
    }

    public void testCreateUser_returnsTrue_whenInsertSucceeds() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.insert(eq(DatabaseHelper.TABLE_USERS), isNull(), any(ContentValues.class)))
                .thenReturn(1L);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToLast()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(1);
        when(cursor.getString(1)).thenReturn("alice");

        UserDAO userDAO = newUserDAO(database);
        User user = new User();
        user.setUsername("alice");

        assertTrue(userDAO.createUser(user));
    }

    public void testCreateUser_returnsFalse_whenInsertFails() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.insert(eq(DatabaseHelper.TABLE_USERS), isNull(), any(ContentValues.class)))
                .thenReturn(-1L);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);

        UserDAO userDAO = newUserDAO(database);
        User user = new User();
        user.setUsername("alice");

        assertFalse(userDAO.createUser(user));
    }

    public void testCheckUsername_returnsTrue_whenUserExists() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(1);

        UserDAO userDAO = newUserDAO(database);

        assertTrue(userDAO.checkUsername("alice"));
        verify(cursor).close();
    }

    public void testCheckUsername_returnsFalse_whenUserDoesNotExist() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(0);

        UserDAO userDAO = newUserDAO(database);

        assertFalse(userDAO.checkUsername("nobody"));
    }

    public void testDeleteUser_returnsTrue_whenARowWasDeleted() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.delete(eq(DatabaseHelper.TABLE_USERS), anyString(), any(String[].class)))
                .thenReturn(1);

        User user = new User();
        user.setUsernameId(3);

        assertTrue(newUserDAO(database).deleteUser(user));
    }

    public void testDeleteUser_returnsFalse_whenNoRowWasDeleted() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.delete(eq(DatabaseHelper.TABLE_USERS), anyString(), any(String[].class)))
                .thenReturn(0);

        User user = new User();
        user.setUsernameId(3);

        assertFalse(newUserDAO(database).deleteUser(user));
    }

    public void testDeleteResults_returnsTrue_whenARowWasDeleted() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        when(database.delete(eq(DatabaseHelper.TABLE_TIMER), anyString(), any(String[].class)))
                .thenReturn(2);

        User user = new User();
        user.setUsernameId(3);

        assertTrue(newUserDAO(database).deleteResults(user));
    }

    public void testGetAllUsers_mapsEveryCursorRowToAUser() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        // two rows, then the cursor is exhausted
        when(cursor.isAfterLast()).thenReturn(false, false, true);
        when(cursor.getInt(0)).thenReturn(1, 2);
        when(cursor.getString(1)).thenReturn("alice", "bob");

        List<User> users = newUserDAO(database).getAllUsers();

        assertEquals(2, users.size());
        assertEquals("alice", users.get(0).getUsername());
        assertEquals("bob", users.get(1).getUsername());
        verify(cursor).close();
    }

    public void testGetUserById_returnsUser_whenFound() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(5);
        when(cursor.getString(1)).thenReturn("carol");

        User user = newUserDAO(database).getUserById(5);

        assertEquals(5, user.getUsernameId());
        assertEquals("carol", user.getUsername());
    }

    public void testGetUserById_returnsNull_whenNotFound() {
        SQLiteDatabase database = mock(SQLiteDatabase.class);
        Cursor cursor = mock(Cursor.class);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(false);

        assertNull(newUserDAO(database).getUserById(999));
    }
}
