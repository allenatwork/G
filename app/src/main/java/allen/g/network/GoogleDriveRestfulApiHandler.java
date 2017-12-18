package allen.g.network;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by local on 18/12/2017.
 */

public class GoogleDriveRestfulApiHandler {
    public static final String TAG = GoogleDriveRestfulApiHandler.class.getName();
    String upLoadServerUri;
    String token;

    public GoogleDriveRestfulApiHandler(String upLoadServerUri, String token) {
        this.upLoadServerUri = upLoadServerUri;
        this.token = token;
    }

    public void uploadFile(String filePath) {
        DriveUploadTask uploadTask = new DriveUploadTask(upLoadServerUri, filePath, token);
        uploadTask.execute();
    }
}
