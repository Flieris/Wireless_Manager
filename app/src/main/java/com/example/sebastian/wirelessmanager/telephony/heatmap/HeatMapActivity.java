/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony.heatmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.MainActivity;
import com.example.sebastian.wirelessmanager.R;
import com.example.sebastian.wirelessmanager.telephony.Cell;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class HeatMapActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ArrayList<Cell> cellArrayList;
    private TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        cellArrayList = new ArrayList<Cell>();
        test = (TextView)findViewById(R.id.heatmap_test);

        getDataFromFirebase();
        test.setText(cellArrayList.toString());
        System.out.println(cellArrayList.toString());
    }


    private void getDataFromFirebase(){
        databaseReference = FirebaseDatabase.getInstance().getReference("cells");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String cell_id = dataSnapshot.getKey();
                System.out.println(cell_id);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String cell_id = dataSnapshot.getKey();
                System.out.println(cell_id);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String cell_id = dataSnapshot.getKey();
                System.out.println(cell_id);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                String cell_id = dataSnapshot.getKey();
                System.out.println(cell_id);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(HeatMapActivity.this, MainActivity.class));
        finish();
    }
}
