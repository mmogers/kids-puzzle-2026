package lv.marmog.androidpuzzlegame.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import lv.marmog.androidpuzzlegame.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class UserDAOTest {

    @Mock
    private SQLiteDatabase database;
    @Mock
    private Cursor cursor;
    @Mock
    private DatabaseProvider dbProvider;

    // built in setUp(), not @InjectMocks: the constructor stores dbProvider.getWritableDatabase(),
    // so it must run after that's stubbed
    private UserDAO userDAO;

    @BeforeEach
     void setUp() {
        when(dbProvider.getWritableDatabase()).thenReturn(database);
        userDAO = new UserDAO(dbProvider);
    }

    @Test
     void testCreateUser_returnsTrue_whenInsertSucceeds() {
        when(database.insert(eq(DatabaseHelper.TABLE_USERS), isNull(), any(ContentValues.class)))
                .thenReturn(1L);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToLast()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(1);
        when(cursor.getString(1)).thenReturn("alice");

        User user = new User();
        user.setUsername("alice");

        assertTrue(userDAO.createUser(user));
    }

    @Test
     void testCreateUser_returnsFalse_whenInsertFails() {
        when(database.insert(eq(DatabaseHelper.TABLE_USERS), isNull(), any(ContentValues.class)))
                .thenReturn(-1L);
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);

        User user = new User();
        user.setUsername("alice");

        assertFalse(userDAO.createUser(user));
    }

    @Test
     void testCheckUsername_returnsTrue_whenUserExists() {
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(1);

        assertTrue(userDAO.checkUsername("alice"));
        verify(cursor).close();
    }

    @Test
     void testCheckUsername_returnsFalse_whenUserDoesNotExist() {
        when(database.rawQuery(anyString(), any(String[].class))).thenReturn(cursor);
        when(cursor.getCount()).thenReturn(0);

        assertFalse(userDAO.checkUsername("nobody"));
    }

    @Test
     void testDeleteUser_returnsTrue_whenARowWasDeleted() {
        when(database.delete(eq(DatabaseHelper.TABLE_USERS), anyString(), any(String[].class)))
                .thenReturn(1);

        User user = new User();
        user.setUsernameId(3);

        assertTrue(userDAO.deleteUser(user));
    }

    @Test
     void testDeleteUser_returnsFalse_whenNoRowWasDeleted() {
        when(database.delete(eq(DatabaseHelper.TABLE_USERS), anyString(), any(String[].class)))
                .thenReturn(0);

        User user = new User();
        user.setUsernameId(3);

        assertFalse(userDAO.deleteUser(user));
    }

    @Test
     void testDeleteResults_returnsTrue_whenARowWasDeleted() {
        when(database.delete(eq(DatabaseHelper.TABLE_TIMER), anyString(), any(String[].class)))
                .thenReturn(2);

        User user = new User();
        user.setUsernameId(3);

        assertTrue(userDAO.deleteResults(user));
    }

    @Test
     void testGetAllUsers_mapsEveryCursorRowToAUser() {
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        // two rows, then the cursor is exhausted
        when(cursor.isAfterLast()).thenReturn(false, false, true);
        when(cursor.getInt(0)).thenReturn(1, 2);
        when(cursor.getString(1)).thenReturn("alice", "bob");

        List<User> users = userDAO.getAllUsers();

        assertEquals(2, users.size());
        assertEquals("alice", users.get(0).getUsername());
        assertEquals("bob", users.get(1).getUsername());
        verify(cursor).close();
    }

    @Test
     void testGetUserById_returnsUser_whenFound() {
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(true);
        when(cursor.getInt(0)).thenReturn(5);
        when(cursor.getString(1)).thenReturn("carol");

        User user = userDAO.getUserById(5);

        assertEquals(5, user.getUsernameId());
        assertEquals("carol", user.getUsername());
    }

    @Test
     void testGetUserById_returnsNull_whenNotFound() {
        when(database.query(eq(DatabaseHelper.TABLE_USERS), any(String[].class), anyString(),
                any(String[].class), isNull(), isNull(), isNull()))
                .thenReturn(cursor);
        when(cursor.moveToFirst()).thenReturn(false);

        assertNull(userDAO.getUserById(999));
    }
}
