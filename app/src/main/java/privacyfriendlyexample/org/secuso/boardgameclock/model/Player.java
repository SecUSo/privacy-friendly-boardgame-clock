package privacyfriendlyexample.org.secuso.boardgameclock.model;

import android.net.Uri;

import privacyfriendlyexample.org.secuso.boardgameclock.R;

public class Player {

    private long id;
    private String name;
    private String photoUri;

    public Player(int id, String name, String photoUri) {
        this.id = id;
        this.name = name;
        this.photoUri = photoUri;
    }

    public Player() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

}
