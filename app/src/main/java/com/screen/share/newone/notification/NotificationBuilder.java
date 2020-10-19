package com.screen.share.newone.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.screen.share.newone.R;

public class NotificationBuilder {

    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    NotificationBuilder(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
                .setWhen(System.currentTimeMillis());
    }

    public NotificationBuilder setActivityClass(Class activityClass) {
        Intent activityIntent = new Intent(context, activityClass);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        return this;
    }

    public void post(int id) {
        notificationManager.notify(id, build());
    }

    public Notification build() {
        return builder.build();
    }

    public NotificationBuilder setText(String text) {
        builder.setContentText(text);
        return this;
    }


    public NotificationBuilder setTitle(String title) {
        builder.setContentTitle(title);
        return this;
    }

    public NotificationBuilder setBigText(String text) {
        builder.setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        return this;
    }

}
