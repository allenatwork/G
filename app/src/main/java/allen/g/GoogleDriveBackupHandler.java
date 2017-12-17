package allen.g;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import allen.g.utils.FolderInfo;
import timber.log.Timber;

/**
 * Created by local on 08/12/2017.
 */

public class GoogleDriveBackupHandler {
    public static final String TAG = "Drive-Backup-Handler";
    public static final String BACKUP_FOLDER_NAME = "picture";
    int MAX_BUFFER_SIZE = 1 * 1024 * 1024;


    private GoogleApiClient mGoogleApiClient;

    private ArrayList<String> listMediaNeedDel;
    private ArrayList<String> listMediaNeedAdd;
    private ArrayList<String> listFileOnGdrive;
    private DriveFolder mBackupFolder;
    private String pictureDirectory;
    protected static ThreadPoolExecutor uploadPool;
    private GdriveBackupProgressCallback updateProgressCallback;
    private int numberOfFileUploaded;
    private int numberOfFileNeedUpload;

    public interface UploadTaskCallback {
        void onUploadDone(String path, int pos);

        void onUploadFail(String path, int pos);
    }

    public interface GdriveBackupProgressCallback {
        void onGdriveBackupProgress(int currentProgress);

        void onGdriveUploadDone();

        void onGdriveUploadFail();
    }

    public GoogleDriveBackupHandler(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
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
                File file = new File(picturePath);
                if (file != null && file.exists()) {
                    listPictureNames.add(file.getName());
                }
            }
        }
        return listPictureNames;
    }


    private DriveFolder getRootFolder(GoogleApiClient googleApiClient) {
        return Drive.DriveApi.getRootFolder(googleApiClient);
    }


    private void getOrCreateBackupFolder_ThenListFile_ThenCalculateDiff_ThenUpload() {
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
                    listFileInBackupFolder(backupFolder, false);
                    mBackupFolder = backupFolder;
                }
                metadataBuffer.release();
            }
        });
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

                mBackupFolder = driveFolderResult.getDriveFolder();
                listFileInBackupFolder(mBackupFolder, true);
            }
        });
    }

    private void listFileInBackupFolder(DriveFolder backupFolder, boolean isFolderHasjustCreate) {
        if (listFileOnGdrive == null)
            listFileOnGdrive = new ArrayList<>();
        else listFileOnGdrive.clear();
        if (isFolderHasjustCreate) {
            calculateFileNeedAddorRemoveThenUpload();
            return; // No need to list file on a new folder
        }

        if (backupFolder == null) {
            Log.w(TAG, "Drive Backup Folder null. Maybe you should get or create it first");
            return;
        }

        backupFolder.listChildren(mGoogleApiClient).setResultCallback(metadataResult);
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataResult = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.d(TAG, "Problem when retrieving files list ");
                return;
            }

            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
            int number_of_file = metadataBuffer.getCount();
            for (int i = 0; i < number_of_file; i++) {
                Metadata metadata = metadataBuffer.get(i);
                listFileOnGdrive.add(metadata.getOriginalFilename());
//                DriveId driveId = metadata.getDriveId();
//                DriveResource resource = driveId.asDriveResource();
//                resource.delete(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//
//                    }
//                });
            }
            metadataBuffer.release();
            calculateFileNeedAddorRemoveThenUpload();
        }
    };

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
        getOrCreateBackupFolder_ThenListFile_ThenCalculateDiff_ThenUpload();
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
            for (int i = 0; i < numberOfFileNeedUpload; i++) {
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
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "G Api Client connect ! Start upload file: " + filePath);
                uploadFile(filePath);
                Log.d(TAG, "Start Create file !!! Now wait. Running in thread: " + Thread.currentThread().getName());
            } else {
                Log.d(TAG, "G Api Client not connected ! Upload Task stop without doing anything");
            }
        }

        public void uploadFile(final String filePath) {
            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Log.d(TAG, "Connect not success");
                        return;
                    }

                    final DriveContents driveContents = result.getDriveContents();

                    OutputStream outputStream = driveContents.getOutputStream();
                    byte[] buffer;
                    int bufferSize;
                    int byteAvaiable;
                    int byteRead;

                    File file = new File(filePath);
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        Log.d(TAG, "A file in list not found:" + file.getAbsolutePath());
                    }

                    try {
                        byteAvaiable = fileInputStream.available();
                        bufferSize = Math.max(MAX_BUFFER_SIZE, byteAvaiable);
                        buffer = new byte[bufferSize];
                        byteRead = fileInputStream.read(buffer, 0, bufferSize);
                        while (byteRead > 0) {
                            outputStream.write(buffer, 0, bufferSize);
                            byteAvaiable = fileInputStream.available();
                            bufferSize = Math.max(byteAvaiable, MAX_BUFFER_SIZE);
                            byteRead = fileInputStream.read(buffer, 0, bufferSize);
                        }
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    }

                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(file.getName())
                            .setMimeType("image/*")
                            .setStarred(true)
                            .build();
                    mBackupFolder.createFile(mGoogleApiClient, changeSet, driveContents).setResultCallback(createFileResultCallback);

                    try {
                        fileInputStream.close();
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        final ResultCallback<DriveFolder.DriveFileResult> createFileResultCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                if (!driveFileResult.getStatus().isSuccess()) {
                    Log.w(TAG, "Create file fail");
                    if (uploadTaskCallback != null) {
                        uploadTaskCallback.onUploadFail(filePath, pos);
                    }
                    return;
                }

                if (uploadTaskCallback != null) {
                    uploadTaskCallback.onUploadDone(filePath, pos);
                }

                Log.d(TAG, "Upload file success! Id = " + driveFileResult.getDriveFile().getDriveId());
            }
        };
    }

}
