package allen.g;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import allen.g.utils.FolderInfo;

/**
 * Created by local on 08/12/2017.
 */

public class GoogleDriveBackupHandler {
    public static final String TAG = "Drive-Backup-Handler";
    public static final String BACKUP_FOLDER_NAME = "picture";
    int MAX_BUFFER_SIZE = 1 * 1024 * 1024;
    GoogleAccountCredential credential;


    private ArrayList<String> listMediaNeedDel;
    private ArrayList<String> listMediaNeedAdd;
    private ArrayList<String> listFileOnGdrive;
    private String pictureDirectory;
    protected static ThreadPoolExecutor uploadPool;
    private GdriveBackupProgressCallback updateProgressCallback;
    private int numberOfFileUploaded;
    private int numberOfFileNeedUpload;
    private Activity activity;

    private com.google.api.services.drive.Drive mService;

    public interface UploadTaskCallback {
        void onUploadDone(String path, int pos);

        void onUploadFail(String path, int pos);
    }

    public interface GdriveBackupProgressCallback {
        void onGdriveBackupProgress(int currentProgress);

        void onGdriveUploadDone();

        void onGdriveUploadFail();
    }

    public GoogleDriveBackupHandler(GoogleAccountCredential credential, Activity activity) {
        this.activity = activity;
        this.credential = credential;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Drive API Android Quickstart")
                .build();
        String pathRoot = Environment.getExternalStorageDirectory().getPath();

        pictureDirectory = pathRoot + "/zalo/picture";
    }

    public void setUpdateProgressCallback(GdriveBackupProgressCallback updateProgressCallback) {
        this.updateProgressCallback = updateProgressCallback;
    }

    private ArrayList<String> getListMediaFromLocal() {
        FolderInfo picFolder = new FolderInfo(pictureDirectory);
        return picFolder.getListFiles();
    }

    private ArrayList<String> getListMediaFromDb() {
//        return (ArrayList<String>) DatabaseHelper.getInstance().getAllLocalMedia();
        return null;
    }

    private ArrayList<String> getListPictureOfCurrentUser() {
        ArrayList<String> listPictures = getListMediaFromDb();
        ArrayList<String> listPictureNames = new ArrayList<>();
        for (String picturePath : listPictures) {
            if (picturePath.contains("/picture/")) {
                java.io.File file = new java.io.File(picturePath);
                if (file != null && file.exists()) {
                    listPictureNames.add(file.getName());
                }
            }
        }
        return listPictureNames;
    }


//    private DriveFolder getRootFolder(GoogleApiClient googleApiClient) {
//        return Drive.DriveApi.getRootFolder(googleApiClient);
//    }


//    private void getOrCreateBackupFolder_ThenListFile_ThenCalculateDiff_ThenUpload() {
//        final DriveFolder rootFolder = getRootFolder(mGoogleApiClient);
//        // Check if backup folder exist
//        Query query = new Query.Builder()
//                .addFilter(Filters.eq(SearchableField.TITLE, BACKUP_FOLDER_NAME))
//                .build();
//        rootFolder.queryChildren(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
//            @Override
//            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
//                MetadataBuffer metadataBuffer = result.getMetadataBuffer();
//                if (metadataBuffer.getCount() <= 0) { // Backup folder not created
//                    Log.d(TAG, "Backup Folder not exist so now create one. Ofcourse backup folder will empty");
//                    createBackupFolder();
//
//                } else { // Backup folder exist
//                    Log.d(TAG, "Backup Folder exit. Now list all file ");
//                    Metadata metadata = metadataBuffer.get(0);
//                    DriveId backupFolderId = metadata.getDriveId();
//                    DriveFolder backupFolder = backupFolderId.asDriveFolder();
//                    listFileInBackupFolder(backupFolder, false);
//                    mBackupFolder = backupFolder;
//                }
//                metadataBuffer.release();
//            }
//        });
//    }

//    private void createBackupFolder() {
//        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(BACKUP_FOLDER_NAME).build();
//        getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
//            @Override
//            public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
//                if (!driveFolderResult.getStatus().isSuccess()) {
//                    Log.d(TAG, "Create backup folder on Google Drive failed. Return");
//                    return;
//                }
//
//                mBackupFolder = driveFolderResult.getDriveFolder();
//                listFileInBackupFolder(mBackupFolder, true);
//            }
//        });
//    }

//    private void listFileInBackupFolder(DriveFolder backupFolder, boolean isFolderHasjustCreate) {
//        if (listFileOnGdrive == null)
//            listFileOnGdrive = new ArrayList<>();
//        else listFileOnGdrive.clear();
//        if (isFolderHasjustCreate) {
//            calculateFileNeedAddorRemoveThenUpload();
//            return; // No need to list file on a new folder
//        }
//
//        if (backupFolder == null) {
//            Log.w(TAG, "Drive Backup Folder null. Maybe you should get or create it first");
//            return;
//        }
//
//        backupFolder.listChildren(mGoogleApiClient).setResultCallback(metadataResult);
//    }

//    final private ResultCallback<DriveApi.MetadataBufferResult> metadataResult = new ResultCallback<DriveApi.MetadataBufferResult>() {
//        @Override
//        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
//            if (!result.getStatus().isSuccess()) {
//                Log.d(TAG, "Problem when retrieving files list ");
//                return;
//            }
//
//            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
//            int number_of_file = metadataBuffer.getCount();
//            for (int i = 0; i < number_of_file; i++) {
//                Metadata metadata = metadataBuffer.get(i);
//                listFileOnGdrive.add(metadata.getOriginalFilename());
////                DriveId driveId = metadata.getDriveId();
////                DriveResource resource = driveId.asDriveResource();
////                resource.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
////                    @Override
////                    public void onResult(@NonNull Status status) {
////
////                    }
////                });
//            }
//            metadataBuffer.release();
//            calculateFileNeedAddorRemoveThenUpload();
//        }
//    };

    private void calculateFileNeedAddorRemoveThenUpload() {
        if (listFileOnGdrive == null)
            throw new IllegalArgumentException("Query list file on Gdrive before use this function");
        printList("List media from Gdrive", listFileOnGdrive);
//        ArrayList<String> listLocal = getListPictureOfCurrentUser();
        ArrayList<String> listLocal = getListMediaFromLocal();
        printList("List media from local", listLocal);
        ArrayList<String> intersectionList = (ArrayList<String>) CollectionUtils.intersection(listLocal, listFileOnGdrive);

        listMediaNeedDel = (ArrayList<String>) CollectionUtils.subtract(listFileOnGdrive, intersectionList);
        printList("List Media Need Del", listMediaNeedDel);
        listMediaNeedAdd = (ArrayList<String>) CollectionUtils.subtract(listLocal, intersectionList);
        printList("List Media Need Add", listMediaNeedAdd);

        uploadListFiles(listMediaNeedAdd);
    }


    public static void printList(String mes, ArrayList<String> list) {
        Log.d(TAG, "\n" + mes);
        for (String str : list) {
            if (str != null) {
                Log.d(TAG, str);
            }
        }
        Log.d(TAG, "END " + mes + "\n");
    }

    /*
    * Public method for outside call. Do the following task
    * - Get backup folder. Create one if not exist
    * - Then get list file on that folder
    * - Then compare list on gdrive with local to detect list file to upload (from local -> server) or delete file on Gdrive server
    * - Then do upload & delete
    * */
    public boolean backupMediatoGdrive() {
//        getOrCreateBackupFolder_ThenListFile_ThenCalculateDiff_ThenUpload();
        uploadListFiles(getListMediaFromLocal());
        return true;
    }

    //Actually do the jobs
    private void uploadListFiles(ArrayList<String> listFiles) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        uploadPool = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES * 2,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        Log.d(TAG, "Need upload (" + listFiles.size() + ") files");
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo/picture";
        numberOfFileUploaded = 0;
        numberOfFileNeedUpload = listFiles.size();
        if (numberOfFileNeedUpload > 0) {
            for (int i = 2; i < 4; i++) {
                UploadGoogleDriveTask uploadTask = new UploadGoogleDriveTask(pictureDirectory + "/" + listFiles.get(i), i);
                uploadTask.setUploadTaskCallback(uploadTaskCallback);
                uploadPool.execute(uploadTask);
            }
        } else {
            // No need to backup
        }

//        uploadPool.shutdown();
    }

    private UploadTaskCallback uploadTaskCallback = new UploadTaskCallback() {

        @Override
        public void onUploadDone(String path, int pos) {
            numberOfFileUploaded++;

            if (updateProgressCallback != null) {
                if (numberOfFileUploaded == numberOfFileNeedUpload) {
                    updateProgressCallback.onGdriveUploadDone();
                } else {
                    updateProgressCallback.onGdriveBackupProgress((int) (numberOfFileUploaded / numberOfFileNeedUpload));
                }
            }
        }

        @Override
        public void onUploadFail(String path, int pos) {
            Log.d(TAG, "Upload file failed at pos: " + pos);
        }
    };

    private class UploadGoogleDriveTask implements Runnable {
        private Exception mLastError = null;
        String filePath;
        int pos;
        protected UploadTaskCallback uploadTaskCallback;

        public UploadGoogleDriveTask(String filePath, int pos) {
            this.filePath = filePath;
            this.pos = pos;
        }

        public void setUploadTaskCallback(UploadTaskCallback uploadTaskCallback) {
            this.uploadTaskCallback = uploadTaskCallback;
        }

        @Override
        public void run() {
            uploadFile(filePath);
        }

        public void uploadFile(final String filePath) {
            try {
                Log.d(TAG, "Token = " + credential.getToken());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            File driveFile = new File();
            driveFile.setName("Test_file.jpg");
            driveFile.setMimeType("image/*");
            java.io.File localFile = new java.io.File(filePath);
            FileContent fileContent = new FileContent("image/*", localFile);

            try {
                File file = mService.files().create(driveFile, fileContent)
                        .setFields("id")
                        .execute();

                Log.d(TAG, "File upload done with id: " + file.getId());
            } catch (IOException e) {
                mLastError = e;
                handleException(mLastError);
            }
        }

        private void handleException(Exception mLastError) {
            if (mLastError instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        MainActivity.REQUEST_AUTHORIZATION);
            }
        }

//        final ResultCallback<DriveFolder.DriveFileResult> createFileResultCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
//            @Override
//            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
//                if (!driveFileResult.getStatus().isSuccess()) {
//                    Log.w(TAG, "Create file fail");
//                    if (uploadTaskCallback != null) {
//                        uploadTaskCallback.onUploadFail(filePath, pos);
//                    }
//                    return;
//                }
//
//                if (uploadTaskCallback != null) {
//                    uploadTaskCallback.onUploadDone(filePath, pos);
//                }
//
//                Log.d(TAG, "Upload file success! Id = " + driveFileResult.getDriveFile().getDriveId());
//            }
//        };
    }

}
