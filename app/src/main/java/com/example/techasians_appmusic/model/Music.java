package com.example.techasians_appmusic.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Music implements Serializable {
    @SerializedName("url")
    private String path;
    @SerializedName("name")
    private String title;
    @SerializedName("duration")
    private String duration;
    @SerializedName("id")
    private String id;
    private String artist;
    private String album;
    private String cover;
    private int index;

    public Music() {
    }

    public Music(int index, String path, String title, String artist, String album, String duration, String id, String cover) {
        this.index = index;
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.cover = cover;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
