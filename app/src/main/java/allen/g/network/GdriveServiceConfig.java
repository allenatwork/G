package allen.g.network;

/**
 * Created by local on 20/12/2017.
 */

public class GdriveServiceConfig {
    public static final boolean IS_DEBUG = true;


    public static String getParentFolderId() {
        if (!IS_DEBUG) {
            return "appDataFolder";
        } else {
            return "1wXlYCmpFSC8Wklw5poX_2TGv2Bag74_A";
        }
    }

    public static String getQueryFiles() {
        StringBuilder builder = new StringBuilder();
        builder
                .append("'")
                .append(getParentFolderId())
                .append("'%20in%20parents");
        return builder.toString();
    }
}
