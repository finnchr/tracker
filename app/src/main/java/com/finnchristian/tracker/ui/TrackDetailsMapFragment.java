package com.finnchristian.tracker.ui;


import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.finnchristian.tracker.data.TracksProvider;
import com.finnchristian.tracker.model.GeoLocation;
import com.finnchristian.tracker.model.Track;
import com.finnchristian.tracker.task.CalculationsAsyncTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class TrackDetailsMapFragment extends Fragment {
    private static final String TAG = TrackDetailsMapFragment.class.getSimpleName();
    private static final String ARG_TRACK_ID = "track_id";

    /**
     * Calculations callback
     */
    protected final CalculationsAsyncTask.Callback calculationTaskCallback = new CalculationsAsyncTask.Callback() {
        @Override
        public void onResultReady(final CalculationsAsyncTask.Result result) {
            if(getActivity() == null) {
                return;
            }

            // Update map
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    googleMap.clear();

                    final Marker mapStartMarker = googleMap.addMarker(new MarkerOptions().position(result.getFirstPoint()));
                    mapStartMarker.setPosition(result.getFirstPoint());

                    final Marker mapEndMarker = googleMap.addMarker(new MarkerOptions().position(result.getLastPoint()));
                    mapEndMarker.setPosition(result.getLastPoint());

                    final Polyline mapTrackPolyline = googleMap.addPolyline(new PolylineOptions().addAll(result.getPoints()));
                    mapTrackPolyline.setPoints(result.getPoints());


                    googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition arg0) {
                            try {
                                final LatLngBounds bounds = LatLngBounds.builder().include(result.getFirstPoint()).include(result.getLastPoint()).build();
                                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                                googleMap.animateCamera(cameraUpdate);

                            }
                            catch (Exception ex) {
                                Log.w(TAG, "Error on camera update", ex);
                            }

                            // Remove listener to prevent position reset on camera move.
                            googleMap.setOnCameraChangeListener(null);
                        }
                    });

                }
            });
        }
    };

    /**
     * Observes changes to geo locations.
     */
    private final ContentObserver geoLocationsContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d(TAG, "onChange");

            if(contentResolverHelper != null) {
                final List<GeoLocation> geoLocations = contentResolverHelper.getTrackLocations(trackId);
                new CalculationsAsyncTask(geoLocations, calculationTaskCallback).execute();
            }
        }
    };

    private int trackId;
    private SupportMapFragment supportMapFragment;
    private ContentResolverHelper contentResolverHelper;


    public static TrackDetailsMapFragment newInstance(final int trackId) {
        TrackDetailsMapFragment fragment = new TrackDetailsMapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);
        fragment.setArguments(args);

        return fragment;
    }

    public TrackDetailsMapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            trackId = getArguments().getInt(ARG_TRACK_ID);
        }

        contentResolverHelper = new ContentResolverHelper(getActivity().getContentResolver());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_track_details_map, container, false);

        // Initialize map
        supportMapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().compassEnabled(true));
        getChildFragmentManager().beginTransaction().add(R.id.map_container, supportMapFragment).commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        supportMapFragment.onResume();

        // register observers (observes changes to geo locations and the track)
        getActivity().getContentResolver().registerContentObserver(TracksProvider.buildGeoLocationsUri(trackId), false, geoLocationsContentObserver);

        final List<GeoLocation> geoLocations = contentResolverHelper.getTrackLocations(trackId);
        new CalculationsAsyncTask(geoLocations, calculationTaskCallback).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        supportMapFragment.onPause();

        // unregister observers
        getActivity().getContentResolver().unregisterContentObserver(geoLocationsContentObserver);
    }


}
