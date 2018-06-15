package com.jam.maps;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Jam on 03-05-2017.
 */

public class DirectionPolyline extends AsyncTask<String,String,RouteData> {
    private static final String DIRECTION_API_BASE = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String DIRECTION_API_KEY = "AIzaSyBLgUitoJCyISV7JvpOaLDyRJxLZF0z_k4";

    HttpURLConnection conn = null;
    StringBuilder jsonResult = new StringBuilder();
    TrackerActivity a;
    RouteData routedata = null;

    public DirectionPolyline(TrackerActivity a) {
        this.a = a;
    }



    @Override
    protected RouteData doInBackground(String... params) {
        try {

            StringBuilder sb = new StringBuilder(DIRECTION_API_BASE);
            sb.append("origin=" + URLEncoder.encode(params[0],"utf8"));
            sb.append("&destination=" + URLEncoder.encode(params[1],"utf8"));

            Log.d("12345", "doInBackground: " + sb.toString());

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            //loading response in a string
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1){
                jsonResult.append(buff,0,read);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        String polyline = null;

        try {
            JSONObject obj = new JSONObject(jsonResult.toString());
            JSONArray routes = obj.getJSONArray("routes");
            JSONObject overview_polyline = routes.getJSONObject(0).getJSONObject("overview_polyline");
            polyline = overview_polyline.getString("points");

            JSONArray legsarray = routes.getJSONObject(0).getJSONArray("legs");

            JSONObject startlocation = legsarray.getJSONObject(0).getJSONObject("start_location");
            JSONObject endlocation = legsarray.getJSONObject(0).getJSONObject("end_location");


            Double start_lat = Double.valueOf(startlocation.getString("lat"));
            Double start_lon = Double.valueOf(startlocation.getString("lng"));
            Double end_lat = Double.valueOf(endlocation.getString("lat"));
            Double end_lon = Double.valueOf(endlocation.getString("lng"));

            LatLng start_location = new LatLng(start_lat,start_lon);
            LatLng end_location = new LatLng(end_lat,end_lon);

            JSONObject bounds = routes.getJSONObject(0).getJSONObject("bounds");
            JSONObject northeast = bounds.getJSONObject("northeast");
            JSONObject southwest = bounds.getJSONObject("southwest");

            Double northeastlat = Double.valueOf(northeast.getString("lat"));
            Double northeastlng = Double.valueOf(northeast.getString("lng"));
            Double southwestlat = Double.valueOf(southwest.getString("lat"));
            Double southwestlng = Double.valueOf(southwest.getString("lng"));

            LatLng northeast_bound = new LatLng(northeastlat,northeastlng);
            LatLng southwest_bound = new LatLng(southwestlat,southwestlng);


            routedata = new RouteData(start_location,end_location,northeast_bound,southwest_bound,polyline,200);

            Log.d("12345", "returnPolyLine: " + polyline);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return routedata;
    }


    @Override
    protected void onPostExecute(RouteData routedata) {
        super.onPostExecute(routedata);

        a.setEncodedPolyLine(routedata);
    }
}
