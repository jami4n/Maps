package com.jam.maps;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

/**
 * Created by Jam on 01-05-2017.
 */

public class TrackerActivity extends AppCompatActivity implements LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final String TAG = "12345";
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    SupportMapFragment mapFragment;
    GoogleMap googlemap;

    Marker tempmarker,originmarker,destinationmarker;
    Polyline tempPolyline;
    ArrayList<LatLng> locationarray = new ArrayList<>();

    TextView et_origin,et_destination,tv_time_and_distance;
    ImageView img_search;
    LinearLayout ll_distance_matrix_holder;

    DirectionPolyline directionPolyline;
    DistanceMatrix distanceMatrix;

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int locationFor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        et_origin = (TextView) findViewById(R.id.et_origin);
        et_destination = (TextView) findViewById(R.id.et_destination);
        tv_time_and_distance = (TextView) findViewById(R.id.tv_time_and_distance);
        ll_distance_matrix_holder = (LinearLayout) findViewById(R.id.ll_distance_matrix_holder);

        img_search = (ImageView) findViewById(R.id.img_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPolyline();
                getMatrixDistance();
            }
        });


        //test

        et_origin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationFor = 1;
                getPlace();
            }
        });


        et_destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationFor = 2;
                getPlace();
            }
        });

//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                Toast.makeText(TrackerActivity.this, "" + place.getName(), Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(Status status) {
//                Log.d(TAG, "onError: An error occoured");
//            }
//        });

        //end test

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_tracker);
        mapFragment.getMapAsync(this);


        checkForPermission();

    }

    private void getMatrixDistance() {
        ll_distance_matrix_holder.setVisibility(View.INVISIBLE);

        String origin = et_origin.getText().toString().trim();
        String destination = et_destination.getText().toString().trim();

        if(!origin.isEmpty() && !destination.isEmpty()){
            distanceMatrix = new DistanceMatrix(this);
            distanceMatrix.execute(origin,destination);
        }
    }

    public void setDistance(String distanceandtime){
        ll_distance_matrix_holder.setVisibility(View.VISIBLE);
        tv_time_and_distance.setText(distanceandtime);
    }


    public void getPlace(){

        try {
            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setCountry("IN")
                    .build();



            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setFilter(typeFilter)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                //Toast.makeText(this, "" + place.getName(), Toast.LENGTH_SHORT).show();


                if(locationFor == 1){
                    et_origin.setText(place.getName());
                }else if(locationFor == 2){
                    et_destination.setText(place.getName());

                    String et_origin_content = et_origin.getText().toString();
                    if(!et_origin_content.isEmpty()){
                        getPolyline();
                        getMatrixDistance();
                    }
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void setEncodedPolyLine(RouteData routedata){
        if(routedata == null){
            Toast.makeText(this, "Could not get a route.", Toast.LENGTH_SHORT).show();
        }else{
            stopLocationUpdates();
            googlemap.clear();
            locationarray.clear();

            //googlemap.animateCamera(CameraUpdateFactory.newLatLngZoom(routedata.getOriginLocation(),routedata.getZoomLevel()));
            LatLngBounds latlngbounds = new LatLngBounds(routedata.getSouthwestbound(),routedata.getNortheastbound());
            int mapPadding = routedata.getZoomLevel();

            googlemap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngbounds,mapPadding));
            tempPolyline = googlemap.addPolyline(new PolylineOptions()
                    .addAll(PolyUtil.decode(routedata.getPolyline())));

            originmarker = googlemap.addMarker(new MarkerOptions()
                    .title("Start Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapstart))
                    .position(routedata.getOriginLocation()));

            destinationmarker = googlemap.addMarker(new MarkerOptions()
                    .title("Destination Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapend))
                    .position(routedata.getDestinationLocation()));
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void getPolyline(){
        String origin = et_origin.getText().toString().trim();
        String destination = et_destination.getText().toString().trim();

        if(origin.isEmpty()){
            Toast.makeText(this, "Please Enter the source location", Toast.LENGTH_SHORT).show();
        }else if(destination.isEmpty()){
            Toast.makeText(this, "Please Enter the Destination", Toast.LENGTH_SHORT).show();
        }else{
            directionPolyline = new DirectionPolyline(this);
            directionPolyline.execute(origin,destination);
        }
    }

    protected void checkForPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
        }else{
            buildGooleClient();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    buildGooleClient();
                }else{
                    Toast.makeText(this, "You must give permission for location", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void buildGooleClient() {

        if(googleApiClient == null){
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            createLocationRequest();
        }
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);


        //check if GPS is active

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,builder.build());


        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Status status = locationSettingsResult.getStatus();

                switch(status.getStatusCode()){
                    case LocationSettingsStatusCodes.SUCCESS:
                        //all location equip in place. Good to go
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //GPS not activated Start GPS
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        startLocationUpdates();

    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates(){
        if( googleApiClient!= null && googleApiClient.isConnected())
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location.getLatitude());
        Log.d(TAG, "onLocationChanged: " + location.getLongitude());

        if(googlemap != null){
            displayOnMap(location);
        }
    }

    private void displayOnMap(Location location) {
        LatLng current = new LatLng(location.getLatitude(),location.getLongitude());
        locationarray.add(current);

        if(tempmarker != null){
            tempmarker.remove();
        }

        if(tempPolyline != null){
            tempPolyline.remove();
        }

        tempPolyline = googlemap.addPolyline(new PolylineOptions()
                        .addAll(locationarray));

        tempmarker = googlemap.addMarker(new MarkerOptions()
                .position(current)
                .title("Current Location"));

        if(googlemap.getCameraPosition().zoom == 2.0){
            googlemap.animateCamera(CameraUpdateFactory.newLatLngZoom(current,18));
        }else{
            googlemap.animateCamera(CameraUpdateFactory.newLatLng(current));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(googleApiClient != null){
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient != null){
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

            stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if( googleApiClient!= null && googleApiClient.isConnected()){
            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googlemap = googleMap;
        googlemap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style_json));
    }
}
