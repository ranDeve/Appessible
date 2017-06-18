package com.sagur.pcshortcuts.appessible;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */

public class SearchIntentService extends IntentService {

    public SearchIntentService() {
        super("SearchIntentService");
    }
    String urll;
    final static String keyForAPI = "AIzaSyD30IMWfPWYey8XgYVJa0djQMfv2Tl0wpA";
    @Override
    protected void onHandleIntent(Intent intent) {

        boolean isNearby = intent.getBooleanExtra("isNearby", false);
        String toFind = intent.getStringExtra("placeToFind");
        String latt = intent.getStringExtra("lat");
        String lngg = intent.getStringExtra("lng");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String rad = sharedPreferences.getString("newradius", "10000");
        int newrad = Integer.parseInt(rad);



        //check if the user chose to show only nearby places
        if(isNearby) {
            if((latt!=null)&(lngg!=null)){
            urll = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location="+latt+","+lngg+"&radius="+newrad+"&keyword=" + toFind + "&key="+keyForAPI;}
            else{
                urll ="https://maps.googleapis.com/maps/api/place/textsearch/json?" +
                        "query="+toFind+"&key="+keyForAPI;
            }
        }
        else{
            urll ="https://maps.googleapis.com/maps/api/place/textsearch/json?" +
                    "query="+toFind+"&key="+keyForAPI;
        }



        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(urll).build();
        String result="";
        try {
            Response response = client.newCall(request).execute();
            result= response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JSonResponse jSonResponse= gson.fromJson(result, JSonResponse.class);

        try {
            Intent finishDownloadIntent = new Intent("com.sagur.pcshortcuts.appessible.FINISH");
            finishDownloadIntent.putParcelableArrayListExtra("searchedArray", jSonResponse.results);
            finishDownloadIntent.putExtra("latp", latt);
            finishDownloadIntent.putExtra("lngp", lngg);

            LocalBroadcastManager.getInstance(this).sendBroadcast(finishDownloadIntent);
        }catch (Exception e){

        }

        // SearchFragment is listening to this service





    }

}
