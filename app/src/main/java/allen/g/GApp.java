package allen.g;

import android.app.Application;

import allen.g.utils.FileLoggingTree;
import timber.log.Timber;

/**
 * Created by local on 05/12/2017.
 */

public class GApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        Timber.plant(new FileLoggingTree(getApplicationContext()));
        Timber.plant();
    }
}
