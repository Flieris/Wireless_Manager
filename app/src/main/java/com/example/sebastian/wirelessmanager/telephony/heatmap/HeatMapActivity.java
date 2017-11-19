/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony.heatmap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sebastian.wirelessmanager.MainActivity;
import com.example.sebastian.wirelessmanager.R;
import com.example.sebastian.wirelessmanager.telephony.Cell;
import com.example.sebastian.wirelessmanager.telephony.MyPhoneStateListener;
import com.example.sebastian.wirelessmanager.telephony.TelephonyFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HeatMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMapLoadedCallback {
    //Ui components:
    private TextView operatorTv;
    private RadioButton lteRadio, umtsRadio;
    //Firebase reference and data:
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mDatabase;
    //GoogleMap and Location:
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    //Cell heat map:
    private ArrayList<Cell> cellList;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private ArrayList<WeightedLatLng> weightedLatLngList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        cellList = new ArrayList<>();
        operatorTv = (TextView)findViewById(R.id.map_network_operator);
        lteRadio = (RadioButton)findViewById(R.id.type_lte);
        umtsRadio = (RadioButton)findViewById(R.id.type_umts);
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
        final LayoutInflater factory = getLayoutInflater();
        final View telephonyView = factory.inflate(R.layout.fragment_telephony,null);
        MyPhoneStateListener phoneStateListener = new MyPhoneStateListener(this.getApplicationContext(),telephonyView);
        phoneStateListener.start();
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
        Log.i("test",cellList.toString());

        //mProvider = new HeatmapTileProvider.Builder()
         //       .weightedData(list)
          //      .build();
    }

    private void getDataFromFirebase() {

        weightedLatLngList = new ArrayList<>();
        TelephonyManager mTelephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final int mcc = Integer.parseInt(mTelephony.getNetworkOperator().substring(0, 3));
        final String operator = mTelephony.getNetworkOperatorName();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // database.setPersistenceEnabled(true);
        RadioGroup cellSelection = (RadioGroup)findViewById(R.id.cell_selection);
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
        final String finalType = getNetworkType();
        mDatabase = database.getReference(mcc + "/" + operator + "/" + type);
        ValueEventListener cellListener = new ValueEventListener() {
            ArrayList<WeightedLatLng> list = new ArrayList<>();
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cellSnapshot: dataSnapshot.getChildren()){
                    for(DataSnapshot pointSnapshot: cellSnapshot.getChildren()){
                        Cell cell = pointSnapshot.getValue(Cell.class);
                        double lat = cell.latitude;
                        double lng = cell.longitude;
                        double dBm = 200 + cell.signalStrength;
                        LatLng point = new LatLng(lat,lng);
                        System.out.println(cell.toString());
                        WeightedLatLng weightedPoint = new WeightedLatLng(point,dBm);
                        list.add(weightedPoint);
                    }
                }
                // find an alternative to heatmap
                // find a way so points dissipate with zoom
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(list)
                        .radius(20)
                        .opacity(0.5)
                        .build();
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                mOverlay.clearTileCache();
                RadioGroup cellSelection = (RadioGroup)findViewById(R.id.cell_selection);
                cellSelection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        mOverlay.remove();
                        getDataFromFirebase();
                    }
                });
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
