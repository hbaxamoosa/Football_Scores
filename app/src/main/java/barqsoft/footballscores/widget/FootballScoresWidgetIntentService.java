package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.provider.DatabaseContract;
import timber.log.Timber;

/**
 * IntentService which handles updating all Football Scores widgets with the latest data
 */
public class FootballScoresWidgetIntentService extends IntentService {
    private static final String[] FOOTBALL_SCORES_COLUMNS = {
            DatabaseContract.scores_table.LEAGUE_COL = "league",
            DatabaseContract.scores_table.DATE_COL = "date",
            DatabaseContract.scores_table.TIME_COL = "time",
            DatabaseContract.scores_table.HOME_COL = "home",
            DatabaseContract.scores_table.AWAY_COL = "away",
            DatabaseContract.scores_table.HOME_GOALS_COL = "home_goals",
            DatabaseContract.scores_table.AWAY_GOALS_COL = "away_goals",
            DatabaseContract.scores_table.MATCH_ID = "match_id",
            DatabaseContract.scores_table.MATCH_DAY = "match_day"
    };
    // these indices must match the projection
    private static final int INDEX_LEAGUE_COL = 0;
    private static final int INDEX_DATE_COL = 1;
    private static final int INDEX_TIME_COL = 2;
    private static final int INDEX_HOME_COL = 3;
    private static final int INDEX_AWAY_COL = 4;
    private static final int INDEX_HOME_GOALS_COL = 5;
    private static final int INDEX_AWAY_GOALS_COL = 6;
    private static final int INDEX_MATCH_ID = 7;
    private static final int INDEX_MATCH_DAY = 8;

    public FootballScoresWidgetIntentService() {
        super("FootballScoresWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (BuildConfig.DEBUG) {
            Timber.d("onHandleIntent(Intent intent)");
        }

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FootballScoresWidgetProvider.class));

        // Get today's Football Scores from the ContentProvider
        Uri footballScoresUri = DatabaseContract.scores_table.buildScoreWithDate();
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
        String formateDate = mformat.format(System.currentTimeMillis());
        String[] dates = {formateDate};

        // for testing purposes
        // String[] dates = {"2016-02-10"};
        Cursor data = getContentResolver().query(footballScoresUri, FOOTBALL_SCORES_COLUMNS, null, dates, DatabaseContract.scores_table.TIME_COL + " DESC");
        if (data == null) {
            if (BuildConfig.DEBUG) {
                Timber.d("date is null");
            }
            return;
        }
        if (!data.moveToFirst()) {
            if (BuildConfig.DEBUG) {
                Timber.d("!data.moveToFirst()");
            }
            data.close();
            return;
        }
        // extract data from cursor
        String homeName = data.getString(INDEX_HOME_COL);
        String awayName = data.getString(INDEX_AWAY_COL);
        int homeCrest = Utilities.getTeamCrestByTeamName(homeName);
        int awayCrest = Utilities.getTeamCrestByTeamName(awayName);
        int homeScore = data.getInt(INDEX_HOME_GOALS_COL);
        int awayScore = data.getInt(INDEX_AWAY_GOALS_COL);
        String time = data.getString(INDEX_TIME_COL);
        if (BuildConfig.DEBUG) {
            Timber.d("data.close()");
        }
        data.close();

        // Perform this loop procedure for each Football Scores widget
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_football_scores);
            views.setViewVisibility(R.id.no_score, View.INVISIBLE);
            views.setViewVisibility(R.id.app_icon, View.INVISIBLE);

            // Add the data to the RemoteViews
            //display team icons
            views.setImageViewResource(R.id.home_crest, homeCrest);
            views.setImageViewResource(R.id.away_crest, awayCrest);
            //display team names
            views.setTextViewText(R.id.home_name, homeName);
            views.setTextViewText(R.id.away_name, awayName);
            //display scores and time
            views.setTextViewText(R.id.score_textview, Utilities.getScores(homeScore, awayScore));
            views.setTextViewText(R.id.time_textview, time);

            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                views.setContentDescription(R.id.home_crest, homeName);
                views.setContentDescription(R.id.away_crest, awayName);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget_info
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        data.moveToNext();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.home_team_crest, description);
    }
}
