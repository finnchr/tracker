package com.finnchristian.tracker.ui;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.data.TracksProvider;
import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.task.BuildGpxFileAsyncTask;

import java.io.File;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackDetailsViewPagerFragment extends Fragment {
    private static final String TAG = TrackDetailsViewPagerFragment.class.getSimpleName();
    public static final String ARG_TRACK_ID = "track_id";

    protected ShareActionProvider shareActionProvider;
    private int trackId;

    public static TrackDetailsViewPagerFragment newInstance(final int trackId) {
        TrackDetailsViewPagerFragment fragment = new TrackDetailsViewPagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    public TrackDetailsViewPagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID, 0);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.trackdetailsviewpagerfragment, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share_track);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if(shareActionProvider != null) {
            final ContentResolverHelper contentResolverHelper = new ContentResolverHelper(getActivity().getContentResolver());
            final Track track = contentResolverHelper.getTrackById(trackId);
            final Uri gpxUri = TracksProvider.buildGpxUri(trackId, track.getName());

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/xml");
            intent.putExtra(Intent.EXTRA_STREAM, gpxUri);
            intent.putExtra(Intent.EXTRA_SUBJECT, track.getName()); // Google Drive filename
            intent.putExtra(Intent.EXTRA_TEXT, track.getName());

            shareActionProvider.setShareIntent(intent);
        }
        else {
            Log.d(TAG, "Share action provider is null ...");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_track_details, container, false);

        final PagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return TrackDetailsInfoFragment.newInstance(trackId);
                    case 1:
                        return TrackDetailsMapFragment.newInstance(trackId);
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.track_details_tabs_info);
                    case 1:
                        return getString(R.string.track_details_tabs_map);
                    default:
                        return "";
                }
            }
        };

        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        viewPager.setAdapter(adapter);

        return view;
    }
}
