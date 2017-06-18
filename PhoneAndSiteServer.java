package com.sagur.pcshortcuts.appessible;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PhoneAndSiteServer extends IntentService {


    public PhoneAndSiteServer() {
        super("PhoneAndSiteServer");
    }

    String phone;

    @Override
    protected void onHandleIntent(Intent intent) {
        String placeID = intent.getStringExtra("placid");
        String urll="https://maps.googleapis.com/maps/api/place/details/json?placeid="+placeID+"&key="+SearchIntentService.keyForAPI;

        StringBuilder response= null;
        try{
            URL website = new URL(urll);



            URLConnection connection = website.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
        } catch(Exception ee)
        {            }

        JSONObject mainObject= null;
        try {
            mainObject = new JSONObject(response.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONObject currentObj = mainObject.getJSONObject("result");
            phone = currentObj.getString("formatted_phone_number");
        } catch (JSONException e) {
            e.printStackTrace();
        }


            try {
            Intent finishDownloadIntent = new Intent("com.sagur.pcshortcuts.appessible.HASPHONE");
            finishDownloadIntent.putExtra("phonenum", phone);

            LocalBroadcastManager.getInstance(this).sendBroadcast(finishDownloadIntent);
        }catch (Exception e){

        }


    }

}
