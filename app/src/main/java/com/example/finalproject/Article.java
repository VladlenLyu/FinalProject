package com.example.finalproject;

import java.io.Serializable;

public class Article implements Serializable {
    public String title, description, pubDate, link;

    public Article(String title, String description, String pubDate, String link) {
        this.title = title;
        this.description = description;
        this.pubDate = pubDate;
        this.link = link;
    }

    @Override
    public String toString() {
        return title;
    }
}