package com.screen.share.newone.notification;

import android.app.NotificationManager;
import android.content.Context;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MyNotification {

    public static void cancelNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(id);
        }
    }

    public static NotificationBuilder from(Context context) {
        return new NotificationBuilder(context);
    }
}
