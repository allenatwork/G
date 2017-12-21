package allen.g.network;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.zing.zalo.db.backup.gdrive.GdriveServiceConfig;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by local on 19/12/2017.
 */

public class DriveGetListTask extends AsyncTask<Void, Void, ArrayList<DriveFileMetadata>> {
    public static final String TAG = "Drive-Get-List";
    //    final String endPoint = "https://www.googleapis.com/drive/v3/files";
    public static final int PAGE_SIZE = 10;
    final String endPoint = "https://www.googleapis.com/drive/v3/files";
    String token;
    ArrayList<DriveFileMetadata> listFiles;
    HttpURLConnection conn;

    public DriveGetListTask(String token) {
        this.token = token;
    }

    @Override
    protected ArrayList<DriveFileMetadata> doInBackground(Void... voids) {
        Log.d(TAG, "Run on thread -" + Thread.currentThread().getName());
        return getListFileofPagewithToken(null);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listFiles = new ArrayList<>();
    }

    public String generateUrlEndpoint(String nextToken) {
        StringBuilder builder = new StringBuilder();
        builder.append(endPoint).append("?pageSize=").append(PAGE_SIZE);
        builder.append("&").append("q=").append(GdriveServiceConfig.getQueryFiles());
        if (TextUtils.isEmpty(nextToken)) {
            builder.append("&").append("pageToken=").append(nextToken);
        }
        String url = builder.toString();
        Log.d(TAG + "-Url", url);
        return url;
    }

    public ArrayList<DriveFileMetadata> getListFileofPagewithToken(String nextToken) {
        ListFileResponse response = null;
        try {
            URL url = new URL(generateUrlEndpoint(nextToken));
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setRequestMethod("GET");
            conn.setReadTimeout(2 * 1000);

            conn.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
//                InputStream inputStream = conn.getInputStream();
//                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//                String line;
//                StringBuffer stringBuffer = new StringBuffer();
//                while ((line = bufferedReader.readLine()) != null) {
//                    Log.d(TAG + "-SUCCESS", line);
//                    stringBuffer.append(line);
//                }
//                Log.d(TAG, stringBuffer.toString());
//                response = new Gson().fromJson(stringBuffer.toString(), ListFileResponse.class);
//                bufferedReader.close();
//                inputStream.close();
            } else {
                Log.d(TAG + "-FAIL", "Response code = " + responseCode);
                //Todo: handle response different response code
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.d(TAG, e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        if (response != null) {
            listFiles.addAll(response.getListFile());
            if (response.getNextPageToken() != null && response.getNextPageToken().length() > 0) {
                getListFileofPagewithToken(response.getNextPageToken());
            } else {
                Log.d(TAG + "-ListFile", "List File size:" + listFiles.size());
            }
        } else {

        }
        return listFiles;

    }
}
