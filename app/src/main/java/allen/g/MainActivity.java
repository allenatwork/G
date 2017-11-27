package allen.g;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TEST";
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGoogleSignInClient = buildGoogleSignInClient();
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

//    private void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
//        Log.i(TAG, "Update view with sign in account task");
//        task.addOnSuccessListener(
//                new OnSuccessListener<GoogleSignInAccount>() {
//                    @Override
//                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
//                        Log.i(TAG, "Sign in success");
//                        // Build a drive client.
//                        mDriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
//                        // Build a drive resource client.
//                        mDriveResourceClient =
//                                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);
//                        // Start camera.
//                        startActivityForResult(
//                                new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
//                    }
//                })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Log.w(TAG, "Sign in failed", e);
//                            }
//                        });
//    }
}
