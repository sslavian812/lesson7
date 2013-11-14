package ru.ifmo.mobdev.shalamov.Rss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 30.10.13
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */
public class ShowActivity extends Activity {
    Intent intent;
    WebView webView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mywebview);
        intent = getIntent();

        String title = intent.getStringExtra(ListActivity.KEY_TITLE);
        String description = intent.getStringExtra(ListActivity.KEY_DESC);
        String link = intent.getStringExtra(ListActivity.KEY_LINK);

        webView = (WebView) findViewById(R.id.webView);

        String content = null;
        content = "<b>" +"<a href=" + link + ">" + title + "</a>" + "</b>" + "<br>" + "<br>" + description;
        webView.loadData(content, "text/html; charset=UTF-8", null);
    }
}
