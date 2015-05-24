package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Attribute;

public class Author {
    @Attribute(required = false)
    private String name;
    @Attribute(required = false)
    private String email;
    @Attribute(required = false)
    private String link;

    public Author(final String name, final String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getLink() {
        return link;
    }
}
