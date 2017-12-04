package allen.g;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.google.android.gms.auth.api.signin.GoogleSignInClient;
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
//import com.google.android.gms.common.api.GoogleApiClient;
//import com.google.android.gms.drive.CreateFileActivityOptions;
//import com.google.android.gms.drive.Drive;
//import com.google.android.gms.drive.DriveClient;
//import com.google.android.gms.drive.DriveContents;
//import com.google.android.gms.drive.DriveFile;
//import com.google.android.gms.drive.DriveFolder;
//import com.google.android.gms.drive.DriveResourceClient;
//import com.google.android.gms.drive.MetadataChangeSet;
//import com.google.android.gms.tasks.Continuation;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.gms.tasks.Tasks;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    protected static final int REQUEST_CODE_RESOLUTION = 1;
    private static final String TAG = "ZBackupDrive";
    private static final int REQUEST_CODE_SIGN_IN = 0;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;

    //    private GoogleSignInClient mGoogleSignInClient;
//    private DriveClient mDriveClient;
//    private DriveResourceClient mDriveResourceClient;
    private GoogleApiClient mGoogleApiClient;
    Button btUpload, btLogin;
    TextView tvLoginStatus;
    int MAX_BUFFER_SIZE = 1 * 1024 * 1024;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btUpload = findViewById(R.id.upload);
        btLogin = findViewById(R.id.bt_login);
        tvLoginStatus = findViewById(R.id.login_status);
//        String pathRoot = Environment.getExternalStorageDirectory().getPath();
//        String pictureDirectory = pathRoot + "/zalo/picture";
//        File pictureFolder = new File(pictureDirectory);
//        final ArrayList<String> listFile = new ArrayList<>();
//        if (pictureFolder.isDirectory()) {
//            File[] listFiles = pictureFolder.listFiles();
//            for (int i = 0; i < listFiles.length; i++) {
//                if (listFiles[i].isFile() && listFiles[i].getName().contains(".jpg")) {
//                    listFile.add(listFiles[i].getPath());
//                }
//            }
//        }
//        Log.d(TAG,"Path root = " + pictureDirectory);
//        final String filePath = pathRoot + "/Download/toy.apk";
        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                saveFileToDrive();
//                createFileInAppFolder(listFile.get(0));
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                signIn();
                requestLogin();
            }
        });
    }

    /**
     * Start sign in activity.
     */
//    private void signIn() {
//        Log.i(TAG, "Start sign in");
//        mGoogleSignInClient = buildGoogleSignInClient();
//        startActivityForResult(mGoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//    }
//
//    /**
//     * Build a Google SignIn client.
//     */
//    private GoogleSignInClient buildGoogleSignInClient() {
//        GoogleSignInOptions signInOptions =
//                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestScopes(Drive.SCOPE_FILE)
//                        .build();
//        return GoogleSignIn.getClient(this, signInOptions);
//    }

    /**
     * Create a new file and save it to Drive.
     */
//    private void saveFileToDrive() {
//        // Start by creating a new contents, and setting a callback.
//        Log.i(TAG, "Creating new contents.");
//        final Bitmap image = createBitmapToSave();
//
//        mDriveResourceClient
//                .createContents()
//                .continueWithTask(
//                        new Continuation<DriveContents, Task<Void>>() {
//                            @Override
//                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception {
//                                return createFileIntentSender(task.getResult(), image);
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "Failed to create new contents.", e);
//                            }
//                        });
//    }

    /**
     * Creates an {@link IntentSender} to start a dialog activity with configured {@link
     * CreateFileActivityOptions} for user to create a new photo in Drive.
     */
//    private Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {
//        Log.i(TAG, "New contents created.");
//        // Get an output stream for the contents.
//        OutputStream outputStream = driveContents.getOutputStream();
//        // Write the bitmap data from it.
//        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
//        try {
//            outputStream.write(bitmapStream.toByteArray());
//        } catch (IOException e) {
//            Log.w(TAG, "Unable to write file contents.", e);
//        }
//
//        // Create the initial metadata - MIME type and title.
//        // Note that the user will be able to change the title later.
//        MetadataChangeSet metadataChangeSet =
//                new MetadataChangeSet.Builder()
//                        .setMimeType("image/jpeg")
//                        .setTitle("Android Photo.png")
//                        .build();
//        // Set up options to configure and display the create file activity.
//        CreateFileActivityOptions createFileActivityOptions =
//                new CreateFileActivityOptions.Builder()
//                        .setInitialMetadata(metadataChangeSet)
//                        .setInitialDriveContents(driveContents)
//                        .build();
//
//        return mDriveClient
//                .newCreateFileActivityIntentSender(createFileActivityOptions)
//                .continueWith(
//                        new Continuation<IntentSender, Void>() {
//                            @Override
//                            public Void then(@NonNull Task<IntentSender> task) throws Exception {
//                                startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATOR, null, 0, 0, 0);
//                                return null;
//                            }
//                        });
//    }

//    @Override
//    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_CODE_SIGN_IN:
//                Log.i(TAG, "Sign in request code");
//                // Called after user is signed in.
//                if (resultCode == RESULT_OK) {
//                    Log.i(TAG, "Signed in successfully.");
//                    // Use the last signed in account here since it already have a Drive scope.
//                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
//                    // Build a drive resource client.
//                    mDriveResourceClient =
//                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
//                    handleUI();
//                }
//                break;
//        }
//    }

//    private void handleUI() {
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        tvLoginStatus.setText(account.getDisplayName());
//    }
    private Bitmap createBitmapToSave() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.small_image);

        return bm;
    }

//    private void createFileInAppFolder(final String filePath) {
//        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getRootFolder();
//        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
//        Tasks.whenAll(appFolderTask, createContentsTask)
//                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
//                    @Override
//                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
//                        DriveFolder parent = appFolderTask.getResult();
//                        DriveContents contents = createContentsTask.getResult();
//                        OutputStream outputStream = contents.getOutputStream();
//                        byte[] buffer;
//                        int bufferSize;
//                        int byteAvaiable;
//                        int byteRead;
//
//                        File file = new File(filePath);
//                        FileInputStream fileInputStream = null;
//                        try {
//                            fileInputStream = new FileInputStream(file);
//                        } catch (FileNotFoundException e) {
//                            e.printStackTrace();
//                        }
//
//                        try {
//                            byteAvaiable = fileInputStream.available();
//                            bufferSize = Math.max(MAX_BUFFER_SIZE,byteAvaiable);
//                            buffer = new byte[bufferSize];
//                            byteRead = fileInputStream.read(buffer,0,bufferSize);
//                            while (byteRead > 0) {
//                                outputStream.write(buffer,0,bufferSize);
//                                byteAvaiable = fileInputStream.available();
//                                bufferSize = Math.max(byteAvaiable,MAX_BUFFER_SIZE);
//                                byteRead = fileInputStream.read(buffer,0,bufferSize);
//                            }
//                        } catch (IOException e) {
//                            Log.d(TAG,e.getMessage());
//                        }
//
//                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
//                                .setTitle(file.getName())
//                                .setMimeType("image/*")
//                                .setStarred(true)
//                                .build();
//
//                        return getDriveResourceClient().createFile(parent, changeSet, contents);
//                    }
//                })
//                .addOnSuccessListener(this,
//                        new OnSuccessListener<DriveFile>() {
//                            @Override
//                            public void onSuccess(DriveFile driveFile) {
//                                Log.e(TAG, "Create file success. File path = " + filePath);
//                            }
//                        })
//                .addOnFailureListener(this, new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "Unable to create file: " + filePath , e);
//                    }
//                });
//    }

//    private DriveResourceClient getDriveResourceClient() {
//        return mDriveResourceClient;
//    }

    private void requestLogin() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended");
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
        }
    }
}
