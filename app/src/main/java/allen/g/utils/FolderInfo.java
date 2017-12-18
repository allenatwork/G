package allen.g.utils;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by allen on 12/12/17.
 */

public class FolderInfo {
    public static final String TAG = "Folder Info";
    String folderPath;
    long folderSize;
    ArrayList<String> listFiles;

    public FolderInfo() {
        folderSize = 0;
        listFiles = new ArrayList<>();
    }

    public FolderInfo(String path) {
        folderPath = path;
        File thisFolder = new File(path);
        if (!thisFolder.exists() || !thisFolder.isDirectory())
            throw new IllegalArgumentException("Path is not a folder. Check mate");

        folderSize = 0;
        listFiles = new ArrayList<>();

        goThroughFolder(thisFolder);

    }

    private void goThroughFolder(File folder) {
        File[] listFileArray = folder.listFiles();

        for (File file : listFileArray) {
            if (file.isFile()) {
                listFiles.add(file.getName());
                folderSize += file.length();
            } else if (file.isDirectory()) {
                goThroughFolder(file);
            }
        }
    }

    public ArrayList<String> getListFiles () {
        return listFiles;
    }

    public void printListFileinFolder() {
        Log.d(TAG, "Listing File in folder ");
        for (String filePath : listFiles) {
            Log.d(TAG, filePath);
        }
        Log.d(TAG, "End Listing File in folder ");
    }

    public void printFolderInfo() {
        Log.d(TAG, "Info of folder - " + folderPath);
        Log.d(TAG, "Folder size = " + folderSize);
        printListFileinFolder();
        Log.d(TAG, "End Info of folder");
    }
}
