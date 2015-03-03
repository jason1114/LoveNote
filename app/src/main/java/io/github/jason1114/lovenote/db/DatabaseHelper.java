package io.github.jason1114.lovenote.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.github.jason1114.lovenote.utils.GlobalContext;


/**
 * User: qii
 * Date: 12-7-30
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "weibo.db";

    private static final int DATABASE_VERSION = 37;

    static final String CREATE_ACCOUNT_TABLE_SQL = "create table " + AccountTable.TABLE_NAME
            + "("
            + AccountTable.UID + " integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_EXPIRES_TIME + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.NAVIGATION_POSITION + " integer,"
            + AccountTable.INFOJSON + " text"
            + ");";
    public static final String CREATE_DOWNLOAD_PICTURES_TABLE_SQL = "create table "
            + DownloadPicturesTable.TABLE_NAME
            + "("
            + DownloadPicturesTable.ID + " integer,"
            + DownloadPicturesTable.URL + " text primary key,"
            + DownloadPicturesTable.PATH + " text,"
            + DownloadPicturesTable.SIZE + " integer,"
            + DownloadPicturesTable.TIME + " integer,"
            + DownloadPicturesTable.TYPE + " integer"
            + ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);
        db.execSQL(CREATE_DOWNLOAD_PICTURES_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }



    private void deleteAllTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DownloadPicturesTable.TABLE_NAME);
    }
}
