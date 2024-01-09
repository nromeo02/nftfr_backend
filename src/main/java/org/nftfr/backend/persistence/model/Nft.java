package org.nftfr.backend.persistence.model;
import java.util.ArrayList;

public class Nft {
    private String id;
    private String author;
    private String owner;
    private String caption;
    private String title;
    private double value;
    private ArrayList<String> tag;

    public Nft(String id, String author, String owner, String caption, String title, double value, ArrayList<String> tag) {
        this.id = id;
        this.author = author;
        this.owner = owner;
        this.caption = caption;
        this.title = title;
        this.value = value;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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

    public ArrayList<String> getTag() {
        return tag;
    }

    public void setTag(ArrayList<String> tag) {
        this.tag = tag;
    }
    public void addTag(String tag){
        this.tag.add(tag);
    }
}
