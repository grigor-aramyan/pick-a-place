package com.mycompany.john.pickaplace.models;

public class MyCustomLocation {
    private Coordinates location;

    public MyCustomLocation(Coordinates location) {
        this.location = location;
    }

    public Coordinates getLocation() {
        return location;
    }

    public void setLocation(Coordinates location) {
        this.location = location;
    }
}
