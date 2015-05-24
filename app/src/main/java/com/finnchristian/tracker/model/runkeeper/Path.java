package com.finnchristian.tracker.model.runkeeper;

public class Path {
    private double timestamp;
    private double latitude;
    private double longitude;
    private double altitude;
    private String type; // start, end, gps, pause, resume, manual

    public double getTimestamp() {
        return timestamp;
    }

    public Path setTimestamp(double timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public Path setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public Path setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public Path setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public String getType() {
        return type;
    }

    public Path setType(String type) {
        this.type = type;
        return this;
    }
}
