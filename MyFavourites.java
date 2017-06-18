package com.sagur.pcshortcuts.appessible;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MyFavourites extends AppCompatActivity {
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favourites);

        final MySqlHelper mySqlHelper = new MySqlHelper(this);


        cursor = mySqlHelper.getReadableDatabase().query(DBConstants.dbTable, null, null, null, null, null, null);


        final ListView listView = (ListView)findViewById(R.id.myFavouritesLV);
        String[] fromcolumns = new String[]{DBConstants.nameColumn, DBConstants.addressColumn};
        int[] toColumns = new int[]{android.R.id.text1, android.R.id.text2};
        final SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, fromcolumns, toColumns);
        listView.setAdapter(simpleCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                cursor.moveToPosition(position);
                AlertDialog.Builder alert = new AlertDialog.Builder(MyFavourites.this);
                alert.setTitle("Are you sure you want to delete this item?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int currentID= cursor.getInt( cursor.getColumnIndex("_id"));
                        mySqlHelper.getWritableDatabase().delete(DBConstants.dbTable , "_id=?" , new String[]{ ""+currentID }  );
                        cursor = mySqlHelper.getReadableDatabase().query(DBConstants.dbTable, null, null, null,null,null,null);
                        simpleCursorAdapter.swapCursor(cursor);

                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();


            }
        });

    }
}
