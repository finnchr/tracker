package com.finnchristian.tracker.model.runkeeper;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FitnessActivity {
    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);

    private String type; // Running, Cycling, Mountain Biking, Walking, Hiking, Downhill Skiing, Cross-Country Skiing, Snowboardning, Skating, Swimming, Wheelchair, Rowing, Elliptical, Other
    @SerializedName("start_time")
    private String startTime;
    @SerializedName("utc_offset")
    private int utcOffset;
    private String source;
    private String notes;
    private boolean postToFacebook;
    private boolean postToTwitter;
    private List<Path> path;

    public FitnessActivity() {
        utcOffset = 0;
        postToFacebook = false;
        postToTwitter = false;
    }

    public String getType() {
        return type;
    }

    public FitnessActivity setType(String type) {
        this.type = type;
        return this;
    }

    public String getStartTime() {
        return startTime;
    }

    public FitnessActivity setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public FitnessActivity setStartTime(long startTimeUtc) {
        setStartTime(FORMATTER.format(new Date(startTimeUtc)));
        return this;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public FitnessActivity setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
        return this;
    }

    public String getSource() {
        return source;
    }

    public FitnessActivity setSource(String source) {
        this.source = source;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public FitnessActivity setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public boolean isPostToFacebook() {
        return postToFacebook;
    }

    public FitnessActivity setPostToFacebook(boolean postToFacebook) {
        this.postToFacebook = postToFacebook;
        return this;
    }

    public boolean isPostToTwitter() {
        return postToTwitter;
    }

    public FitnessActivity setPostToTwitter(boolean postToTwitter) {
        this.postToTwitter = postToTwitter;
        return this;
    }

    public List<Path> getPath() {
        return path;
    }

    public FitnessActivity setPath(List<Path> path) {
        this.path = path;
        return this;
    }
}
