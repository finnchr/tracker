package com.finnchristian.tracker.model;

import java.util.Date;

public class Track {
    protected int id;
    protected String name;
    protected long created;
    protected long lastUploadedToRunKeeper;
    protected String type;

    public Track() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public Date getCreatedAsDate() {
        return new Date(getCreated());
    }

    public long getLastUploadedToRunKeeper() {
        return lastUploadedToRunKeeper;
    }

    public void setLastUploadedToRunKeeper(long lastUploadedToRunKeeper) {
        this.lastUploadedToRunKeeper = lastUploadedToRunKeeper;
    }

    public Date getLastUploadedToRunKeeperAsDate() {
        return new Date(getLastUploadedToRunKeeper());
    }

    public boolean isUploadedToRunKeeper() {
        return lastUploadedToRunKeeper > -1;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
