package allen.g.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by local on 19/12/2017.
 */

public class DriveGetListTask extends AsyncTask {
    public static final String TAG = "Drive-Get-List";
    //    final String endPoint = "https://www.googleapis.com/drive/v3/files";
    final String endPoint = "https://www.googleapis.com/drive/v3/files?pageSize=100";
    String token;
    ArrayList<DriveFileMetadata> listFiles;

    public DriveGetListTask(String token) {
        this.token = token;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        HttpURLConnection conn;
        try {
            URL url = new URL(endPoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5 * 1000);

            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                InputStream inputStream = conn.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuffer stringBuffer = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    Log.d(TAG + "-SUCCESS", line);
                    stringBuffer.append(line);
                }
                Log.d(TAG, stringBuffer.toString());

                bufferedReader.close();
            } else {
                Log.d(TAG + "-FAIL", "Response code = " + responseCode);
            }
        } catch (MalformedURLException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
