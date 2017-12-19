package allen.g.network;

import com.google.gson.annotations.SerializedName;

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

    public DriveFileMetadata(String name) {
        this.name = name;
    }

    public DriveFileMetadata(String name, String id, String mimeType) {
        this.name = name;
        this.id = id;
        this.mimeType = mimeType;
    }
}
