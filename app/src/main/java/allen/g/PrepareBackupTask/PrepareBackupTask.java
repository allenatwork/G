package allen.g.PrepareBackupTask;

import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import allen.g.utils.CollectionUtil;

/**
 * Created by local on 06/12/2017.
 */

public class PrepareBackupTask extends AsyncTask<Void, Void, List<String>> {
    public static final String BACKUP_FOLDER_NAME = "Zalo_Backup";
    private static final String TAG = "PrepareBackupTask";
    private List<String> listFiles;
    private GoogleApiClient mGoogleApiClient;

    public PrepareBackupTask(GoogleApiClient googleApiClient) {
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        listFiles = new ArrayList<>();
        Log.d(TAG, "Do in background " + Thread.currentThread().getName());

        final DriveFolder rootFolder = getRootFolder(mGoogleApiClient);

        // Check if backup folder exist
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, BACKUP_FOLDER_NAME))
                .build();
        rootFolder.queryChildren(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                MetadataBuffer metadataBuffer = result.getMetadataBuffer();
                if (metadataBuffer.getCount() <= 0) { // Backup folder not created
                    Log.d(TAG, "Backup Folder not exist so now create one. Ofcourse backup folder will empty");
                    createBackupFolder();

                } else { // Backup folder exist
                    Log.d(TAG, "Backup Folder exit. Now list all file ");
                    Metadata metadata = metadataBuffer.get(0);
                    DriveId backupFolderId = metadata.getDriveId();
                    DriveFolder backupFolder = backupFolderId.asDriveFolder();
                    listFileInBackupFolder(backupFolder);
                }
                metadataBuffer.release();
            }
        });
        ArrayList<String> listLocalFile = getListFileInPictureFolder();
//        Log.d(TAG, "============LIST LOCAL FILES===========");
//        printList(listLocalFile);
//        Log.d(TAG, "============END LIST LOCAL FILES===========");
        compareTwoList(listLocalFile, listFiles);
        return listFiles;
    }

    @Override
    protected void onPostExecute(List<String> listFile) {
        super.onPostExecute(listFile);

//        ArrayList<String> listLocalFile = getListFileInPictureFolder();
//        Log.d(TAG, "============LIST LOCAL FILES===========");
//        printList(listLocalFile);
//        Log.d(TAG, "============END LIST LOCAL FILES===========");
//        compareTwoList(listLocalFile, listFiles);
    }

    public void printList(ArrayList<String> listFiles) {
        for (String s : listFiles) {
            Log.d(TAG, s);
        }

    }

    private void createBackupFolder() {
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(BACKUP_FOLDER_NAME).build();
        getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                if (!driveFolderResult.getStatus().isSuccess()) {
                    Log.d(TAG, "Create backup folder on Google Drive failed. Return");
                    return;
                }
            }
        });
    }

    private void listFileInBackupFolder(DriveFolder backupFolder) {
        backupFolder.listChildren(mGoogleApiClient).setResultCallback(metadataResult);
    }

    private ArrayList<String> getListFileInPictureFolder() {
        ArrayList<String> listPictures = new ArrayList<>();
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo/picture";
        File pictureFolder = new File(pictureDirectory);
        if (pictureFolder.isDirectory()) {
            File[] listFiles = pictureFolder.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile() && listFiles[i].getName().contains(".jpg")) {
                    listPictures.add(listFiles[i].getName());
                }
            }
        }
        return listPictures;
    }

    private void compareTwoList(List<String> listLocal, List<String> listServer) {
        ArrayList<String> intersectionList = (ArrayList<String>) CollectionUtil.intersection(listLocal, listServer);
//        Log.d(TAG, "============LIST FILE INTERSECTION===========");
//        printList(intersectionList);
//        Log.d(TAG, "============END LIST FILE INTERSECTION===========");
        ArrayList<String> listNeedDel = (ArrayList<String>) CollectionUtil.subtract(listServer, intersectionList);
        ArrayList<String> listNeedAdd = (ArrayList<String>) CollectionUtil.subtract(listLocal, intersectionList);
//        int i;
//        Log.d(TAG, "============LIST FILE NEED DEL===========");
//        printList(listNeedDel);
//        Log.d(TAG, "============END LIST FILE NEED DEL===========");
//
//        Log.d(TAG, "============LIST FILE NEED ADD===========");
//        printList(listNeedAdd);
//        Log.d(TAG, "============END LIST FILE NEED ADD===========");
    }


    final private ResultCallback<DriveApi.MetadataBufferResult> metadataResult = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Problem when retrieving files list ");
                return;
            }
//            Log.d(TAG, "============LIST FILE IN FOLDER===========");
            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
            int number_of_file = metadataBuffer.getCount();
            for (int i = 0; i < number_of_file; i++) {
                Metadata metadata = metadataBuffer.get(i);
                listFiles.add(metadata.getOriginalFilename());
                Log.d(TAG, "File name get from folder: " + metadata.getOriginalFilename());
            }
            metadataBuffer.release();
//            Log.d(TAG, "============END LIST FILE IN FOLDER===========");
        }
    };

    private DriveFolder getRootFolder(GoogleApiClient googleApiClient) {
        return Drive.DriveApi.getRootFolder(googleApiClient);
    }

}
