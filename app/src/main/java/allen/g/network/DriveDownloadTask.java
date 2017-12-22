package allen.g.network;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by local on 21/12/2017.
 */

public class DriveDownloadTask implements Runnable {
    public static final String TAG = "Drive-Download";
    String token;
    DriveFileMetadata fileNeedDownload;
    String endPoind = "https://www.googleapis.com/drive/v3/files/";
    int maxBufferSize = 1 * 1024 * 1024;
    String fileUrl;

    public DriveDownloadTask(String token, DriveFileMetadata fileNeedDownload) {
        this.token = token;
        this.fileNeedDownload = fileNeedDownload;
        fileUrl = endPoind + fileNeedDownload.getId()+"?alt=media";
    }


    @Override
    public void run() {
        if (TextUtils.isEmpty(fileNeedDownload.getId())) return;

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoInput(true);

            conn.connect();
                InputStream inputStream = conn.getInputStream();
                Log.d(TAG,"File path = " + fileNeedDownload.getFilePath());

                String dirPath = fileNeedDownload.getLocalPath();
                File dir = new File(dirPath);
                dir.mkdir();

                File outFile = new File(dir,fileNeedDownload.getName());
                if (!outFile.exists()) {
                    outFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                int bytes_read = -1;
                byte[] buffer = new byte[maxBufferSize];
                while ((bytes_read = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytes_read);
                }
                fileOutputStream.close();
                inputStream.close();
                conn.disconnect();
//            } else {
//                Log.d(TAG, "Download fail with response code: " + responseCode);
//                InputStream errorStream = conn.getErrorStream();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errorStream));
//                String line;
//                while ((line = bufferedReader.readLine()) != null) {
//                    Log.d(TAG + "-Error", line);
//                }
//            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
