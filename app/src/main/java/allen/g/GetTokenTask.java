package allen.g;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;


public class GetTokenTask extends AsyncTask<Void, Void, Void> {
    private FetchProfileListener fetchProfileListener;
    private static final String TAG = "Get Token Task";

    protected String mScope;
    protected String mEmail;
    protected Context context;

    public void setFetchProfileListener(FetchProfileListener fetchProfileListener) {
        this.fetchProfileListener = fetchProfileListener;
    }

    public GetTokenTask(Context context, String email, String scope) {
        this.mScope = scope;
        this.mEmail = email;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String token = fetchToken();
        Log.d(TAG, "Token = " + token);
        return null;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
            Log.e(TAG, "Exception: ", e);
        }

    }

    protected String fetchToken() {
        String token = null;
        try {
            token = GoogleAuthUtil.getToken(context, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            fetchProfileListener.onDataError(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        } catch (Exception e) {
            onError("error", e);
        }
        return token;
    }


}

