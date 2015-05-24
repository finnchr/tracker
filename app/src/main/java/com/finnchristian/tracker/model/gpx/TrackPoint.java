package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class TrackPoint {
    @Attribute(name = "lat")
    private double latitude;

    @Attribute(name = "lon")
    private double longitude;

    @Element(name = "ele")
    private double elevation;

    @Element(name = "time")
    private long time;

    @Element(name = "magvar")
    private float bearing;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }
}
