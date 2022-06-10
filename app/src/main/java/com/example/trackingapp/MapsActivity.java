package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.trackingapp.databinding.ActivityMapsBinding;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.channels.AsynchronousByteChannel;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Location> savedLocations;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationApplication locationApplication = (LocationApplication) getApplicationContext();

        savedLocations = locationApplication.getLocationList();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for(Location location : savedLocations){
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions marker = new MarkerOptions();
            marker.position(latLng);
            marker.title("Lat: " + location.getLatitude() + " Lon: " + location.getLongitude());
            mMap.addMarker(marker);
        }
        Location  lastLocation = savedLocations.get(savedLocations.size() -1);
        LatLng lastLocationLatLgn = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLgn,12.0f));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                GetLocationHash getHash = new GetLocationHash();
                getHash.execute(marker.getPosition());
                return false;
            }
        });
    }

    private class GetLocationHash extends AsyncTask<LatLng, Void,String>{
        @Override
        protected String doInBackground(LatLng... latLngs) {

            String LocationString = latLngs[0].toString();
            System.out.println("create context...");
            ZContext context = new ZContext(1);
            ZMQ.Socket socket = context.createSocket(SocketType.REQ);
            socket.connect("tcp://192.168.20.11:7777");
            System.out.println("start sending request...");

            byte[] request = LocationString.getBytes();
            socket.send(request,0);
            byte[] response = socket.recv(0);
            System.out.println("received from server..." + new String(response));
            socket.close();
            context.close();

            System.out.println("finished task...");
            return new String(response);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(MapsActivity.this, result, Toast.LENGTH_LONG).show();
        }
    }
}