package com.example.trackingapp;

import android.app.Application;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationApplication extends Application {
    private static LocationApplication application;
    private List<Location> list;

    public LocationApplication getInstance (){
        return application;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        application = this;
        application.initialized();
    }

    private void initialized() {
        list = new ArrayList<>();
    }

    public List<Location> getLocationList (){
        return list;
    }
}
