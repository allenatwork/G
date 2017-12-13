package allen.g;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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

import allen.g.PrepareBackupTask.PrepareBackupTask;
import allen.g.utils.FolderInfo;
import timber.log.Timber;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    public static final int CHOOSE_ACCOUNT = 2;
    private static final String TAG = "ZBackupDrive";
    private GoogleApiClient mGoogleApiClient;
    Button btUpload, btLogin, btChooseAcc;
    TextView tvLoginStatus;
    String filePath;
    String syncAccount;

    int MAX_BUFFER_SIZE = 1 * 1024 * 1024;
    private DriveFolder backupFolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btUpload = findViewById(R.id.upload);
        btLogin = findViewById(R.id.bt_login);
        btChooseAcc = findViewById(R.id.bt_choose);
        tvLoginStatus = findViewById(R.id.login_status);
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo/picture";
        File pictureFolder = new File(pictureDirectory);
        final ArrayList<String> listFile = new ArrayList<>();
        if (pictureFolder.isDirectory()) {
            File[] listFiles = pictureFolder.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                if (listFiles[i].isFile() && listFiles[i].getName().contains(".jpg")) {
                    listFile.add(listFiles[i].getPath());
                }
            }
        }
        filePath = listFile.get(0);
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFolder();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLogin();
            }
        });
    }

    @Override
    protected void onResume() {
        String pathRoot = Environment.getExternalStorageDirectory().getPath();
        String pictureDirectory = pathRoot + "/zalo/picture";
        FolderInfo folderInfo = new FolderInfo(pictureDirectory);
        folderInfo.printFolderInfo();
        super.onResume();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    private void requestLogin() {
//        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{"com.google"}, true, null, null,
                null, null);
        startActivityForResult(intent, CHOOSE_ACCOUNT);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    public void createFolder() {
        Timber.d("Start create folder Zalo_backup in root folder");
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("Zalo_Backup").build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                if (!driveFolderResult.getStatus().isSuccess()) {
                    logMes("Create folder failed");
                    return;
                }


                backupFolder = driveFolderResult.getDriveFolder();

                String pathRoot = Environment.getExternalStorageDirectory().getPath();
                String pictureDirectory = pathRoot + "/zalo/picture";
                FolderInfo folderInfo = new FolderInfo(pictureDirectory);
                folderInfo.printFolderInfo();
                File pictureFolder = new File(pictureDirectory);
                final ArrayList<String> listFile = new ArrayList<>();
                if (pictureFolder.isDirectory()) {
                    File[] listFiles = pictureFolder.listFiles();
                    for (int i = 0; i < listFiles.length; i++) {
                        if (listFiles[i].isFile() && listFiles[i].getName().contains(".jpg")) {
                            listFile.add(listFiles[i].getPath());
                        }
                    }
                }

                for (int i = 0; i < listFile.size(); i++) {
                    uploadFile(listFile.get(i));
                }
            }
        });
    }

    public void uploadFile(final String filePath) {
        Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                if (!result.getStatus().isSuccess()) {
                    logMes("Connect not success");
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
                    Timber.w("A file in list not found:" + file.getAbsolutePath());
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
                DriveFolder rootFolder = backupFolder;
                rootFolder.createFile(mGoogleApiClient, changeSet, driveContents).setResultCallback(createFileResultCallback);

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

    final private ResultCallback<DriveFolder.DriveFileResult> createFileResultCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
        @Override
        public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
            if (!driveFileResult.getStatus().isSuccess()) {
                logMes("Create file fail");
                return;
            }

            logMes("Create file success:" + driveFileResult.getDriveFile().getDriveId());
            Timber.d("Upload file %s success with id:%s", filePath, driveFileResult.getDriveFile().getDriveId());
        }
    };

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        android.util.Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            android.util.Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "On Activity Result");
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        } else if (requestCode == CHOOSE_ACCOUNT && resultCode == RESULT_OK) {
            Log.d(TAG, "user choose account: " + data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
            syncAccount = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            requestConnectDriveService(syncAccount);
        }
    }

    private void requestConnectDriveService(String email) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Drive.SCOPE_FILE)
                .setAccountName(email)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    public void logMes(String mes) {
        Log.d(TAG, mes);
    }
}
