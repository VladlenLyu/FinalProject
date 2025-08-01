package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "favorites.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "favorites";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                "link TEXT PRIMARY KEY, " +
                "title TEXT, " +
                "description TEXT, " +
                "pubDate TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addFavorite(Article article) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("link", article.link);
        cv.put("title", article.title);
        cv.put("description", article.description);
        cv.put("pubDate", article.pubDate);
        db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeFavorite(String link) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "link=?", new String[]{link});
        db.close();
    }

    public boolean isFavorite(String link) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"link"},
                "link=?", new String[]{link},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<Article> getAllFavorites() {
        List<Article> favorites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{"title", "description", "pubDate", "link"},
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                favorites.add(new Article(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return favorites;
    }
}
