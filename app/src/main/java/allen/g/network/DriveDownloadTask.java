package allen.g.network;

import android.text.TextUtils;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by local on 21/12/2017.
 */

public class DriveDownloadTask implements Runnable {
    public static final String TAG = "Drive-Download-Task";
    String token;
    DriveFileMetadata fileNeedDownload;
    String endPoind = "https://www.googleapis.com/drive/v3/files/";
    int maxBufferSize = 1 * 1024 * 1024;
    String fileUrl;

    public DriveDownloadTask(String token, DriveFileMetadata fileNeedDownload) {
        this.token = token;
        this.fileNeedDownload = fileNeedDownload;
        fileUrl = endPoind + "/" + fileNeedDownload.getId();
    }


    @Override
    public void run() {
        if (TextUtils.isEmpty(fileNeedDownload.getId())) return;

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = conn.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(fileNeedDownload.getFilePath());
                int bytes_read = -1;
                byte[] buffer = new byte[maxBufferSize];
                while ((bytes_read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytes_read);
                }
                fileOutputStream.close();
                inputStream.close();
                conn.disconnect();
            } else {
                Log.d(TAG, "Download fail with response code: " + responseCode);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
