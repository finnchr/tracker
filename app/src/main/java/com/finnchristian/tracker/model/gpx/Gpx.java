package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="gpx")
public class Gpx {
    @Attribute(required = false)
    private String version;

    @Attribute(required = false)
    private String creator;

    @Element(required = false)
    private Metadata metadata;

    @Element(name = "trk", required = true)
    private Track track;

    public String getVersion() {
        return version;
    }

    public Gpx setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getCreator() {
        return creator;
    }

    public Gpx setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public Gpx setMetadata(Metadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public Track getTrack() {
        return track;
    }

    public void setTrack(Track track) {
        this.track = track;
    }
}
