package com.androidhacks7.filedownloader_sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.androidhacks7.filedownloader_sample.download.DownloadFileService;
import com.androidhacks7.filedownloader_sample.download.StorageHelper;

public class MainActivity extends AppCompatActivity {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 50;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.enter_download_url);
        Button button = (Button) findViewById(R.id.download_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                    return;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                StorageHelper.createDirectory(this);
                Intent intent = new Intent(MainActivity.this, DownloadFileService.class);
                intent.putExtra(DownloadFileService.DOWNLOAD_PATH, editText.getText().toString());
                intent.putExtra(DownloadFileService.STORAGE_PATH, StorageHelper.ROOT_DIRECTORY_NAME.concat("/"));
                DownloadFileService.enqueueWork(this, intent);
            }
        }
    }
}
