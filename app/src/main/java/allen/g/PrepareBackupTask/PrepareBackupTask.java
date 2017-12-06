package allen.g.PrepareBackupTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by local on 06/12/2017.
 */

public class PrepareBackupTask extends AsyncTask<Void, Void, Void> {

    private WeakReference<Context> mContext;
    private GoogleApiClient mGoogleApiClient;

    public PrepareBackupTask(Context context, GoogleApiClient googleApiClient) {
        this.mContext = new WeakReference<>(context);
        this.mGoogleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Context context = null;
        if (mContext != null) {
            context = mContext.get();
        }

        if (context == null) return null;

        Timber.d("Start create folder Zalo_backup in root folder");
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle("Zalo_Backup").build();
        getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
            @Override
            public void onResult(@NonNull DriveFolder.DriveFolderResult driveFolderResult) {
                if (!driveFolderResult.getStatus().isSuccess()) {
                    Timber.e("Create backup folder on Google Drive failed. Return");
                    return;
                }

                DriveFolder driveBackupFolder = driveFolderResult.getDriveFolder();
                driveBackupFolder.listChildren(mGoogleApiClient).setResultCallback(metadataResult);
            }
        });
        return null;
    }

    final private ResultCallback<DriveApi.MetadataBufferResult> metadataResult = new ResultCallback<DriveApi.MetadataBufferResult>() {
        @Override
        public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Timber.e("Problem when retrieving files list ");
                return;
            }
            MetadataBuffer metadataBuffer = result.getMetadataBuffer();

            Metadata metadata = metadataBuffer.get(0);
            Timber.d("File name get from folder" + metadata.getOriginalFilename());
        }
    };

    private DriveFolder getRootFolder(GoogleApiClient googleApiClient) {
        return Drive.DriveApi.getRootFolder(googleApiClient);
    }
}
