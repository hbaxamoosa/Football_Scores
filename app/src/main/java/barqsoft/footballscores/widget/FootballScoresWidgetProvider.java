package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.adapter.FootballScoresSyncAdapter;
import timber.log.Timber;

/**
 * Provider for a widget showing football scores.
 * <p/>
 * Delegates widget updating to {@link FootballScoresWidgetIntentService} to ensure that
 * data retrieval is done on a background thread
 */
public class FootballScoresWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (BuildConfig.DEBUG) {
            Timber.d("onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)");
        }
        context.startService(new Intent(context, FootballScoresWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        if (BuildConfig.DEBUG) {
            Timber.d("onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions)");
        }
        context.startService(new Intent(context, FootballScoresWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (BuildConfig.DEBUG) {
            Timber.d("onReceive(@NonNull Context context, @NonNull Intent intent)");
            Timber.d("intent.getAction() is " + intent.getAction());
        }
        if (FootballScoresSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, FootballScoresWidgetIntentService.class));
        }
    }
}
