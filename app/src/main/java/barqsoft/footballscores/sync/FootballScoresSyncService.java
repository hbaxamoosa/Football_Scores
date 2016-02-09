package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.adapter.FootballScoresSyncAdapter;
import timber.log.Timber;

public class FootballScoresSyncService extends Service {

    private static final Object sSyncAdapterLock = new Object();
    private static FootballScoresSyncAdapter sFootballScoresSyncAdapter = null;

    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.d("SunshineSyncService", "onCreate - SunshineSyncService");
        }
        synchronized (sSyncAdapterLock) {
            if (sFootballScoresSyncAdapter == null) {
                sFootballScoresSyncAdapter = new FootballScoresSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sFootballScoresSyncAdapter.getSyncAdapterBinder();
    }
}
