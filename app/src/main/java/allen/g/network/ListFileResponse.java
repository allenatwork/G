package allen.g.network;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by local on 19/12/2017.
 */

public class ListFileResponse {
    @SerializedName("kind")
    String kind;

    @SerializedName("nextPageToken")
    String nextPageToken;

    @SerializedName("incompleteSearch")
    String incompleteSearch;

    @SerializedName("files")
    ArrayList<DriveFileMetadata> listFile;

    public ListFileResponse(String kind, String nextPageToken, String incompleteSearch, ArrayList<DriveFileMetadata> listFile) {
        this.kind = kind;
        this.nextPageToken = nextPageToken;
        this.incompleteSearch = incompleteSearch;
        this.listFile = listFile;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getIncompleteSearch() {
        return incompleteSearch;
    }

    public void setIncompleteSearch(String incompleteSearch) {
        this.incompleteSearch = incompleteSearch;
    }

    public ArrayList<DriveFileMetadata> getListFile() {
        return listFile;
    }

    public void setListFile(ArrayList<DriveFileMetadata> listFile) {
        this.listFile = listFile;
    }
}
