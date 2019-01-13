package com.example.snehil.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;


public class BookmarkDbHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "bookmark.db";
    public static final String TABLE_NAME = "bookmark";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FILENAME = "fileName";
    public static final String COLUMN_PATHNAME = "pathName";


    public BookmarkDbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FILENAME + " TEXT, " +
                COLUMN_PATHNAME + " TEXT" +
                ");";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Add a neew roe to database
    public void addBookmark(FileInitializer fileInitializer){
        ContentValues values = new ContentValues();
        values.put(COLUMN_FILENAME, fileInitializer.getFileName());
        values.put(COLUMN_PATHNAME, fileInitializer.getPath());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    //Delete a history from database
    public void deleteBookmark(String fileName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_FILENAME + "=\"" + fileName + "\";");
    }
    //clear History
    public void clearBookmark(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //print out database
    public ArrayList<FileInitializer> read(){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE 1";
        ArrayList<FileInitializer> fileList = new ArrayList<FileInitializer>();
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()) {
            do {
                int id = Integer.parseInt(c.getString(c.getColumnIndex("_id")));
                String fileName = c.getString(c.getColumnIndex("fileName"));
                String pathName = c.getString(c.getColumnIndex("pathName"));

                FileInitializer fileInitializer = new FileInitializer();
                fileInitializer.setId(id);
                fileInitializer.setFileName(fileName);
                fileInitializer.setPath(pathName);

                fileList.add(fileInitializer);
            } while (c.moveToNext());
        }


        db.close();
        return fileList;
    }
}
