/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sebastian Lenkiewicz on 15.10.2017.
 */

class MyPhoneStateListener extends PhoneStateListener implements LocationListener {
    private Context context;
    private View view;
    private LocationManager locationManager;
    private TelephonyManager telephonyManager;
    private Location mLocation;
    private int cid,lac, signaldbm;
    private String cellType;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    MyPhoneStateListener(Context c, View v){
        context = c;
        view = v;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    void start(){
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        CellLocation.requestLocationUpdate();
        // slightly increase value of minDistance
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,60000,0,this);
        telephonyManager.listen(this,PhoneStateListener.LISTEN_CALL_STATE
                | PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                | PhoneStateListener.LISTEN_CELL_LOCATION
                | PhoneStateListener.LISTEN_DATA_ACTIVITY
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                | PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    }
    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo){
        int id_ = telephonyManager.getNetworkType();
        if (cellInfo != null){
            onCellsChanged(cellInfo);
        } else {
            onCellsChanged(telephonyManager.getAllCellInfo());
        }
    }
    @Override
    public void onServiceStateChanged(ServiceState serviceState){
        onCellsChanged(telephonyManager.getAllCellInfo());
    }
    private void onCellsChanged(List<CellInfo> cellInfo){
        String operator = telephonyManager.getNetworkOperatorName();
        //onCellInfoChanged(telephonyManager.getAllCellInfo());
        TextView cellView = (TextView)view.findViewById(R.id.cell_tv);
        TextView dataView = (TextView)view.findViewById(R.id.cell_data);
        String cell_data;
        int cell_ = 0, lac_ = 0, mnc_ = 0, mcc_ = 0;
        if (cellInfo != null){
            for (CellInfo c: cellInfo){
                if (c instanceof CellInfoGsm){
                    CellInfoGsm cellInfoGsm = (CellInfoGsm)c;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    cellType = "GSM";
                    cell_ = cellIdentityGsm.getCid();
                    lac_ = cellIdentityGsm.getLac();
                    mnc_ = cellIdentityGsm.getMnc();
                    mcc_ = cellIdentityGsm.getMcc();
                    break;
                }
                else if (c instanceof CellInfoCdma){
                    CellInfoCdma cellInfoCdma = (CellInfoCdma)c;
                    CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                    cellType = "CDMA";
                    cell_ = cellIdentityCdma.getBasestationId();
                    lac_ = cellIdentityCdma.getSystemId();
                    mnc_ = cellIdentityCdma.getNetworkId();
                    break;
                }
                else if (c instanceof CellInfoWcdma){
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma)c;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    cellType = "WCDMA";
                    System.out.println(cellInfoWcdma.toString());
                    cell_ = cellIdentityWcdma.getCid();
                    mcc_ = cellIdentityWcdma.getMcc();
                    mnc_ = cellIdentityWcdma.getMnc();
                    lac_ = cellIdentityWcdma.getLac();
                    break;
                }
                else if (c instanceof CellInfoLte){
                    CellInfoLte cellInfoLte = (CellInfoLte)c;
                    CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                    cellType = "LTE";
                    System.out.println(cellInfoLte.toString());
                    cell_ = cellIdentityLte.getCi();
                    mcc_ = cellIdentityLte.getMcc();
                    mnc_ = cellIdentityLte.getMnc();
                    lac_ = cellIdentityLte.getTac();
                    break;
                }
            }
        }
        cid = cell_;
        lac = lac_;
        cell_data = "Cell id: " + cell_ + "\nMobile Country Code: " + mcc_
                + "\nMobile Network Code: " + mnc_ + "\nLocation Area Code: " + lac_;
        cellView.setText("Operator: " + operator + "\t" + "Cell: " + cellType);// + "\n" + cell_data);
        dataView.setText(cell_data);

    }
    @Override
    public void onCellLocationChanged(CellLocation location){
        onCellsChanged(telephonyManager.getAllCellInfo());
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength){
        TextView signalText = (TextView)view.findViewById(R.id.signal_strength);
        List<CellInfo> mcellinfo = telephonyManager.getAllCellInfo();
        String signal_str = "";
        String ssignal = signalStrength.toString();
        String[] parts = ssignal.split(" ");
        int phone_type = telephonyManager.getPhoneType();
        if (mcellinfo != null) {
            for (CellInfo c : mcellinfo) {
                if (c instanceof CellInfoGsm) {
                    CellInfoGsm cellInfoGsm = (CellInfoGsm) c;
                    signaldbm = cellInfoGsm.getCellSignalStrength().getDbm();
                    signal_str += "Gsm: " + cellInfoGsm.getCellSignalStrength().getDbm() + " [dBm]";
                    break;
                } else if (c instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) c;
                    signaldbm = cellInfoCdma.getCellSignalStrength().getDbm();
                    signal_str += "Cdma: " + cellInfoCdma.getCellSignalStrength().getDbm() + " [dBm]";
                    break;
                } else if (c instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) c;
                    signaldbm = cellInfoWcdma.getCellSignalStrength().getDbm();
                    signal_str += "Wcdma: " + cellInfoWcdma.getCellSignalStrength().getDbm()+ " [dBm]";
                    break;
                } else if (c instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) c;
                    signaldbm = cellInfoLte.getCellSignalStrength().getDbm();
                    signal_str += "Lte: " + cellInfoLte.getCellSignalStrength().getDbm() + " [dBm]";
                    break;
                }
            }
        }
        /*
        switch(phone_type){
            case (TelephonyManager.PHONE_TYPE_CDMA):
                signal_str += signalStrength.getCdmaDbm() + " dBm";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                signal_str += (signalStrength.getGsmSignalStrength()*2-113) + " dBm";
                break;
            case (TelephonyManager.NETWORK_TYPE_LTE):
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                break;
        }
        */
        signalText.setText(signal_str);
        signalText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        System.out.println("Long: " + mLocation.getLongitude() + " Lat: " + mLocation.getLatitude());
        Cell cell = new Cell(cid,lac,signaldbm,mLocation.getLongitude(),mLocation.getLatitude(),cellType);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            String userId = firebaseAuth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference dbRef = databaseReference.child("cells/"+cid).push();
            dbRef.setValue(cell);

        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
