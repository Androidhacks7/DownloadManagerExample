package com.androidhacks7.filedownloader_sample.download;

public interface NotificationListener {

    String NOTIFICATION_CHANNEL_ID = "10001";

    int triggerDownloadStartNotification(String title, String message);

    void updateNotification(int notificationId, String message);

    void triggerDownloadEndedNotification(int notificationId, String message);
}
