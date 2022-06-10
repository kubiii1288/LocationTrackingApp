package com.example.trackingapp;

import android.os.AsyncTask;
import android.widget.Toast;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


import com.google.android.gms.maps.model.LatLng;

public class GetLocationHash extends AsyncTask <LatLng, Void, String> {
    private String hashcode;
    public GetLocationHash(String hash){
        this.hashcode = hash;
    }
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
        hashcode = result;
    }
}
