package com.mycompany.john.pickaplace.models;

public class CoordinatesExtended extends Coordinates{
    private int user_id;

    public CoordinatesExtended(String latitude, String longitude,
                               String message, int user_id) {
        super(latitude, longitude, message);
        this.user_id = user_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
