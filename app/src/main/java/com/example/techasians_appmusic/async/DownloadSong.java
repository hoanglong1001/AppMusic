package com.example.techasians_appmusic.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static com.example.techasians_appmusic.activity.PlayerActivity.isDownload;

public class DownloadSong extends AsyncTask<String, Integer, String> {

    private Context context;
    private ProgressDialog progressDialog;

    public DownloadSong(Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        String stringURL = strings[0];
        String songName = strings[1];
        try {
            URL url = new URL(stringURL);
            String sdCard = Environment.getExternalStorageDirectory().toString();
            File mkdir = new File(sdCard, "");
            if (!mkdir.exists()) {
                mkdir.mkdir();
            }
            File file = new File(mkdir, songName);
            if (file.exists()) {
                file.delete();
            }
            URLConnection urlConn = url.openConnection();
            InputStream inputStream = null;
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            int totalSize = httpConn.getContentLength();
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            float downloadSize = 0;
            float bufferLength = 0;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer);
                downloadSize += bufferLength;
                publishProgress((int) ((downloadSize / totalSize) * 100));
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            outputStream.close();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            isDownload = true;
            return "Đã tải xong";
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        isDownload = false;
        return "Lỗi";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
        Log.e("TEST", "PERCENT: " + values[0]);
    }

    @Override
    protected void onPostExecute(String b) {
        super.onPostExecute(b);
        progressDialog.dismiss();
        Toast.makeText(context, b, Toast.LENGTH_SHORT).show();
    }
}

