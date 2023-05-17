package com.example.bitelens;

public class Photo {
    private String thumb;
    private String highres;
    private boolean is_user_uploaded;

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getHighres() {
        return highres;
    }

    public void setHighres(String highres) {
        this.highres = highres;
    }

    public boolean isIs_user_uploaded() {
        return is_user_uploaded;
    }

    public void setIs_user_uploaded(boolean is_user_uploaded) {
        this.is_user_uploaded = is_user_uploaded;
    }
}
