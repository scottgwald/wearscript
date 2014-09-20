package com.dappervision.wearscript.managers;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.dappervision.wearscript.BackgroundService;
import com.dappervision.wearscript.R;
import com.dappervision.wearscript.events.WearNotificationEvent;

public class WearManager extends Manager{
    private Context context;
    NotificationManager mNotificationManager;

    public WearManager(Context context, BackgroundService bs) {
        super(bs);
        this.context = context;
        reset();
    }

    @Override
    public void reset() {
        super.reset();
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    public void onEvent(WearNotificationEvent event) {
        int notificationId = 001;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_interpunc)
                        .setContentTitle(event.getTitle())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(event.getText()))
                        .setContentText(event.getText());

        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(notificationId, mBuilder.build());
    }
}
