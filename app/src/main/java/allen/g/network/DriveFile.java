package allen.g.network;

import com.google.gson.annotations.SerializedName;

/**
 * Created by local on 19/12/2017.
 */

public class DriveFile {
    @SerializedName("name")
    String name;

    public DriveFile(String name) {
        this.name = name;
    }
}
