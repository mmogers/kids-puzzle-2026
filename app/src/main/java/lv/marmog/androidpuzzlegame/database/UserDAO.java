package lv.marmog.androidpuzzlegame.database;

import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_ID;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.COLUMN_USER_ID;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_TIMER;
import static lv.marmog.androidpuzzlegame.database.DatabaseHelper.TABLE_USERS;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import lv.marmog.androidpuzzlegame.model.User;

public class UserDAO {
    private final SQLiteDatabase database;
    private final String[] allColumns = { DatabaseHelper.COLUMN_ID, DatabaseHelper.COLUMN_USERNAME };

    public UserDAO(Context context) {
        this(new DatabaseHelper(context));
    }

    // test-only seam: lets tests inject a fake/mocked DatabaseProvider without a real Context
    UserDAO(DatabaseProvider dbProvider) {
        this.database = dbProvider.getWritableDatabase();
    }

    public boolean createUser(User username) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.COLUMN_USERNAME, username.getUsername());
        long insertID = database.insert(TABLE_USERS, null, contentValues);
        Cursor cursor = database.query(TABLE_USERS, allColumns,
                DatabaseHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(insertID) }, null, null, null);
        cursor.moveToLast();
        User newUser = cursorToUser(cursor);
        cursor.close();

        if (insertID == -1) {
            Log.e(UserDAO.class.getSimpleName(), "New user was NOT created");
            return false;
        }
        else {
            Log.i(UserDAO.class.getSimpleName(), "New user was created");
            return true;
        }
    }

    public boolean checkUsername(String username) {
        Cursor cursor =
                database.rawQuery("Select * from " + TABLE_USERS + " where username = ?", new String[] { username });
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean deleteUser(User user) {
        int rows =
                database.delete(TABLE_USERS, COLUMN_ID + " = ?", new String[] { String.valueOf(user.getUsernameId()) });
        return rows > 0;
    }

    // --- need to delete from timer database
    public boolean deleteResults(User user) {
        int rows = database.delete(TABLE_TIMER,
                COLUMN_USER_ID + " = ?", new String[] { String.valueOf(user.getUsernameId()) });
        return rows > 0;
    }

    //Method that show us all the users we have in database
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<User>(0);

        //get data from the db
        Cursor cursor = database.query(TABLE_USERS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        //loop through the cursor(result set) and create new usernames objects
        while (!cursor.isAfterLast()) {

            User user = cursorToUser(cursor);
            userList.add(user);
            cursor.moveToNext();
        }
        cursor.close();
        return userList;
    }

    private User cursorToUser(Cursor cursor) {
        int id = cursor.getInt(0);
        String username = cursor.getString(1);

        User u = new User();
        u.setUsernameId(id);
        u.setUsername(username);
        return u;

    }

    //method that gives us user by id
    public User getUserById(int id) {

        Cursor cursor = database.query(TABLE_USERS, allColumns,
                DatabaseHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(id) }, null, null, null);
        User user = cursor.moveToFirst() ? cursorToUser(cursor) : null;
        cursor.close();
        return user;
    }
}



