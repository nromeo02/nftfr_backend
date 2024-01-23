package org.nftfr.backend.persistence.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class Nft {
    private String id;
    private User author;
    private User owner;
    private String caption;
    private String title;
    private double value;
    private ArrayList<String> tags;

    public Nft() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> inputTags) {
        tags = new ArrayList<>();
        for (String tag : inputTags)
            tags.add(tag.toLowerCase());
    }

    @JsonIgnore
    public String getTagsAsString() {
        return String.join(",", tags);
    }

    public void setTagsFromString(String tagsString) {
        tags = new ArrayList<>();
        for (String tag : tagsString.split(","))
            tags.add(tag.trim().toLowerCase());
    }
}
