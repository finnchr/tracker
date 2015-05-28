package com.finnchristian.tracker.model.gpx;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class Metadata {
    @Attribute(required = false)
    private String name;
    @Attribute(required = false)
    private String description;
    @Element(required = false)
    private Author author;
    @Attribute(required = false)
    private String keywords;

    public String getName() {
        return name;
    }

    public Metadata setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Author getAuthor() {
        return author;
    }

    public Metadata setAuthor(Author author) {
        this.author = author;
        return this;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
