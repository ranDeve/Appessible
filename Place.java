package com.sagur.pcshortcuts.appessible;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Ran on 10/04/2017.
 */

public class Place implements Parcelable {

    String name;
    String formatted_address;
    String vicinity;
    Geometry geometry;
    ArrayList<PlacePhotos> photos;
    OpenOrClossed opening_hours;
    String rating;
    String place_id;


    public Place(String name, String formatted_address ) {
        this.name = name;
        this.formatted_address = formatted_address;
    }


    protected Place(Parcel in) {
        name = in.readString();
        formatted_address = in.readString();
        vicinity = in.readString();
        rating = in.readString();
        place_id = in.readString();
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(formatted_address);
        dest.writeString(vicinity);
        dest.writeString(rating);
        dest.writeString(place_id);
    }
}
