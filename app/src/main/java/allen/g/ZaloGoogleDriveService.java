package allen.g;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ZaloGoogleDriveService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String TAG = "ZaloDriveService";
    private static final String ACTION_UPLOAD = "com.zing.zalo.db.backup.gdrive.action.UPLOAD";
    public static final int NOTIFICATION_ID = 1001;

    private static final String EXTRA_LIST_UPLOAD_FILE = "com.zing.zalo.db.backup.gdrive.extra.UPLOAD_LIST_FILE";
    private static final String EXTRA_EMAIL = "com.zing.zalo.db.backup.gdrive.extra.EMAIL";
    protected static GoogleApiClient mGoogleApiClient;
    private String account;
    private ArrayList<String> listFiles;


    public static final int MAX_BUFFER_SIZE = 1 * 1024 * 1024;
    protected static ThreadPoolExecutor uploadPool;


//    public ZaloGoogleDriveService() {
//        super("ZaloGoogleDriveService");
//    }

    public static void startGdriveUploadService(Context context, ArrayList<String> listFiles, String email) {
        Intent intent = new Intent(context, ZaloGoogleDriveService.class);
        intent.setAction(ACTION_UPLOAD);
        intent.putExtra(EXTRA_LIST_UPLOAD_FILE, listFiles);
        intent.putExtra(EXTRA_EMAIL, email);
        context.startService(intent);
    }


    //    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            Log.d(TAG, "Start Handle Intent Service");
//            final String action = intent.getAction();
//            if (ACTION_UPLOAD.equals(action)) {
//                listFiles = intent.getStringArrayListExtra(EXTRA_LIST_UPLOAD_FILE);
//                account = intent.getStringExtra(EXTRA_EMAIL);
//                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
//                        .addApi(Drive.API)
//                        .addScope(Drive.SCOPE_APPFOLDER)
//                        .addScope(Drive.SCOPE_FILE)
//                        .setAccountName(account)
//                        .addConnectionCallbacks(this)
//                        .addOnConnectionFailedListener(this)
//                        .build();
//                mGoogleApiClient.connect();
//            }
//        } else {
//            Log.d(TAG, "Service Intent null ! Service stop without doing anything");
//        }
//    }
//
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d(TAG, "Start Handle Intent Service");
            final String action = intent.getAction();
            if (ACTION_UPLOAD.equals(action)) {
                listFiles = intent.getStringArrayListExtra(EXTRA_LIST_UPLOAD_FILE);
                account = intent.getStringExtra(EXTRA_EMAIL);
                mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                        .addApi(Drive.API)
                        .addScope(Drive.SCOPE_APPFOLDER)
                        .addScope(Drive.SCOPE_FILE)
                        .setAccountName(account)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();
                showNotification();
            }
        } else {
            Log.d(TAG, "Service Intent null ! Service stop without doing anything");
            stopSelf();
        }
        return Service.START_STICKY;
    }

    private void showNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(ACTION_UPLOAD);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);


        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher_background);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Uploading File to Gdrive")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setOngoing(true).build();
        startForeground(NOTIFICATION_ID, notification);

    }

    private void uploadListFiles(ArrayList<String> listFiles) {
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        uploadPool = new ThreadPoolExecutor(NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());


        for (int i = 0; i < listFiles.size(); i++) {
            uploadPool.execute(new UploadGoogleDriveTask(listFiles.get(i), i));
        }
    }


    public interface UploadTaskCallback {
        public void onUploadDone(String path, int pos);

        public void onUploadFail(String path, int pos);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Service connect to GoogleApi Service SUCCESS");
        uploadListFiles(listFiles);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Service can connect Api service suppendded");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Service connect to GoogleApi Service FAILED");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Intent service done it's task and now shutdown !!! Bye");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static class UploadGoogleDriveTask implements Runnable {
        String filePath;
        int pos;
        protected UploadTaskCallback uploadTaskCallback;

        public UploadGoogleDriveTask(String filePath, int pos) {
            this.filePath = filePath;
            this.pos = pos;
            this.uploadTaskCallback = uploadTaskCallback;
        }

        @Override
        public void run() {
            if (mGoogleApiClient.isConnected()) {
                Log.d(TAG, "G Api Client connect ! Start upload file: " + filePath);
                uploadFile(filePath);
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
                    getRootFolder(mGoogleApiClient).createFile(mGoogleApiClient, changeSet, driveContents).setResultCallback(createFileResultCallback);

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

        private DriveFolder getRootFolder(GoogleApiClient googleApiClient) {
            return Drive.DriveApi.getRootFolder(googleApiClient);
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
                Log.d(TAG, "Thread pool remain task" + uploadPool.getActiveCount());
            }
        };
    }

}
