package com.androidhacks7.filedownloader_sample.download;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper implements NotificationListener {

    private Context mContext;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    public NotificationHelper(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int triggerDownloadStartNotification(String title, String message) {

        if (mNotificationManager == null) {
            return -1;
        }

        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setContentTitle(title)
                .setSmallIcon(android.R.drawable.arrow_down_float)
                .setContentText(message)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        int random = (int) Math.random();
        mNotificationManager.notify(random, mBuilder.build());
        return random;
    }

    @Override
    public void updateNotification(int notificationID, String message) {
        if (mNotificationManager != null) {
            mBuilder.setContentText(message);
            mNotificationManager.notify(notificationID, mBuilder.build());
        }
    }

    @Override
    public void triggerDownloadEndedNotification(int notificationID, String message) {
        updateNotification(notificationID, message);
        //TODO add pending intent to builder once download finishes
    }
}
