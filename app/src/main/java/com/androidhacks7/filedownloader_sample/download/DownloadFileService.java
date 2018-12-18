package com.androidhacks7.filedownloader_sample.download;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

public class DownloadFileService extends JobIntentService {

    public static final String DOWNLOAD_PATH = "DOWNLOAD_PATH";
    public static final String STORAGE_PATH = "STORAGE_PATH";
    private NotificationListener notificationListener;

    private static final int UPDATE_PROGRESS = 5001;
    private static final int DOWNLOAD_DONE = 5002;

    private int notificationId;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_PROGRESS) {
                String downloaded = String.format("%.2f MB", (double) ((msg.arg1) / 1024) / 1024);
                String total = String.format("%.2f MB", (double) ((msg.arg2) / 1024) / 1024);
                String status = downloaded + " / " + total;
                if (notificationId != -1) {
                    notificationListener.updateNotification(notificationId, status);
                }
            } else if (msg.what == DOWNLOAD_DONE) {
                if (notificationId != -1) {
                    updateNotificationToDone();
                }
            }
            super.handleMessage(msg);
        }
    };

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadFileService.class, 100, work);
    }

    private void updateNotificationToDone() {
        if (notificationId != -1) {
            notificationListener.triggerDownloadEndedNotification(notificationId, "Completed");
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String downloadPath = intent.getStringExtra(DOWNLOAD_PATH);
        String destinationPath = intent.getStringExtra(STORAGE_PATH);
        startDownload(downloadPath, destinationPath);
        notificationListener = new NotificationHelper(DownloadFileService.this);
        notificationId = notificationListener.triggerDownloadStartNotification("Downloading ", "");
    }

    private void startDownload(String downloadPath, String destinationPath) {

        Uri uri = Uri.parse(downloadPath);
        final DownloadManager downloadManager = ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE));
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setAllowedOverRoaming(false);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalPublicDir(destinationPath, uri.getLastPathSegment());
        downloadManager.enqueue(request);

        final long downloadId = downloadManager.enqueue(request);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);
                    Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                        Message msg = handler.obtainMessage();
                        msg.what = DOWNLOAD_DONE;
                        handler.sendMessage(msg);
                        return;
                    }
                    //Post message to UI Thread
                    Message msg = handler.obtainMessage();
                    msg.what = UPDATE_PROGRESS;
                    msg.arg1 = bytes_downloaded;
                    msg.arg2 = bytes_total;
                    handler.sendMessage(msg);
                    cursor.close();
                    //Update every second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }
}
