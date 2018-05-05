package com.mycompany.john.pickaplace.models;

public class Coordinates {
    private String latitude;
    private String longitude;
    private String message;

    public Coordinates(String latitude, String longitude, String message) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
