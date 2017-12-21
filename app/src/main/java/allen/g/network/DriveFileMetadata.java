package allen.g.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    }


    public DriveFileMetadata(String name, String id, String mimeType, String parentId) {
        this.name = name;
        this.id = id;
        this.mimeType = mimeType;
        listIds = new ArrayList<>();
        listIds.add(parentId);
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
}
