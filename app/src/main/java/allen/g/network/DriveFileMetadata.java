package allen.g.network;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by local on 19/12/2017.
 */

public class DriveFileMetadata {
    @SerializedName("name")
    String name;

    @SerializedName("id")
    String id;

    @SerializedName("mimeType")
    String mimeType;

    @SerializedName("parents")
    ArrayList<String> listIds;

    @Expose
    String localPath;

    public DriveFileMetadata(String name) {
        this.name = name;
        id = null;
        mimeType = null;
        listIds = new ArrayList<>();
    }


    public DriveFileMetadata(String name, String id, String mimeType, String parentId) {
        this.name = name;
        this.id = id;
        this.mimeType = mimeType;
        listIds = new ArrayList<>();
        if (!TextUtils.isEmpty(parentId)) {
            listIds.add(parentId);
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!DriveFileMetadata.class.isAssignableFrom(obj.getClass())) return false;
        DriveFileMetadata target = (DriveFileMetadata) obj;
        if (target.name == null) return false;
        return target.name.equals(this.name);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public String getName() {
        return name;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getFilePath() {
        return localPath + "/" + name;
    }

    public void setParentId(String parentId) {
        if (TextUtils.isEmpty(parentId)) return;
        listIds.add(parentId);
    }

    public String getId() {
        return id;
    }

    public boolean isFileExitOnLocal () {
        File file = new File(getFilePath());
        return file.exists();
    }
}
