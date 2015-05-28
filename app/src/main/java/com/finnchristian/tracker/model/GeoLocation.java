package com.finnchristian.tracker.model;

import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class GeoLocation {
    protected int trackId;
    protected int id;
    protected long created;
    protected double latitude;
    protected double longitude;
    protected double altitude;
    protected float speed;
    protected float bearing;
    protected float accuracy;

    public GeoLocation() {

    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

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

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public Date getCreatedAsDate() {
        return new Date(getCreated());
    }

    public Location toLocation() {
        final Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(getLatitude());
        location.setLongitude(getLongitude());
        location.setAltitude(getAltitude());
        location.setAccuracy(getAccuracy());
        location.setBearing(getBearing());
        location.setSpeed(getSpeed());
        return location;
    }

    public LatLng getLatLng() {
        return new LatLng(getLatitude(), getLongitude());
    }
}


