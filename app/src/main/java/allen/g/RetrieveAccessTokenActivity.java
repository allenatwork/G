package allen.g;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.services.drive.DriveScopes;

import java.io.IOException;
import java.util.ArrayList;

import allen.g.network.GoogleDriveRestfulApiHandler;
import allen.g.utils.FolderInfo;


public class RetrieveAccessTokenActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "RetrieveAccessToken";
    private static final int REQ_SIGN_IN_REQUIRED = 55664;

    private String mAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_access_token);
        findViewById(R.id.button_token).setOnClickListener(this);

        mAccountName = "nguyengocbro@gmail.com";
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.button_token) {
            new RetrieveTokenTask().execute(mAccountName);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SIGN_IN_REQUIRED && resultCode == RESULT_OK) {
            // We had to sign in - now we can finish off the token request.
            new RetrieveTokenTask().execute(mAccountName);
        }
    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2: " + DriveScopes.DRIVE;
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ((TextView) findViewById(R.id.token_value)).setText("Token Value: " + s);
            requestUploadFileWithToken(s);
            Log.d("TAG", "Token: " + s);
        }
    }

    public void requestUploadFileWithToken(final String token) {
        if (token == null) {
            Log.d(TAG, "Token null ! Return program");
        }

        String pathRoot = Environment.getExternalStorageDirectory().getPath();

        String pictureDirectory = pathRoot + "/zalo/picture";

        FolderInfo folderPicture = new FolderInfo(pictureDirectory);
        ArrayList<String> listFile = folderPicture.getListFiles();
        final String randomFile = pictureDirectory + "/" + listFile.get(4);
        Log.d(TAG, "UPload file: " + randomFile);
//        final String uri = "https://www.googleapis.com/upload/drive/v3/files";
        final String uri = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart";
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                GoogleDriveRestfulApiHandler restfulApiHandler = new GoogleDriveRestfulApiHandler(uri, token);
                restfulApiHandler.uploadFile(randomFile);
            }
        });
        thread.run();

    }


}