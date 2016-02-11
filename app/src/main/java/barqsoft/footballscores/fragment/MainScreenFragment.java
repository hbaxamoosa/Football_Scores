package barqsoft.footballscores.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.BuildConfig;
import barqsoft.footballscores.FootballScoresApplication;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.ViewHolder;
import barqsoft.footballscores.adapter.FootballScoresSyncAdapter;
import barqsoft.footballscores.adapter.scoresAdapter;
import barqsoft.footballscores.provider.DatabaseContract;
import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>
{
    public static final int SCORES_LOADER = 0;
    public scoresAdapter mAdapter;
    private String[] fragmentdate = new String[1];
    private int last_selected_item = -1;
    private ListView score_list;

    public MainScreenFragment() {
        Timber.v("MainScreenFragment()");
    }

    static public boolean isNetworkAvailable(Context c) {
        if (BuildConfig.DEBUG) {
            Timber.d("isNetworkAvailable(Context c)");
        }
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    private void update_scores()
    {
        Timber.v("update_scores()");
        /*Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);*/
        FootballScoresSyncAdapter.initializeSyncAdapter(FootballScoresApplication.getAppContext());
    }

    public void setFragmentDate(String date)
    {
        fragmentdate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Timber.d("onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)");
        }
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        score_list = (ListView) rootView.findViewById(R.id.scores_list);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (BuildConfig.DEBUG) {
            Timber.d("onResume()");
        }
        //check if there is internet connection, load contents when true, or show dialog when false
        if (isNetworkAvailable(getActivity())) {
            update_scores();
            mAdapter = new scoresAdapter(getActivity(), null, 0);
            score_list.setAdapter(mAdapter);
            getLoaderManager().initLoader(SCORES_LOADER, null, this);
            mAdapter.detail_match_id = MainActivity.selected_match_id;
            score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ViewHolder selected = (ViewHolder) view.getTag();
                    mAdapter.detail_match_id = selected.match_id;
                    MainActivity.selected_match_id = (int) selected.match_id;
                    mAdapter.notifyDataSetChanged();
                }
            });
        } else {
            showDialogWhenOffline();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null,null,fragmentdate,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            cursor.moveToNext();
        }

        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }

    public void showDialogWhenOffline() {

        if (BuildConfig.DEBUG) {
            Timber.d("showDialogWhenOffline()");
        }
        new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.no_network_dialog_title)
                .setMessage(R.string.no_network)
                .setPositiveButton(R.string.button_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //retry loading
                        onResume();
                    }
                }).setNegativeButton(R.string.button_OK, null).show();
    }
}
