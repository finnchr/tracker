package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.Collection;

public class Track {
    @Element(required = true)
    private String name;

    @Element(required = false)
    private String comment;

    @Element(name = "desc", required = false)
    private String description;

    @Element(name = "src", required = false)
    private String source;

    @Element(required = false)
    private int number;

    @Element(required = false)
    private String type;

    @ElementList(entry = "trkseg", required = false, inline = true)
    private Collection<TrackSegment> segments;


    public String getName() {
        return name;
    }

    public Track setName(String name) {
        this.name = name;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getNumber() {
        return number;
    }

    public Track setNumber(int number) {
        this.number = number;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<TrackSegment> getSegments() {
        return segments;
    }

    public void setSegments(Collection<TrackSegment> segments) {
        this.segments = segments;
    }

    public void addSegment(final TrackSegment trackSegment) {
        if(segments == null) {
            segments = new ArrayList<>();
        }
        segments.add(trackSegment);
    }
}
