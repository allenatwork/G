package allen.g.network;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by local on 18/12/2017.
 */

public class DriveUploadTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = "DriveUploadTask";
    String serverUri, localFilePath, token;

    public DriveUploadTask(String serverUri, String localFilePath, String token) {
        this.serverUri = serverUri;
        this.localFilePath = localFilePath;
        this.token = token;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        uploadFile(localFilePath);
        return null;
    }

    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpsURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "foo_bar_baz";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);
        int serverResponseCode = -1;
        try {
            DriveFileMetadata file = new DriveFileMetadata("allen_test.jpg", null, null, GdriveServiceConfig.getParentFolderId());

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

            builder.append(new Gson().toJson(file));

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
            } else {
                InputStream responseStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(responseStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG + "-Success", line);
                }
            }
            if (serverResponseCode == 200) {
                //Todo: upload complete
                Log.d(TAG, "Upload complete");
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponseCode;

    }
}
