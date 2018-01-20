/*
 * Copyright (c) Sebastian Lenkiewicz 2018.
 */

package com.example.sebastian.wirelessmanager.telephony.heatmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sebastian.wirelessmanager.MainActivity;
import com.example.sebastian.wirelessmanager.R;
import com.example.sebastian.wirelessmanager.telephony.Cell;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HeatMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapLoadedCallback {
    //Ui components:
    private TextView operatorTv;
    private RadioButton lteRadio, umtsRadio, heatmapRadio, convexRadio, concaveRadio, clusterRadio, pilotRadio;
    private CardView mapOptions;
    //Firebase reference and data:
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    //GoogleMap and Location:
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    //heat map:
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    int colors[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        operatorTv = findViewById(R.id.map_network_operator);
        lteRadio = findViewById(R.id.type_lte);
        umtsRadio = findViewById(R.id.type_umts);
        heatmapRadio = findViewById(R.id.type_heatmap);
        convexRadio = findViewById(R.id.type_convex);
        concaveRadio = findViewById(R.id.type_concave);
        clusterRadio = findViewById(R.id.type_cluster);
        pilotRadio = findViewById(R.id.type_best_pilot);
        mapOptions= findViewById(R.id.map_options);
        mapOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                RelativeLayout optionsLayout = findViewById(R.id.options_layout);
                if (optionsLayout.getVisibility() == View.GONE) {
                    optionsLayout.setVisibility(View.VISIBLE);
                } else {
                    optionsLayout.setVisibility(View.GONE);
                }
            }
        });
        colors = getApplicationContext().getResources().getIntArray(R.array.cellRainbow);
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        operatorTv.setText(mTelephony.getNetworkOperatorName());
        setRadioButtons();
        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onStart(){
        super.onStart();
    }
    @Override
    protected void onStop(){
        super.onStop();
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDataFromFirebase();
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            getDeviceLocation();
        }
        //final LayoutInflater factory = getLayoutInflater();
        //final View telephonyView = factory.inflate(R.layout.fragment_telephony,null);
        //MyPhoneStateListener phoneStateListener = new MyPhoneStateListener(this.getApplicationContext(),telephonyView);
        //phoneStateListener.start();
        mMap.setOnMyLocationButtonClickListener(this);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(HeatMapActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapLoaded() {
        if (ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
            Location location = LocationServices.getFusedLocationProviderClient(this).getLastLocation().getResult();
            if (location != null && mMap != null){
                mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude())));
            }
        }
    }

    private void getDeviceLocation(){
        try {
            if (ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()),DEFAULT_ZOOM));

                        } else{
                            Log.e("ABC", "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void addHeatMap(){
        List<WeightedLatLng> list = null;
        getDataFromFirebase();

    }

    private void getDataFromFirebase() {
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final int mcc = Integer.parseInt(mTelephony.getNetworkOperator().substring(0, 3));
        final String operator = mTelephony.getNetworkOperatorName();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // database.setPersistenceEnabled(true);
        RadioGroup cellSelection = findViewById(R.id.cell_selection);
        int id = cellSelection.getCheckedRadioButtonId();
        String type;
        switch(id){
            case R.id.type_lte:
                type = "LTE";
                break;
            case R.id.type_umts:
                type = "UMTS";
                break;
            default:
                // if somehow none is selected let's just break the app
                type = "UNKNOWN";
                break;
        }
        mDatabase = database.getReference(mcc + "/" + operator + "/" + type);
        ValueEventListener cellListener = new ValueEventListener() {
            ArrayList<WeightedLatLng> heatmapList = new ArrayList<>();
            ArrayList<PolygonOptions> concaveList = new ArrayList<>();
            ArrayList<PolygonOptions> convexList = new ArrayList<>();
            ArrayList<PolygonOptions> clusterList = new ArrayList<>();
            ArrayList<PolygonOptions> pilotList = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cellSnapshot : dataSnapshot.getChildren()) {
                    int cid = Integer.parseInt(cellSnapshot.getKey()) % colors.length;
                    int argb = colors[cid];
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    int strokeColor = Color.argb(0xFF,  red, green, blue);
                    int fillColor = Color.argb(0xFF/2,  red, green, blue);
                    for (DataSnapshot pointSnapshot : cellSnapshot.getChildren()) {
                        if (pointSnapshot.getKey().equalsIgnoreCase("cluster")) {
                            for (DataSnapshot borderSnapshot : pointSnapshot.getChildren()) {
                                PolygonOptions clusterOptions = new PolygonOptions().geodesic(true).strokeColor(strokeColor).fillColor(fillColor);
                                HashMap<String, Object> map;
                                for (DataSnapshot clusterSnapshot : borderSnapshot.getChildren()) {
                                    map = (HashMap<String, Object>) clusterSnapshot.getValue();
                                    LatLng point = new LatLng((double) map.get("latitude"), (double) map.get("longitude"));
                                    clusterOptions.add(point);
                                }
                                clusterList.add(clusterOptions);
                            }
                            continue;
                        } else if (pointSnapshot.getKey().equalsIgnoreCase("concaveborder")) {
                            PolygonOptions concaveOptions = new PolygonOptions().geodesic(true).strokeColor(strokeColor).fillColor(fillColor);
                            HashMap<String, Object> map;
                            for (DataSnapshot concaveSnapshot : pointSnapshot.getChildren()) {
                                map = (HashMap<String, Object>) concaveSnapshot.getValue();
                                LatLng point = new LatLng((double) map.get("latitude"), (double) map.get("longitude"));
                                concaveOptions.add(point);
                            }
                            concaveList.add(concaveOptions);
                        } else if (pointSnapshot.getKey().equalsIgnoreCase("convexborder")) {
                            PolygonOptions convexOptions = new PolygonOptions().geodesic(true).strokeColor(strokeColor).fillColor(fillColor);
                            HashMap<String, Object> map;
                            for (DataSnapshot convexSnapshot : pointSnapshot.getChildren()) {
                                map = (HashMap<String, Object>) convexSnapshot.getValue();
                                LatLng point = new LatLng((double) map.get("latitude"), (double) map.get("longitude"));
                                convexOptions.add(point);
                            }
                            convexList.add(convexOptions);
                        } else {
                            Cell cell = pointSnapshot.getValue(Cell.class);
                            double lat = cell.latitude;
                            double lng = cell.longitude;
                            double dBm = 200 + cell.signalStrength;
                            LatLng point = new LatLng(lat, lng);
                            PolygonOptions polygonOptions = new PolygonOptions().geodesic(true).fillColor(fillColor).strokeWidth(0);
                            ArrayList<LatLng> corners = findCorners(point);
                            polygonOptions.addAll(corners);
                            pilotList.add(polygonOptions);
                            WeightedLatLng weightedPoint = new WeightedLatLng(point, dBm);
                            heatmapList.add(weightedPoint);
                        }
                    }
                }
                RadioGroup cellSelection = findViewById(R.id.cell_selection);
                cellSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        mOverlay.remove();
                        getDataFromFirebase();
                    }
                });
                RadioGroup mapOptions = findViewById(R.id.border_selection);
                mapOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        switch(i) {
                            case R.id.type_heatmap:
                                mMap.clear();
                                mProvider = new HeatmapTileProvider.Builder()
                                        .weightedData(heatmapList)
                                        .radius(20)
                                        .opacity(0.5)
                                        .build();
                                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                                mOverlay.clearTileCache();
                                break;
                            case R.id.type_convex:
                                mMap.clear();
                                for (PolygonOptions convex: convexList) {
                                    Polygon polygon = mMap.addPolygon(convex);
                                }
                                break;
                            case R.id.type_concave:
                                mMap.clear();
                                for (PolygonOptions concave: concaveList) {
                                    Polygon polygon = mMap.addPolygon(concave);
                                }
                                break;
                            case R.id.type_cluster:
                                mMap.clear();
                                for (PolygonOptions cluster: clusterList) {
                                    Polygon clusters = mMap.addPolygon(cluster);
                                }
                                break;
                            case R.id.type_best_pilot:
                                mMap.clear();
                                for (PolygonOptions pilot: pilotList) {
                                    Polygon pilots = mMap.addPolygon(pilot);
                                }
                                break;
                        }
                    }
                });

            }

            private ArrayList<LatLng> findCorners(LatLng point) {
                double R = 6371e3;
                double d = 10.0 * Math.sqrt(2);
                double cphi1 = Math.asin(Math.sin(point.latitude * (Math.PI / 180)) * Math.cos(d / R) + Math.cos(point.latitude * (Math.PI / 180)) * Math.sin(d / R) * Math.cos(Math.PI / 4));
                double cλ1 = (point.longitude * (Math.PI / 180)) + Math.atan2(Math.sin(Math.PI / 4) * Math.sin(d / R) * Math.cos(point.latitude * (Math.PI / 180)), Math.cos(d / R) - Math.sin(point.latitude * (Math.PI / 180)) * Math.sin(cphi1));
                double cphi2 = Math.asin(Math.sin(point.latitude * (Math.PI / 180)) * Math.cos(d / R) + Math.cos(point.latitude * (Math.PI / 180)) * Math.sin(d / R) * Math.cos(3 * Math.PI / 4));
                double cλ2 = (point.longitude * (Math.PI / 180)) + Math.atan2(Math.sin(3 * Math.PI / 4) * Math.sin(d / R) * Math.cos(point.latitude * (Math.PI / 180)), Math.cos(d / R) - Math.sin(point.latitude * (Math.PI / 180)) * Math.sin(cphi2));
                double cphi3 = Math.asin(Math.sin(point.latitude * (Math.PI / 180)) * Math.cos(d / R) + Math.cos(point.latitude * (Math.PI / 180)) * Math.sin(d / R) * Math.cos(-3 * Math.PI / 4));
                double cλ3 = (point.longitude * (Math.PI / 180)) + Math.atan2(Math.sin(-3 * Math.PI / 4) * Math.sin(d / R) * Math.cos(point.latitude * (Math.PI / 180)), Math.cos(d / R) - Math.sin(point.latitude * (Math.PI / 180)) * Math.sin(cphi3));
                double cphi4 = Math.asin(Math.sin(point.latitude * (Math.PI / 180)) * Math.cos(d / R) + Math.cos(point.latitude * (Math.PI / 180)) * Math.sin(d / R) * Math.cos(-Math.PI / 4));
                double cλ4 = (point.longitude * (Math.PI / 180)) + Math.atan2(Math.sin(-Math.PI / 4) * Math.sin(d / R) * Math.cos(point.latitude * (Math.PI / 180)), Math.cos(d / R) - Math.sin(point.latitude * (Math.PI / 180)) * Math.sin(cphi4));
                LatLng corner1 = new LatLng(cphi1* (180 / Math.PI), cλ1* (180 / Math.PI));
                LatLng corner2 = new LatLng(cphi2* (180 / Math.PI), cλ2* (180 / Math.PI));
                LatLng corner3 = new LatLng(cphi3* (180 / Math.PI), cλ3* (180 / Math.PI));
                LatLng corner4 = new LatLng(cphi4* (180 / Math.PI), cλ4* (180 / Math.PI));
                ArrayList<LatLng> list = new ArrayList<>();
                list.add(corner1);
                list.add(corner2);
                list.add(corner3);
                list.add(corner4);
                return list;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("DatabaseError", "loadPost:onCancelled", databaseError.toException());
            }
        };
        mDatabase.addValueEventListener(cellListener);
    }
    private void setRadioButtons(){
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            int networkType = mTelephony.getNetworkType();
            if (networkType == TelephonyManager.NETWORK_TYPE_UMTS
                    || networkType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || networkType == TelephonyManager.NETWORK_TYPE_HSUPA
                    || networkType == TelephonyManager.NETWORK_TYPE_HSPA) {
                umtsRadio.setChecked(true);
                lteRadio.setChecked(false);
            } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                lteRadio.setChecked(true);
                umtsRadio.setChecked(false);
            } else {
                lteRadio.setChecked(false);
                umtsRadio.setChecked(false);
            }
        }
        heatmapRadio.setChecked(true);
        convexRadio.setChecked(false);
        concaveRadio.setChecked(false);
        clusterRadio.setChecked(false);
        pilotRadio.setChecked(false);
    }
    private String getNetworkType(){
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        int fineLocationPermissionCheck = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION);
        String type = "";
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            int networkType = mTelephony.getNetworkType();
            if (networkType == TelephonyManager.NETWORK_TYPE_UMTS
                    || networkType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || networkType == TelephonyManager.NETWORK_TYPE_HSUPA
                    || networkType == TelephonyManager.NETWORK_TYPE_HSPA) {
                type = "UMTS";
            } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                type = "LTE";
            } else {
                type = "GSM";
            }
        }
        return type;
    }
}
