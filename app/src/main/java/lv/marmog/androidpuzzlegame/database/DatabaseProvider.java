package lv.marmog.androidpuzzlegame.database;

import android.database.sqlite.SQLiteDatabase;

interface DatabaseProvider {
    SQLiteDatabase getWritableDatabase();
}
