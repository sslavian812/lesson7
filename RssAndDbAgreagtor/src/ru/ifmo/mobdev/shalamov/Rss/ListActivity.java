package ru.ifmo.mobdev.shalamov.Rss;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 30.10.13
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class ListActivity extends Activity {

    MyBroadcastReceiver myBroadcastReceiver = null;

    ListView lv = null;
    Button btn = null;


    public static final String KEY_TITLE = "title";
    public static final String KEY_DESC = "description";
    public static final String KEY_LINK = "link";

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DROP_ID = Menu.FIRST + 1;
    private static final int SHOWBROWSER_ID = Menu.FIRST + 2;


    private SimpleCursorAdapter simpleCursorAdapter = null;
    RssDbAdapter mDbHelper = null;

    Integer currentFeedId = null;

    private PendingIntent pendingIntent;
    AlarmManager alarmManager = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);
        mDbHelper = new RssDbAdapter(this);
        lv = (ListView) findViewById(R.id.lvContent);

        registerForContextMenu(lv);

        final Intent intent = getIntent();

        String query = intent.getStringExtra(FeedActivity.KEY_QUERY);

        if (FeedActivity.KEY_ALL.equals(query))
            currentFeedId = null;
        else {
            mDbHelper.open();
            currentFeedId = mDbHelper.fetchFeedId(query);
            mDbHelper.close();
        }

        showContent();

        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent update = new Intent(ListActivity.this, UpdateDBService.class);
                startService(update);
            }
        });

        myBroadcastReceiver = new MyBroadcastReceiver();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ListActivity.this, ShowActivity.class);
                Cursor cursor = (Cursor) simpleCursorAdapter.getItem(i);


                Integer id = cursor.getInt(cursor.getColumnIndex(RssDbAdapter.KEY_ROWID));
                mDbHelper.open();
                cursor = mDbHelper.fetchContent(id);

                cursor.moveToNext();

                String title = cursor.getString(cursor.getColumnIndex(RssDbAdapter.KEY_TITLE));
                String descr = cursor.getString(cursor.getColumnIndex(RssDbAdapter.KEY_DESC));
                String link = cursor.getString(cursor.getColumnIndex(RssDbAdapter.KEY_LINK));

                intent.putExtra(KEY_TITLE, title);
                intent.putExtra(KEY_DESC, descr);
                intent.putExtra(KEY_LINK, link);

                startActivity(intent);
            }
        });

        Intent myIntent = new Intent(ListActivity.this, MyAlarmBroadcastReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(ListActivity.this, 0, myIntent, 0);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC, SystemClock.currentThreadTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        IntentFilter intentFilter = new IntentFilter(UpdateDBService.ACTION_NEW);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(myBroadcastReceiver, intentFilter);

//        Intent update = new Intent(ListActivity.this, UpdateDBService.class);
//        startService(update);
    }

    public void show() {
        Toast.makeText(this, "rss updated", 1000).show();
        showContent();
        simpleCursorAdapter.notifyDataSetChanged();
    }

    public void alarm() {
        Toast.makeText(this, "time to update the rss!", 1000).show();
        Intent update = new Intent(ListActivity.this, UpdateDBService.class);
        startService(update);
    }

    private void showContent() {

        mDbHelper.open();
        Cursor cursor = null;
        try {
            cursor = mDbHelper.fetchArticles(currentFeedId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] from = new String[]{RssDbAdapter.KEY_TITLE, RssDbAdapter.KEY_DATE};
        int[] to = new int[]{R.id.textTitle, R.id.textDate};


        try {
            simpleCursorAdapter = new SimpleCursorAdapter(ListActivity.this, R.layout.articles_row, cursor, from, to);


            lv.setAdapter(simpleCursorAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mDbHelper.close();
            } catch (Exception exc) {
            }
            ;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mDbHelper.close();
        } catch (Exception e) {
        }
        unregisterReceiver(myBroadcastReceiver);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //   menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, DROP_ID, 0, R.string.menu_drop);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

            case DROP_ID:
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                a.setTitle(R.string.sure);

                a.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDbHelper.open();
                        mDbHelper.deleteContent(currentFeedId);
                        mDbHelper.close();
                        showContent();
                    }
                });

                a.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                a.show();

        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, SHOWBROWSER_ID, 0, R.string.menu_showbrowser);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case SHOWBROWSER_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.open();
                Cursor cursor = mDbHelper.fetchContent((int) info.id);
                if (cursor == null)
                    return false;
                cursor.moveToFirst();
                String s = cursor.getString(cursor.getColumnIndex(RssDbAdapter.KEY_LINK)).toString();
                cursor.close();
                mDbHelper.close();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                startActivity(intent);


                return true;
        }
        return super.onContextItemSelected(item);
    }


}



