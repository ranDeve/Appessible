package com.sagur.pcshortcuts.appessible;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.URI;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener, FragmentChangerInterface {

    //Global variables
    EditText searchPlaceET;
    ImageButton searchBtn;
    CheckBox isNearbyCB;
    LocationManager locationManager;
    String lat, lng;
    ProgressDialog dialog;
    SearchFragment searchFragment;
    boolean isasked;
    MapFragment mapFragmnent= new MapFragment();
    SharedPreferences sharedPreferences;
    String currentPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //plumbing
        isNearbyCB = (CheckBox) findViewById(R.id.isNearbyCB);
        isNearbyCB.setChecked(true);
        searchPlaceET = (EditText) findViewById(R.id.searchPlaceET);
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);


        if(savedInstanceState!=null){
            isasked = savedInstanceState.getBoolean("asked", false);

        }

            //initialize the Search fragment
            searchFragment = new SearchFragment();

            FragmentTransaction addTransaction = getFragmentManager().beginTransaction();
            addTransaction.addToBackStack("adding transaction");
            if(isasked==true){

                //different settings for large screen after rotation
                int screenSize = getResources().getConfiguration().screenLayout &
                        Configuration.SCREENLAYOUT_SIZE_MASK;

                if(screenSize== Configuration.SCREENLAYOUT_SIZE_LARGE){


                    android.app.Fragment f = getFragmentManager().findFragmentById(R.id.fragL);
                if (f instanceof SearchFragment){

                }
                else{
                    addTransaction.replace(R.id.fragL, searchFragment).commit();

                }





            }
            //addTransaction.replace(R.id.fragL, searchFragment).commit();
                                }
            else{
                addTransaction.replace(R.id.fragL, searchFragment).commit();}


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        //check if permission for location service is granted

            if(!isasked){
            checkPermissionIfChecked();


                String findPlace = sharedPreferences.getString("lastSearch", null);
                if(findPlace!=null){
                searchAtStart(findPlace);}

            }
            if(isasked){
                lat = savedInstanceState.getString("oldlat");
                lng = savedInstanceState.getString("oldlng");

            }


            //check if user clicks on the Nearby-places checkbox
            isNearbyCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {

                        searchPlaceET.setHint("enter a place (e.g. Pizza, Books, etc.)");
                        if(!isasked){

                            checkPermissionIfChecked();}


                    } else {
                        isNearbyCB.setChecked(false);
                        searchPlaceET.setHint("enter a place (e.g. Pizza in Tel-Aviv, etc.)");

                    }
                }
            });


            //when search button's clicked, start the OkHttp service
            searchBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String placeToFind = searchPlaceET.getText().toString();

                    //hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    Intent intent = new Intent(MainActivity.this, SearchIntentService.class);
                    intent.putExtra("placeToFind", placeToFind);
                    intent.putExtra("isNearby", isNearbyCB.isChecked());
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    //go to SearchService to get the JSon
                    startService(intent);
                }
            });

            //check if battery power is connected/disconnected
            MyBatteryReciver myBatteryReciver = new MyBatteryReciver();
            IntentFilter intentFilter = new IntentFilter("android.intent.action.ACTION_POWER_CONNECTED");
            IntentFilter intentFilter2 = new IntentFilter("android.intent.action.ACTION_POWER_DISCONNECTED");
            registerReceiver(myBatteryReciver, intentFilter);
            registerReceiver(myBatteryReciver, intentFilter2);

            //check network connectivity
            NetworkStateReceiver networkReceiver = new NetworkStateReceiver();
            IntentFilter intent3 = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkReceiver, intent3);


        MainActivity.FinishedPhoneServiceReicever myFinishPhone = new MainActivity.FinishedPhoneServiceReicever();
        IntentFilter filter4 = new IntentFilter("com.sagur.pcshortcuts.appessible.HASPHONE");
        LocalBroadcastManager.getInstance(this).registerReceiver(myFinishPhone, filter4);


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //update rotation data
        outState.putBoolean("asked", true);
        outState.putString("oldlat", lat);
        outState.putString("oldlng", lng);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(getFragmentManager().getBackStackEntryCount()==1){
            finish();
        }
        else {
            getFragmentManager().popBackStack();
        }

    }

    //check if permission is granted after the user clicks on the check-box
    public void checkPermissionIfChecked(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {

            startGPS();


        } else {
            //request permission 25 is the request number
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 25);
        }

    }


    private void startGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //getting GPS status
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, MainActivity.this);
            dialog = new ProgressDialog(this);
            dialog.setMessage("Updating Your Location (GPS)!!");
            dialog.setCancelable(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.show();
        }
        //getting network status
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 100, MainActivity.this);
            dialog = new ProgressDialog(this);
            dialog.setMessage("Updating Your Location (NETWORK)!!");
            dialog.setCancelable(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.show();

        } else {

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location!=null){

            lat = "" + location.getLatitude();
            lng = "" + location.getLongitude();
            Toast.makeText(this, lat, Toast.LENGTH_SHORT).show();
            locationManager.removeUpdates(this);}
            else {
                Toast.makeText(this, "Can not find your location", Toast.LENGTH_SHORT).show();
                isNearbyCB.setChecked(false);
            }
        }


    }




    @Override
    public void onLocationChanged(Location location) {

        lat= ""+location.getLatitude();
        lng= ""+location.getLongitude();
        //Toast.makeText(this, lat, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Found your location", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==25)
        {            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startGPS();
        }
        else
        {                Toast.makeText(MainActivity.this, "You must open GPS", Toast.LENGTH_SHORT).show();
        }        }


}

    @Override
    public void changeFragments(final Place currentPlace) {



        mapFragmnent= new MapFragment();

        FragmentTransaction replaceTransaction = getFragmentManager().beginTransaction();
        replaceTransaction.addToBackStack("replace to map fragment");

        LinearLayout mapLayout = (LinearLayout)findViewById(R.id.mapContainer);


        if(mapLayout==null){

            replaceTransaction.replace(R.id.fragL, mapFragmnent).commit();

        }
        else{
            replaceTransaction.replace(R.id.mapContainer, mapFragmnent).commit();

        }













        //map configurations
        mapFragmnent.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LatLng latLng= new LatLng(currentPlace.geometry.location.lat , currentPlace.geometry.location.lng );
                CameraUpdate update= CameraUpdateFactory.newLatLngZoom(latLng, 17);
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng)    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(""+currentPlace.name)).showInfoWindow();

                if((lat!=null)&(lng!=null)) {
                    LatLng latLng1 = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                googleMap.addMarker(new MarkerOptions()
                        .position(latLng1)    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).title("I'm Here "+currentPhone)).showInfoWindow();}




                //draw a path between two locations
                if((lat!=null)&(lng!=null)) {

                    ArrayList<Location> listLocsToDraw = new ArrayList<Location>();
                    Location here = new Location("");
                    here.setLatitude(Double.parseDouble(lat));
                    here.setLongitude(Double.parseDouble(lng));
                    Location there = new Location("");
                    there.setLatitude(Double.parseDouble("" + currentPlace.geometry.location.lat));
                    there.setLongitude(Double.parseDouble("" + currentPlace.geometry.location.lng));
                    listLocsToDraw.add(here);
                    listLocsToDraw.add(there);

                    if (googleMap == null) {
                        return;
                    }

                    if (listLocsToDraw.size() < 2) {
                        return;
                    }

                    PolylineOptions options = new PolylineOptions();
                    options.color(Color.parseColor("#CC0000FF"));
                    options.width(3);
                    options.visible(true);

                    for (Location locRecorded : listLocsToDraw) {
                        options.add(new LatLng(locRecorded.getLatitude(),
                                locRecorded.getLongitude()));
                    }

                    googleMap.addPolyline(options);

                }

                googleMap.moveCamera(update);

                if(!isNetworkConnected()){
                    Toast.makeText(MainActivity.this, "There's no network connectivity", Toast.LENGTH_SHORT).show();
                }

            }        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.settingitem){
            Intent intent = new Intent(MainActivity.this, MySettingsAct.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.favItem){
            Intent intent = new Intent(MainActivity.this, MyFavourites.class);
            startActivity(intent);
        }




        return true;
    }


    class MyBatteryReciver extends BroadcastReceiver{
            //toast when power connected/disconnected
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
                Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                Toast.makeText(context, "Power Disconnected", Toast.LENGTH_SHORT).show();
            }

        }
    }

    //check internet connection
    public class NetworkStateReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if(intent.getExtras()!=null) {
                NetworkInfo ni=(NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {

                    //Toast.makeText(context, "Network "+ni.getTypeName()+" connected", Toast.LENGTH_SHORT).show();
                }
            }
            if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
                Toast.makeText(context, "There's no network connectivity", Toast.LENGTH_SHORT).show();
            }
        }
    }




    @Override
    protected void onPause() {
        super.onPause();

        if(locationManager!=null){
        locationManager.removeUpdates(this);}


        String placeLastSearched = searchPlaceET.getText().toString();

        sharedPreferences.edit().putString("lastSearch", placeLastSearched).commit();



    }

    //check whether mobile is connected to internet and returns true if connected
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


    public void searchAtStart(String placeToFind){


        //hide the keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

        Intent intent = new Intent(MainActivity.this, SearchIntentService.class);
        intent.putExtra("placeToFind", placeToFind);
        intent.putExtra("isNearby", isNearbyCB.isChecked());
        intent.putExtra("lat", lat);
        intent.putExtra("lng", lng);
        //go to SearchService to get the JSon
        startService(intent);

    }


    class FinishedPhoneServiceReicever extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {
            String currentPhone = intent.getStringExtra("phonenum");
            //Dialog dialog = new Dialog(getActivity());
            //dialog.setTitle("tel:"+currentPhone);
            //dialog.show();


            try{
            String prepared = ""+Integer.parseInt(currentPhone.replaceAll("[\\D]", ""));


            //Intent intent2 = new Intent(Intent.ACTION_DIAL);
            //intent.setData(Uri.parse("tel:"+prepared));
            //startActivity(intent2);


            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:"+Uri.encode(prepared.trim())));
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(callIntent);}catch (Exception e){};

        }
    }


}
