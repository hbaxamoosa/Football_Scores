package barqsoft.footballscores;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

public class FootballScoresApplication extends Application{

    private static Context context;

    public static Context getAppContext() {
        return FootballScoresApplication.context;
    }

    @Override public void onCreate() {
        super.onCreate();

        FootballScoresApplication.context = getApplicationContext();

        //Including Jake Wharton's Timber logging library
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // Timber.plant(new CrashReportingTree());
        }

        // Facebook Stetho
        Stetho.initializeWithDefaults(this);
    }
}
