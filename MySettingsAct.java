package com.sagur.pcshortcuts.appessible;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by Ran on 13/04/2017.
 */

public class MySettingsAct extends PreferenceActivity {
    Context context;
    String measur, newRadius;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);

        ListPreference listPreference = (ListPreference)findPreference("list_preference_1");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int measurement = Integer.parseInt(((String)newValue));

                if(measurement==1)
                {
                    measur = "kilo";
                }
                else if(measurement==2)
                {
                    measur = "mile";
                }


                return true;
            }
        });


        EditTextPreference editTextPreference = (EditTextPreference)findPreference("radiusET");
        editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                newRadius = (String)newValue;

                return true;
            }
        });


        Preference deleteFvrts = (Preference)findPreference("delete_favourites");
        deleteFvrts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                        MySqlHelper mySqlHelper = new MySqlHelper(getApplicationContext());
                        mySqlHelper.getWritableDatabase().delete(DBConstants.dbTable, null, null);

                return true;
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("measurement", measur).commit();
        sharedPreferences.edit().putString("newradius", newRadius).commit();
    }
}
