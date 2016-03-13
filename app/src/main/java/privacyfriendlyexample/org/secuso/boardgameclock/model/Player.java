package privacyfriendlyexample.org.secuso.boardgameclock.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Player {

    private long id;
    private long date;
    private String dateString;
    private String name;
    private String photoUri;

    public Player(long id, long date, String name, String photoUri) {
        this.id = id;
        this.date = date;
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

    @Override
    public String toString(){
        return name;
    }

    public void setDate(long date) {
        this.date = date;

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy kk:mm");
        dateString = formatter.format(new Date(date));
    }

    public String getDateString(){
        return dateString;
    }

}
