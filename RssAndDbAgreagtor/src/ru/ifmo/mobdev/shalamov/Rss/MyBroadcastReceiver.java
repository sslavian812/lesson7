package ru.ifmo.mobdev.shalamov.Rss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 07.11.13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ((ListActivity) context).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}