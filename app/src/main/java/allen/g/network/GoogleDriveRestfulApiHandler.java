package allen.g.network;

import android.os.Environment;

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

    public void downloadFile() {
        DriveFileMetadata file = new DriveFileMetadata("7a7eacaeba5e31a857fb5463686bb03c.jpg");
        file.setId("1PttAvpwriEC26uT_5F6Qvm8DtevCjgEE");
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo/picture";
        file.setLocalPath(pictureDirectory);
        DriveDownloadTask downloadTask = new DriveDownloadTask(token, file);
        Thread download = new Thread(downloadTask);
        download.run();
    }

    public void getListFiles() {
        DriveGetListTask getListTask = new DriveGetListTask(token);
        getListTask.execute();
    }
}
