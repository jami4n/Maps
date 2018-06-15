package com.jam.maps;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Jam on 04-05-2017.
 */

public class DistanceMatrix extends AsyncTask<String,String,String> {


    private String URL_BASE = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&";
    HttpURLConnection conn;
    StringBuilder jsonResult = new StringBuilder();
    String distanceMatrix = null;
    TrackerActivity activity;

    public DistanceMatrix(TrackerActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {

        try {

            StringBuilder sb = new StringBuilder(URL_BASE);
            sb.append("origins=" + URLEncoder.encode(params[0],"utf8"));
            sb.append("&destinations=" + URLEncoder.encode(params[1],"utf8"));


            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();

            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1){
                jsonResult.append(buff,0,read);
            }

            Log.d("123456789", "doInBackground: " + jsonResult);

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            JSONObject obj = new JSONObject(jsonResult.toString());
            JSONArray rows = obj.getJSONArray("rows");
            JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
            JSONObject distance = elements.getJSONObject(0).getJSONObject("distance");
            JSONObject duration = elements.getJSONObject(0).getJSONObject("duration");

            distanceMatrix = distance.getString("text") + " (" + duration.getString("text") + ")";

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return distanceMatrix;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        activity.setDistance(s);
    }
}
