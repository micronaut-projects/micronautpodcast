package com.micronautpodcast;

public class Mp3 {
    private final String title;
    private final String comment;
    private final String album;
    private final String artist;

    public Mp3(String title,
               String comment,
               String album,
               String artist) {
        this.title = title;
        this.comment = comment;
        this.album = album;
        this.artist = artist;
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getAlbum() {
        return album;
    }
}
