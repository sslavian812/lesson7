package ru.ifmo.mobdev.shalamov.Rss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 08.11.13
 * Time: 0:52
 * To change this template use File | Settings | File Templates.
 */
public class MyAlarmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent update = new Intent(context, UpdateDBService.class);
            context.startService(update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

