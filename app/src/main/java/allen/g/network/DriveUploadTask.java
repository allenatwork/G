package allen.g.network;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by local on 18/12/2017.
 */

public class DriveUploadTask implements Runnable {
    public static final String TAG = "DriveUploadTask";
    String serverUri, token;
    DriveFileMetadata localFile;
    UploadTaskCallback uploadTaskCallback;
    final String uri = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";

    public interface UploadTaskCallback {
        void onUploadDone(String path);

        void onUploadFail(String path);
    }

    public DriveUploadTask(String token, DriveFileMetadata localFile) {
        this.serverUri = uri;
        this.localFile = localFile;
        this.token = token;
    }

    public void setUploadTaskCallback(UploadTaskCallback uploadTaskCallback) {
        this.uploadTaskCallback = uploadTaskCallback;
    }

    //    @Override
//    protected Void doInBackground(Void... voids) {
//        uploadFile(localFilePath);
//        return null;
//    }

    public int uploadFile() {

        HttpsURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "zalo_upload_media";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(localFile.getFilePath());
        int serverResponseCode = -1;
        try {
//            DriveFileMetadata file = new DriveFileMetadata(, null, null, GdriveServiceConfig.getParentFolderId());
            localFile.setParentId(GdriveServiceConfig.getParentFolderId());
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(serverUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "multipart/related; boundary=" + boundary);
//            conn.setRequestProperty("Content-Length", "124773");

            dos = new DataOutputStream(conn.getOutputStream());

//            Write metadata part
            StringBuilder builder = new StringBuilder();
            builder.append(lineEnd);
            builder.append(twoHyphens + boundary);
            builder.append(lineEnd);
            builder.append("Content-Type: application/json; charset=UTF-8");
            builder.append(lineEnd);
            builder.append(lineEnd);

            builder.append(new Gson().toJson(localFile));

            builder.append(lineEnd);
            builder.append(lineEnd);
            builder.append(twoHyphens + boundary);
            builder.append(lineEnd);
            builder.append("Content-Type: image/jpeg");
            builder.append(lineEnd);
            builder.append(lineEnd);
            Log.d(TAG, "Metadata = " + builder.toString());
            dos.writeBytes(builder.toString());

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens);
            dos.writeBytes(lineEnd);

            fileInputStream.close();
            dos.flush();
            dos.close();
            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            Log.d(TAG, "Response code: " + serverResponseCode + ". With message: " + serverResponseMessage);
            if (serverResponseCode == 400) {
                InputStream errStream = conn.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(errStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG + "-Error", line);
                }

                if (uploadTaskCallback != null) {
                    uploadTaskCallback.onUploadFail(null);
                }
            } else if (serverResponseCode == 200) {
                Log.d(TAG, "Upload complete file: " + localFile.getName());
                InputStream responseStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG + "-Success", line);
                }

                if (uploadTaskCallback != null) {
                    uploadTaskCallback.onUploadDone(null);
                }
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponseCode;

    }

    @Override
    public void run() {
        uploadFile();
    }
}
