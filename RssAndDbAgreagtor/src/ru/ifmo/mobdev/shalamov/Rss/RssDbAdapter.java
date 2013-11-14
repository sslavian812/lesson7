package ru.ifmo.mobdev.shalamov.Rss;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 30.10.13
 * Time: 17:14
 * To change this template use File | Settings | File Templates.
 */
public class RssDbAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LINK = "link";
    //public static final String KEY_ENCD = "encoding";

    public static final String KEY_FEEDID = "feed_id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DATE = "date";
    public static final String KEY_DESC = "description";

    private static final int DEFAULT_ID = 1000;

    private static final String DATABASE_NAME = "MyRssDatabase";

    private static final String TABLE_FEEDS = "feeds";
    private static final String TABLE_CONTENT = "content";

    private static final int DATABASE_VERSION = 2;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;


    private static final String CREATE_TABLE_FEEDS = "create table " + TABLE_FEEDS + " ( " +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_NAME + " text not null, " +
            KEY_LINK + " text not null unique " +         // it's not necessary to have same feeds twice!
            //KEY_ENCD + " text not null" +
            ");";


    private static final String CREATE_TABLE_CONTENT = "create table " + TABLE_CONTENT + " ( " +
            KEY_ROWID + " integer primary key autoincrement, " +
            KEY_FEEDID + " integer not null, " +
            KEY_TITLE + " text not null," +
            KEY_DATE + " text not null, " +
            KEY_DESC + " text not null, " +
            KEY_LINK + " text not null unique);";    // links should be unique, i suppose


    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_FEEDS);
            db.execSQL(CREATE_TABLE_CONTENT);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);
            onCreate(db);
        }
    }

    public RssDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public RssDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        try {
            mDb = mDbHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            try {
                mDb = mDbHelper.getReadableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                return this;
            }
        }
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    public void dropFeeds() {
        try {
            mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDS);
            mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FEEDS + " ( " +
                    KEY_ROWID + " integer primary key autoincrement, " +
                    KEY_NAME + " text not null," +
                    KEY_LINK + " text not null unique " +

                    ");");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dropContent() {
        try {
            mDb.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTENT);
            mDb.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CONTENT + " ( " +
                    KEY_ROWID + " integer primary key autoincrement, " +
                    KEY_FEEDID + " integer not null, " +
                    KEY_TITLE + " text not null," +
                    KEY_DATE + " text not null, " +
                    KEY_DESC + " text not null, " +
                    KEY_LINK + " link text not null unique);");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean createFeed(String name, String link) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_LINK, link);

        try {
            return mDb.insert(TABLE_FEEDS, null, initialValues) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean editFeed(int rowID, String name, String link) {
        ContentValues initialValues = new ContentValues();

        if (rowID > 0) initialValues.put(KEY_ROWID, rowID);
        if (name != null && name != "") initialValues.put(KEY_NAME, name);
        if (name != null && link != "") initialValues.put(KEY_LINK, link);
        try {
            return mDb.update(TABLE_FEEDS, initialValues, KEY_ROWID + " =?", new String[]{((Integer) rowID).toString()}) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFeed(long rowId) {
        return mDb.delete(TABLE_FEEDS, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean deleteContent(int feed_id ) {
        return mDb.delete(TABLE_CONTENT, KEY_FEEDID + "=" + feed_id, null) > 0;
    }


    public Cursor fetchAllFeedsNames() {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_FEEDS, new String[]{KEY_ROWID, KEY_NAME}, null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return cursor;
        }
    }

    public Cursor fetchAllFeedsNamesAndLinks() {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_FEEDS, new String[]{KEY_ROWID, KEY_NAME, KEY_LINK}, null, null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return cursor;
        }
    }

    public Integer fetchFeedId(String s) {
        Cursor cursor = null;
        Integer res = null;
        try {
            if (s == null)
                cursor = mDb.query(TABLE_FEEDS, new String[]{KEY_ROWID}, null, null, null, null, null);
            else
                cursor = mDb.query(TABLE_FEEDS, new String[]{KEY_ROWID}, KEY_NAME + " =?", new String[]{s}, null, null, null, null);

            cursor.moveToNext();
            res = cursor.getInt(cursor.getColumnIndex(KEY_ROWID));
            cursor.close();

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return DEFAULT_ID;
        } finally {
            return res;
        }
    }

    public boolean createContent(FeedItem fi, String feed) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, fi.getTitle());
        initialValues.put(KEY_DATE, fi.getDate());
        initialValues.put(KEY_DESC, fi.getDesc());
        initialValues.put(KEY_LINK, fi.getLink());

        Integer feed_id = fetchFeedId(feed);
        initialValues.put(KEY_FEEDID, feed_id);

        try {
            return mDb.insert(TABLE_CONTENT, null, initialValues) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cursor fetchArticles(Integer feed_id) {
        Cursor cursor = null;
        try {
            if (feed_id == null) {
                cursor = mDb.query(TABLE_CONTENT, new String[]{KEY_ROWID, KEY_TITLE, KEY_DATE}, null, null, null, null, null);
            } else {
                cursor = mDb.query(TABLE_CONTENT, new String[]{KEY_ROWID, KEY_TITLE, KEY_DATE}, KEY_FEEDID + " = " + feed_id, null, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return cursor;
        }
    }

    public Cursor fetchContent(Integer content_id) throws SQLException {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_CONTENT, new String[]{KEY_ROWID, KEY_TITLE, KEY_DATE, KEY_DESC, KEY_LINK}, KEY_ROWID + "=" + content_id.toString(), null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return cursor;
        }
    }

    public Cursor fetchFeed(Integer cur_id) {
        Cursor cursor = null;
        try {
            cursor = mDb.query(TABLE_FEEDS, new String[]{KEY_ROWID, KEY_NAME, KEY_LINK}, KEY_ROWID + "=" + cur_id.toString(), null, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return cursor;
        }
    }
}
