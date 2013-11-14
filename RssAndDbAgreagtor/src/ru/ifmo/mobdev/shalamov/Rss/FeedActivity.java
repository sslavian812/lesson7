package ru.ifmo.mobdev.shalamov.Rss;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

public class FeedActivity extends Activity {
    /**
     * Called when the activity is first created.
     */


    private RssDbAdapter mDbHelper = null;
    private SimpleCursorAdapter items = null;
    private ListView lv = null;

    public static final String KEY_QUERY = "query";
    public static final String KEY_ALL = "*";

    public static final String KEY_NAME = "name";
    public static final String KEY_LINK = "link";

    public static final String KEY_ID = RssDbAdapter.KEY_ROWID;

    public static final int CODE_ADD = 0;
    public static final int CODE_UPDATE = 1;


    private static final int INSERT_ID = Menu.FIRST;
    private static final int DROP_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feeds);

        mDbHelper = new RssDbAdapter(this);


        lv = (ListView) findViewById(R.id.listView);
        registerForContextMenu(lv);
        fillFeedsList();


        Button btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FeedActivity.this, ListActivity.class);
                intent.putExtra(KEY_QUERY, KEY_ALL);
                startActivity(intent);
            }
        });


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(FeedActivity.this, ListActivity.class);
                Cursor cursor = (Cursor) items.getItem(i);
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(RssDbAdapter.KEY_NAME));

                intent.putExtra(KEY_QUERY, name);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, DROP_ID, 0, R.string.menu_drop);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                Intent intent = new Intent(FeedActivity.this, AddFeedActivity.class);
                startActivityForResult(intent, CODE_ADD);
                return true;
            case DROP_ID:
                AlertDialog.Builder a = new AlertDialog.Builder(this);
                a.setTitle(R.string.sure);

                a.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mDbHelper.open();
                        mDbHelper.dropFeeds();
                        mDbHelper.dropContent();
                        mDbHelper.close();
                        fillFeedsList();
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
        menu.add(0, DELETE_ID, 0, R.string.menu_delete);
        menu.add(0, EDIT_ID, 0, R.string.menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.open();
                mDbHelper.deleteFeed(info.id);
                mDbHelper.deleteContent((int)info.id);
                mDbHelper.close();
                fillFeedsList();
                items.notifyDataSetChanged();
                return true;
            case EDIT_ID:
                AdapterView.AdapterContextMenuInfo info2 = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.open();

                Intent intent = new Intent(this, AddFeedActivity.class);
                Cursor cursor = mDbHelper.fetchFeed((int) info2.id);
                startManagingCursor(cursor);
                if (cursor == null) {
                    Toast.makeText(FeedActivity.this, "something goes wrong", 1000).show();      // newer reached
                    return false;
                } else {
                    int posN = cursor.getColumnIndex(KEY_NAME);
                    int posL = cursor.getColumnIndex(KEY_LINK);
                    int posI = cursor.getColumnIndex(KEY_ID);


                    try {
                        cursor.moveToFirst();
                        String name = cursor.getString(posN);
                        String link = cursor.getString(posL);
                        int id = cursor.getInt(posI);
                        intent.putExtra(KEY_NAME, name);
                        intent.putExtra(KEY_LINK, link);
                        intent.putExtra(KEY_ID, id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    cursor.close();
                    startActivityForResult(intent, CODE_UPDATE);
                }
                mDbHelper.close();
                fillFeedsList();
                items.notifyDataSetChanged();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void fillFeedsList() {
        Cursor cursor = null;
        // startManagingCursor(cursor);   this line makes me unhappy and cryshes the app :(

        try {
            mDbHelper.open();
            cursor = mDbHelper.fetchAllFeedsNames();

        } catch (Exception e) {
            e.printStackTrace();
        }


        String[] from = new String[]{RssDbAdapter.KEY_NAME};
        int[] to = new int[]{R.id.text1};

        try {
            items = new SimpleCursorAdapter(this, R.layout.feeds_row, cursor, from, to);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            lv.setAdapter(items);
        } catch (Exception e) {
            e.printStackTrace();
            cursor.close();
        }

        // cursor.close();   this line makes the list of feeds empty, byt in base there are links and content!
        mDbHelper.close();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            mDbHelper.open();
            if (resultCode == AddFeedActivity.RESULT_ADD) {
                if (!mDbHelper.createFeed(data.getStringExtra(KEY_NAME), data.getStringExtra(KEY_LINK)))
                    Toast.makeText(FeedActivity.this, "inserting failed!!", 1000).show();
            } else {
                if (!mDbHelper.editFeed(data.getIntExtra(KEY_ID, -1), data.getStringExtra(KEY_NAME), data.getStringExtra(KEY_LINK)))
                    Toast.makeText(FeedActivity.this, "edit failed!!", 1000).show();
            }
            mDbHelper.close();


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(FeedActivity.this, "inserting failed!!", 1000).show();
        }

        fillFeedsList();
    }

}
