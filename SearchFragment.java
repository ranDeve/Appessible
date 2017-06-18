package com.sagur.pcshortcuts.appessible;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class SearchFragment extends Fragment {

    ArrayList<Place> placesArrayList;
    MyPlacesAdapter myPlacesAdapter;
    RecyclerView recyclerView;
    View rootView;
    MySqlHelper mySqlHelper;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        if(rootView ==null) {


            rootView = inflater.inflate(R.layout.fragment_search, container, false);


            mySqlHelper = new MySqlHelper(getActivity());

            //listening to SearchIntentService
            MyFinishDownloadReciever myFinishDownloadReciever = new MyFinishDownloadReciever();
            IntentFilter filter = new IntentFilter("com.sagur.pcshortcuts.appessible.FINISH");
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(myFinishDownloadReciever, filter);

            placesArrayList = new ArrayList<>();

            if (savedInstanceState != null) {
                placesArrayList = savedInstanceState.getParcelableArrayList("placesArray");
            }



                recyclerView = (RecyclerView) rootView.findViewById(R.id.placesRV);

                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                myPlacesAdapter = new MyPlacesAdapter(placesArrayList);
                recyclerView.setAdapter(myPlacesAdapter);


            // if not connected show previous array
            if(!isNetworkConnected()){

                Cursor cursor = mySqlHelper.getReadableDatabase().query(DBConstants.dbTableSaved, null, null, null, null, null, null);
                while(cursor.moveToNext()){
                    String nameS= cursor.getString(cursor.getColumnIndex(DBConstants.nameColumn));
                    String addressS= cursor.getString(cursor.getColumnIndex(DBConstants.addressColumn));
                    String latt= cursor.getString(cursor.getColumnIndex(DBConstants.latColumn));
                    String lngg= cursor.getString(cursor.getColumnIndex(DBConstants.lngColumn));
                    float lattt = Float.parseFloat(latt);
                    float lnggg = Float.parseFloat(lngg);
                    Place place = new Place(nameS, addressS);
                    Geometry geometry = new Geometry();
                    PlaceLocation placeLocation = new PlaceLocation();
                    placeLocation.lat = lattt;
                    placeLocation.lng = lnggg;
                    geometry.location = placeLocation;
                    place.geometry=geometry;
                    placesArrayList.add(place);
                }
                myPlacesAdapter = new MyPlacesAdapter(placesArrayList);
                recyclerView.setAdapter(myPlacesAdapter);

            }


        }

            else{
                //
                // ((ViewGroup)rootView.getParent()).removeView(rootView);

            }




        return rootView;
    }


    class MyFinishDownloadReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            placesArrayList = intent.getParcelableArrayListExtra("searchedArray");

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            myPlacesAdapter = new MyPlacesAdapter(placesArrayList);
            myPlacesAdapter.lat = intent.getStringExtra("latp");
            myPlacesAdapter.lng = intent.getStringExtra("lngp");
            recyclerView.setAdapter(myPlacesAdapter);

        }
    }




    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //myPlacesAdapter.notifyDataSetChanged();
        outState.putParcelableArrayList("placesArray", placesArrayList);
    }

    @Override
    public void onPause() {
        super.onPause();



        MySqlHelper mySqlHelper = new MySqlHelper(getActivity());


        for(Place place : placesArrayList){

            ContentValues contentValues = new ContentValues();

            contentValues.put(DBConstants.nameColumn, place.name);

            if(place.formatted_address!=null){
                contentValues.put(DBConstants.addressColumn, place.formatted_address);}
            else{
                contentValues.put(DBConstants.addressColumn, place.vicinity);
            }

            contentValues.put(DBConstants.latColumn, place.geometry.location.lat);
            contentValues.put(DBConstants.lngColumn, place.geometry.location.lng);



            mySqlHelper.getWritableDatabase().insert(DBConstants.dbTableSaved, null, contentValues);



        }




    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }




}
