package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {

    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final int MIN_TIME_UPDATE = 2 * 1000;
    public static final int MIN_DISTANCE_UPDATE = 1;
    private TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address, tv_wayPointCount;
    private Button btn_newWayPoint, btn_showWayPointList, btn_showMap;
    private Switch sw_locationsupdates, sw_gps;
    private boolean wantLocationUpdates;
    private static final String UPDATES_BUNDLE_KEY
            = "WantsLocationUpdates";
    private String provider = LocationManager.GPS_PROVIDER;

    private Location currentLocation;

    private List<Location> savedLocations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_wayPointCount = findViewById(R.id.tv_countOfPoints);

        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        btn_newWayPoint = findViewById(R.id.btn_newWayPoint);
        btn_showWayPointList = findViewById(R.id.btn_showWayPointList);
        btn_showMap = findViewById(R.id.btn_showMap);



        if (savedInstanceState != null
                && savedInstanceState.containsKey(UPDATES_BUNDLE_KEY))
            wantLocationUpdates
                    = savedInstanceState.getBoolean(UPDATES_BUNDLE_KEY);
        else // activity is not being reinitialized from prior start
            wantLocationUpdates = false;

        if (!hasLocationPermission())
        {
            permissionDeniedNotification();
            Log.w(MainActivity.class.getName(),
                    "Location permissions denied");
        }

        btn_newWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the GPS location the add to the global list

                LocationApplication locationApplication = (LocationApplication) getApplicationContext();
                savedLocations = locationApplication.getLocationList();
                savedLocations.add(currentLocation);
                tv_wayPointCount.setText(Integer.toString(savedLocations.size()));
            }
        });

        btn_showWayPointList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ShowSavedLocationList.class);
                startActivity(i);
            }
        });

        btn_showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
            }
        });

        sw_locationsupdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsupdates.isChecked()) {
                    startGPS();
                } else {
                    stopGPS();
                }

            }
        });

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sw_gps.isChecked()){
                    tv_sensor.setText(R.string.sensor_GPS);
                    provider = LocationManager.GPS_PROVIDER;
                } else {
                    tv_sensor.setText(R.string.sensor_Network);
                    provider = LocationManager.NETWORK_PROVIDER;
                }
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (wantLocationUpdates
                && permissionCheck == PackageManager.PERMISSION_GRANTED)
            startGPS();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // stop location updates while the activity is paused
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
            stopGPS();
    }

    private void startGPS()
    {
        tv_updates.setText("Location is being tracked");

        try
        {
            LocationManager locationManager = (LocationManager)
                    this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(provider,MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE,this);
            Location lastKnownLocation
                    = locationManager.getLastKnownLocation(provider);
            if (lastKnownLocation != null)
                updateUIValues(lastKnownLocation);
                currentLocation = lastKnownLocation;
        }
        catch (SecurityException e)
        {
            permissionDeniedNotification();
            Log.w(MainActivity.class.getName(),
                    "Security Exception: " + e);
        }
    }

    private void stopGPS()
    {
        LocationManager locationManager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateUIValues(location);
        Log.i(MainActivity.class.getName(), "Location: "+location);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putBoolean(UPDATES_BUNDLE_KEY, wantLocationUpdates);
    }

    private boolean hasLocationPermission()
    {
        int permissionCheck = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.request_permission_title)
                        .setMessage(R.string.request_permission_text)
                        .setPositiveButton(
                                R.string.request_permission_positive,
                                (dialogInterface, i) -> ActivityCompat.requestPermissions
                                        (MainActivity.this,new String[]
                                                        {Manifest.permission.
                                                                ACCESS_FINE_LOCATION},
                                                PERMISSION_REQUEST_CODE))
                        .create()
                        .show();
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_CODE);
            }
            return false;
        }
    }

    private void updateUIValues(Location location) {
        // update all the text fields
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText(R.string.NotAvailable);
        }

        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else {
            tv_speed.setText(R.string.NotAvailable);
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e){
            tv_address.setText("Unable to get street address");
            e.printStackTrace();
        }

    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location is NOT being tracked");
        tv_lat.setText(R.string.NoTracking);
        tv_lon.setText(R.string.NoTracking);
        tv_speed.setText(R.string.NoTracking);
        tv_address.setText(R.string.NoTracking);
        tv_accuracy.setText(R.string.NoTracking);
        tv_altitude.setText(R.string.NoTracking);
    }

    private void permissionDeniedNotification(){
        tv_updates.setText("Require Permission to run location tracking");
        tv_lat.setText(R.string.permissions_denied);
        tv_lon.setText(R.string.permissions_denied);
        tv_speed.setText(R.string.permissions_denied);
        tv_address.setText(R.string.permissions_denied);
        tv_accuracy.setText(R.string.permissions_denied);
        tv_altitude.setText(R.string.permissions_denied);
    }
}