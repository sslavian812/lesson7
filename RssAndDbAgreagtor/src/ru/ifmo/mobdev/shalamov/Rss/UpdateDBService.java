package ru.ifmo.mobdev.shalamov.Rss;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 30.10.13
 * Time: 17:14
 * To change this template use File | Settings | File Templates.
 */
public class UpdateDBService extends IntentService {

    public static final String ACTION_NEW = "UpdateDBService.NEW";
    public static final String ACTION_OLD = "UpdateDBService.OLD";

    private String[] requestNames = null;
    private String[] requestLinks = null;


    private RssDbAdapter mDbHelper = null;

    public UpdateDBService() {
        super("myName");
    }

    public void onCreate() {
        super.onCreate();
        mDbHelper = new RssDbAdapter(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ArrayList<FeedItem> feed = null;     // for parser
        String xml = null;

        getLinks();

        boolean flagChanged = false;


        mDbHelper.open();

        for (int i = 0; i < requestLinks.length; ++i) {
            xml = null;
            feed = null;
            try {
                // load data
                // it's not cool, to make new objects, but
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(requestLinks[i]);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                byte[] x = EntityUtils.toByteArray(httpEntity);
                xml = new String(x);
                String encoding = xml.substring(xml.indexOf("encoding") + 10, xml.indexOf("\"?>"));

                xml = new String(x, encoding);

            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (xml != null)
                    feed = SAXXMLParser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (feed != null && feed.size() != 0) {
                for (int j = 0; j < feed.size(); ++j) {
                    FeedItem fj = feed.get(j);


                    mDbHelper.open();

                    if (!mDbHelper.createContent(fj, requestNames[i])) {
                        Log.w("FUCKENFUCK", "failed adding to base");
                    }
                    mDbHelper.close();
                }

                flagChanged = true;
            }
        }

        mDbHelper.close();

        Intent intentResponse = new Intent();
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
        if (flagChanged) {
            intentResponse.setAction(ACTION_NEW);
        } else {
            intentResponse.setAction(ACTION_OLD);
        }
        sendBroadcast(intentResponse);
    }

    private void getLinks() {
        mDbHelper.open();
        Cursor cursor = mDbHelper.fetchAllFeedsNamesAndLinks();

        int indexL = cursor.getColumnIndex(RssDbAdapter.KEY_LINK);
        int indexN = cursor.getColumnIndex(RssDbAdapter.KEY_NAME);

        int i = 0;
        String link = null;
        String name = null;


        requestLinks = new String[cursor.getCount()];
        requestNames = new String[cursor.getCount()];


        while (cursor.moveToNext() && i < requestLinks.length) {
            try {
                link = cursor.getString(indexL);
                name = cursor.getString(indexN);

            } catch (Exception e) {
                e.printStackTrace();
            }
            requestLinks[i] = link;
            requestNames[i] = name;
            ++i;
        }
        cursor.close();
    }
}

