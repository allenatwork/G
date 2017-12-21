package allen.g.network;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.zing.zalo.db.backup.gdrive.GdriveServiceConfig;
import com.zing.zalo.ui.zviews.SettingBackupView;

import java.io.IOException;
import java.lang.ref.SoftReference;

/**
 * Created by local on 20/12/2017.
 */

public class RetrieveTokenTask extends AsyncTask<String, Void, String> {
    public static final String SCOPE_DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";
    public static final String SCOPE_DRIVE = "https://www.googleapis.com/auth/drive";
    public static final String TAG = "Retrieve-Token-Task";

    public interface GetDriveTokenCallBack {
        void onGetTokenSuccess(String token);

        void onGetTokenFail();
    }

    private GetDriveTokenCallBack getDriveTokenCallBack;

    SoftReference<Context> zaloActivity;

    public RetrieveTokenTask(Context context) {
        this.zaloActivity = new SoftReference<>(context);
    }

    public void setGetDriveTokenCallBack(GetDriveTokenCallBack getDriveTokenCallBack) {
        this.getDriveTokenCallBack = getDriveTokenCallBack;
    }

    @Override
    protected String doInBackground(String... params) {
        String accountName = params[0];
        if (accountName == null && accountName.length() <= 0) return null;
        Context context = zaloActivity.get();
        if (context == null) return null;


        String scopes;
        if (!GdriveServiceConfig.IS_DEBUG) {
            scopes = "oauth2: " + SCOPE_DRIVE_APPDATA;
        } else {
            scopes = "oauth2: " + SCOPE_DRIVE;
        }
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(context, accountName, scopes);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } catch (UserRecoverableAuthException e) {
            ((Activity) context).startActivityForResult(e.getIntent(), SettingBackupView.REQ_SIGN_IN_REQUIRED);
        } catch (GoogleAuthException e) {
            Log.e(TAG, e.getMessage());
        }
        return token;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
//        requestUploadFileWithToken(s);
        if (getDriveTokenCallBack != null) {
            if (TextUtils.isEmpty(s)) {
                getDriveTokenCallBack.onGetTokenFail();
            } else {
                getDriveTokenCallBack.onGetTokenSuccess(s);
            }
        }
        Log.d("TAG", "Token: " + s);
    }
}
