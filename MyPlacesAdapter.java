package com.sagur.pcshortcuts.appessible;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Ran on 10/04/2017.
 */

public class MyPlacesAdapter extends RecyclerView.Adapter<MyPlacesAdapter.MyPlacesVH>  {

    ArrayList<Place> arrayList;
    Context context;
    String lat, lng, mess;
    DecimalFormat df = new DecimalFormat("#.00");
    SharedPreferences sharedPreferences;
    View view1;
    boolean exists;

    public MyPlacesAdapter(ArrayList<Place> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public MyPlacesVH onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mess = sharedPreferences.getString("measurement", "kilo");

        view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        MyPlacesVH myPlacesVH = new MyPlacesVH(view1);

        return myPlacesVH;
    }

    @Override
    public void onBindViewHolder(MyPlacesVH holder, int position) {

        holder.setIsRecyclable(false);
        Place currentPlace = arrayList.get(position);

        holder.binder(currentPlace);

    }

    @Override
    public int getItemCount() {
        try{
        return arrayList.size();}
        catch (Exception e){
            return 0;
        }

    }

    public class MyPlacesVH extends RecyclerView.ViewHolder{

        TextView nameTV;
        TextView addressTV;
        TextView distTV;
        ImageView placeIV, isOpenIV, ratingIV;


        public MyPlacesVH(View itemView) {
            super(itemView);

            nameTV = (TextView)itemView.findViewById(R.id.placeNameTV);
            addressTV = (TextView)itemView.findViewById(R.id.placeAddress);
            placeIV = (ImageView)itemView.findViewById(R.id.placeIV);
            distTV = (TextView)itemView.findViewById(R.id.distanceTV);
            isOpenIV = (ImageView)itemView.findViewById(R.id.isOpenIV);
            ratingIV = (ImageView)itemView.findViewById(R.id.ratingIV);

        }
        public void binder(final Place place){
            nameTV.setText(""+place.name);


            // replace any NULL text to "" String
            String formattedadress = ""+place.formatted_address;
            if (formattedadress.contains("null")){
                formattedadress = formattedadress.replace("null", " ");
            }
            String vicinitytext = ""+place.vicinity;
            if (vicinitytext.contains("null")){
                vicinitytext = formattedadress.replace("null", " ");
            }




            addressTV.setText(""+formattedadress);
            addressTV.append(""+vicinitytext);


            if(place.opening_hours!=null) {
                if (place.opening_hours.open_now.equals("true")) {
                    isOpenIV.setImageResource(R.drawable.open);
                } else {
                    isOpenIV.setImageResource(R.drawable.clossed);
                }
            }

            if((lat!=null)&(lng!=null)) {

                double lt = Double.parseDouble(lat);
                double ln = Double.parseDouble(lng);

                double myDist = haversine(lt, ln, place.geometry.location.lat, place.geometry.location.lng);


                if (mess.equals("kilo")) {
                    distTV.setText(df.format(myDist) + " km");
                } else if (mess.equals("mile")) {

                    myDist = myDist / 1.61;

                    distTV.setText(df.format(myDist) + " mi");
                }
            }


            //show picture if exists
            try {
                String g = place.photos.get(0).photo_reference;
                String picURL = "https://maps.googleapis.com/maps/api/place/photo?" +
                        "maxwidth=400&photoreference=" + g + "&key="+SearchIntentService.keyForAPI;
                Picasso.with(context).load(picURL).into(placeIV);
            }
            catch (Exception e){
                //some places don't have any photos

            }



            //rating image

            if(place.rating!=null) {
                double rats = (Double.parseDouble(place.rating));
                int rats1 = (int)rats;

                switch (rats1) {
                    case 1:
                        ratingIV.setImageResource(R.drawable.firststar);
                        break;
                    case 2:
                        ratingIV.setImageResource(R.drawable.secondstar);
                        break;
                    case 3:
                        ratingIV.setImageResource(R.drawable.thirdstar);
                        break;
                    case 4:
                        ratingIV.setImageResource(R.drawable.forthstar);
                        break;
                    case 5:
                        ratingIV.setImageResource(R.drawable.fithstar);
                        break;

                }
            }


            view1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentChangerInterface fragmentChangerInterface = (FragmentChangerInterface)context;
                    fragmentChangerInterface.changeFragments(place);


                }
            });
            view1.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(place.name)
                        .setIcon(R.drawable.icons)
                            .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("text/plain");
                                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "I like "+place.name+"!");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, place.formatted_address);
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Via"));


                                }
                            })
                            .setNegativeButton("Add to Favourites", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {


                                    if(!(exists(place))) {
                                        MySqlHelper mySqlHelper = new MySqlHelper(context);

                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(DBConstants.nameColumn, place.name);

                                        if(place.formatted_address!=null){
                                        contentValues.put(DBConstants.addressColumn, place.formatted_address);}
                                        else{
                                            contentValues.put(DBConstants.addressColumn, place.vicinity);
                                        }



                                        mySqlHelper.getWritableDatabase().insert(DBConstants.dbTable, null, contentValues);

                                    }
                                    else {
                                        Toast.makeText(context, place.name + " is already exists!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).setNeutralButton("Dial "+place.name, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                            Intent intent = new Intent(context, PhoneAndSiteServer.class);
                            intent.putExtra("placid", place.place_id);
                            //go to PhoneandsiteService to get the JSon
                            context.startService(intent);}catch (Exception c){};

                        }
                    });
                    builder.show();



                    return true;
                }
            });

        }



        public double haversine(double lat1, double lng1, double lat2, double lng2) {


            int r = 6371; // average radius of the earth in km



            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double d = r * c;
            return d;
        }

        public boolean exists (Place place){

            //a boolean method that checks whether movie subject already exists
            String[] columns = { DBConstants.nameColumn };
            String selection = DBConstants.nameColumn + " =?";
            String[] selectionArgs = { place.name };
            String limit = "1";
            MySqlHelper mySqlHelper = new MySqlHelper(context);
            Cursor cursor = mySqlHelper.getReadableDatabase().query(DBConstants.dbTable, columns, selection, selectionArgs, null, null, null, limit);
            exists = (cursor.getCount() > 0);
            cursor.close();
            return exists;
        }




    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }



}
