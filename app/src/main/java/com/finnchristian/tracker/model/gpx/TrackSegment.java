package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Collection;

public class TrackSegment {
    @ElementList(entry = "trkpt", required = false, inline = true)
    private Collection<TrackPoint> trackPoints;

    public Collection<TrackPoint> getTrackPoints() {
        return trackPoints;
    }

    public void setTrackPoints(Collection<TrackPoint> trackPoints) {
        this.trackPoints = trackPoints;
    }

    public void addTrackPoint(final TrackPoint trackPoint) {
        if(trackPoints == null) {
            trackPoints = new ArrayList<>();
        }
        trackPoints.add(trackPoint);
    }
}
