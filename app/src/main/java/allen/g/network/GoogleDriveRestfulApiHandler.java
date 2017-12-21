package allen.g.network;

import android.os.Environment;
import android.util.Log;

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
//        DriveUploadTask uploadTask = new DriveUploadTask(upLoadServerUri, filePath, token);
//        uploadTask.execute();
    }

    public void downloadFile() {
        DriveFileMetadata file = new DriveFileMetadata("00837917916ec19d2d0a04f72e7a99de.jpg");
        file.setId("1Y-aVkGFuQrZ0xmWrMBtIkFLv8ox0hf5l");
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo";
        file.setLocalPath(pictureDirectory);
        Log.d(TAG,"Downlaod file : " + file.getFilePath());
        DriveDownloadTask downloadTask = new DriveDownloadTask(token, file);
        Thread download = new Thread(downloadTask);
        download.start();
    }

    public void getListFiles() {
        DriveGetListTask getListTask = new DriveGetListTask(token);
        getListTask.execute();
    }
}
