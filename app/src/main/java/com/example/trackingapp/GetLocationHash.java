package com.example.trackingapp;

import android.os.AsyncTask;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


import com.google.android.gms.maps.model.LatLng;

public class GetLocationHash extends AsyncTask <LatLng, Void, String> {
    @Override
    protected String doInBackground(LatLng... latLngs) {

        String LocationString = latLngs[0].toString();
        System.out.println("create context...");
        ZContext context = new ZContext(1);
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.connect("tcp://localhost:7777");
        System.out.println("start sending request...");

        byte[] request = LocationString.getBytes();
        socket.send(request,0);
        byte[] response = socket.recv(0);

        socket.close();
        context.close();

        System.out.println("sending request done...");
        return new String(response);
    }
}
