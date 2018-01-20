/*
 * Copyright (c) Sebastian Lenkiewicz 2018.
 */

package com.example.sebastian.wirelessmanager.telephony;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;

/**
 * Created by Sebastian Lenkiewicz on 15.10.2017.
 */

public class MyPhoneStateListener extends PhoneStateListener implements LocationListener {
    private int telephonyPermissionCheck;
    private int fineLocationPermissionCheck;
    private int coarseLocationPermissionCheck;
    private Context context;
    private View view;
    private TextView systemView;
    private LocationManager locationManager;
    private TelephonyManager telephonyManager;
    private Location mLocation;
    private int cid,lac, signaldbm, mcc;
    private String cellType, networkOperator;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    public MyPhoneStateListener(Context c, View v){
        context = c;
        view = v;
    }


    public void start(){
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        systemView = view.findViewById(R.id.system_info);
        CellLocation.requestLocationUpdate();
        fineLocationPermissionCheck = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION);
        telephonyPermissionCheck = ContextCompat.checkSelfPermission(context, READ_PHONE_STATE);
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                telephonyPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.listen(this, //PhoneStateListener.LISTEN_CALL_STATE
                     PhoneStateListener.LISTEN_CELL_INFO // Requires API 17
                    | PhoneStateListener.LISTEN_CELL_LOCATION
                    //| PhoneStateListener.LISTEN_DATA_ACTIVITY
                    //| PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                    | PhoneStateListener.LISTEN_SERVICE_STATE
                    | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            locationManager.removeUpdates(this);
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 10, this);
        }

    }

    public void off() {
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                telephonyPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.listen(this, PhoneStateListener.LISTEN_NONE);
            locationManager.removeUpdates(this);
        }
    }
    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo){
        if (cellInfo != null){
            String operator = telephonyManager.getNetworkOperatorName();
            networkOperator = operator;
            TextView cellView = view.findViewById(R.id.cell_tv);
            TextView dataView = view.findViewById(R.id.cell_data);
            String cell_data;
            int cell_ = 0, lac_ = 0, mnc_ = 0, mcc_ = 0;
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
                    cell_ = cellIdentityLte.getCi();
                    mcc_ = cellIdentityLte.getMcc();
                    mnc_ = cellIdentityLte.getMnc();
                    lac_ = cellIdentityLte.getTac();
                    break;
                }
            }
            cid = cell_;
            lac = lac_;
            cell_data = "Cell id: " + cell_ + "\nLocation Area Code: " + lac_ + "\nMobile Country Code: " + mcc_
                    + "\nMobile Network Code: " + mnc_ ;
            cellView.setText("Operator: " + operator + " Cell: " + cellType);
            dataView.setText(cell_data);
        }
    }
    @Override
    public void onServiceStateChanged(ServiceState serviceState){
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                telephonyPermissionCheck == PackageManager.PERMISSION_GRANTED) {

            String IMEINumber = "";
            String subscriberID = "";
            String SIMSerialNumber = "";
            if (ContextCompat.checkSelfPermission(context, READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                IMEINumber = telephonyManager.getDeviceId();
                subscriberID = telephonyManager.getSubscriberId();
                SIMSerialNumber = telephonyManager.getSimSerialNumber();
            }
            String strphoneType = "";
            int networkType=telephonyManager.getNetworkType();
            switch (networkType)
            {
                case (TelephonyManager.NETWORK_TYPE_CDMA):
                    strphoneType="CDMA";
                    break;
                case (TelephonyManager.NETWORK_TYPE_GSM):
                    strphoneType="GSM";
                    break;
                case (TelephonyManager.NETWORK_TYPE_EDGE):
                    strphoneType="EDGE";
                    break;
                case (TelephonyManager.NETWORK_TYPE_GPRS):
                    strphoneType="GPRS";
                    break;
                case (TelephonyManager.NETWORK_TYPE_LTE):
                    strphoneType="LTE";
                    break;
                case (TelephonyManager.NETWORK_TYPE_UMTS):
                    strphoneType="UMTS";
                    break;
                case (TelephonyManager.NETWORK_TYPE_HSDPA):
                    strphoneType="HDSPA";
                    break;
                case (TelephonyManager.NETWORK_TYPE_HSUPA):
                    strphoneType="HSUPA";
                    break;
                case (TelephonyManager.NETWORK_TYPE_HSPA):
                    strphoneType="HSPA";
                    break;
                default:
                    strphoneType="NONE";
                    break;
            }

            String info="";
            info+="\n IMEI Number:"+IMEINumber;
            info+="\n IMSI Number:"+subscriberID;
            info+="\n Sim Serial Number:"+SIMSerialNumber;
            info+="\n Phone Network Type:"+strphoneType;
            systemView.setText(info);
            /*List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
            if (!cellInfoList.isEmpty())
            {
                onCellsChanged(cellInfoList);
            } else{
                CellLocation cellLocation = telephonyManager.getCellLocation();
                onCellLocationChanged(cellLocation);
            }*/
        }
    }
    /*private void onCellsChanged(List<CellInfo> cellInfo){


    }*/
    @Override
    public void onCellLocationChanged(CellLocation location){
        TextView cellView = view.findViewById(R.id.cell_tv);
        TextView dataView = view.findViewById(R.id.cell_data);
        int cell_ = 0, lac_ = 0;
        String countryCode, operator, cellType;
        countryCode = telephonyManager.getSimOperator();
        String mcc = countryCode.substring(0,3);
        String mnc = countryCode.substring(3);
        operator = telephonyManager.getNetworkOperatorName();
        cellType = getCellType();
        if (location instanceof GsmCellLocation){
            GsmCellLocation gsmCellLocation = (GsmCellLocation)location;
            cell_ = gsmCellLocation.getCid();
            lac_ = gsmCellLocation.getLac();
        } else if (location instanceof  CdmaCellLocation){
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)location;
            cell_ = cdmaCellLocation.getBaseStationId();
            lac_ = cdmaCellLocation.getNetworkId();
        }
        cid = cell_;
        lac = lac_;
        String cellData = "Cell Id: " + cell_ + "\nLocal Area Code: " + lac_ + "\nMobile Country Code: " + mcc
                + "\nMobile Network Code: " + mnc;
        String phoneData = "Operator: " +  operator + " Cell Type: " + cellType;
        cellView.setText(phoneData);
        dataView.setText(cellData);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength){

        super.onSignalStrengthsChanged(signalStrength);
        TextView signalText = view.findViewById(R.id.signal_strength);
        coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION);
        telephonyPermissionCheck = ContextCompat.checkSelfPermission(context, READ_PHONE_STATE);
        if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                telephonyPermissionCheck == PackageManager.PERMISSION_GRANTED) {
            int signal_str=0;
            String text = "";
            int asu = 0;
            try {
                Method[] methods = android.telephony.SignalStrength.class
                        .getMethods();
                for (Method mthd : methods) {
                    if (mthd.getName().equals("getAsuLevel")) {
                        Log.i("tag",
                                "onSignalStrengthsChanged: " + mthd.getName() + " "
                                        + mthd.invoke(signalStrength));
                        asu = Integer.parseInt(mthd.invoke(signalStrength).toString());
                    }
                }
            } catch (SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                e.printStackTrace();
            }
            if (telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){
                try {
                    Method[] methods = android.telephony.SignalStrength.class
                            .getMethods();
                    int rsrq = 0;
                    for (Method mthd : methods) {
                        if (mthd.getName().equals("getLteRsrp")) {
                            Log.i("tag",
                                    "onSignalStrengthsChanged: " + mthd.getName() + " "
                                            + mthd.invoke(signalStrength));
                            signal_str = Integer.parseInt(mthd.invoke(signalStrength).toString());
                        }
                        if (mthd.getName().equals("getLteRsrq")) {
                            Log.i("tag",
                                    "onSignalStrengthsChanged: " + mthd.getName() + " "
                                            + mthd.invoke(signalStrength));
                            rsrq = Integer.parseInt(mthd.invoke(signalStrength).toString());
                        }
                    }
                    text = "RSRP[dBm] = " + signal_str + " RSRQ[dB] = " + rsrq + "\nAsu: "  + asu;
                } catch (SecurityException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
                    e.printStackTrace();
                }

            } else if (telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
                    || telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
                    || telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
                    || telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA) {
                signal_str = asu - 116;
                text = signal_str + " [dBm] asu: " + asu;
            }
            else {
                // TS 27.0007 - V10.3.0 sub clause 8.5 for gsm signal strength
                signal_str = (asu*2)-113;
                text = signal_str + " [dBm] asu: " + asu;
            }
            signaldbm = signal_str;
            signalText.setText(text);
            signalText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        Cell cell = new Cell(cid,lac,signaldbm,mLocation.getLongitude(),mLocation.getLatitude(),cellType);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            databaseReference = FirebaseDatabase.getInstance().getReference();
            mcc = Integer.parseInt(telephonyManager.getNetworkOperator().substring(0,3));
            networkOperator = telephonyManager.getNetworkOperatorName();
            cellType = getCellType();
            DatabaseReference dbRef = databaseReference.child(mcc + "/" + networkOperator + "/" + cellType + "/" + cid).push();
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

    private String getCellType(){
        String type = "";
        if (telephonyManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM){
            fineLocationPermissionCheck = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
            coarseLocationPermissionCheck = ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION);
            if (fineLocationPermissionCheck == PackageManager.PERMISSION_GRANTED &&
                    coarseLocationPermissionCheck == PackageManager.PERMISSION_GRANTED) {
                int networkType = telephonyManager.getNetworkType();
                if (networkType == TelephonyManager.NETWORK_TYPE_UMTS
                        || networkType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || networkType == TelephonyManager.NETWORK_TYPE_HSUPA
                        || networkType == TelephonyManager.NETWORK_TYPE_HSPA){
                    type = "UMTS";
                } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE){
                    type = "LTE";
                }
                else {
                    type = "GSM";
                }
            }

        }
        return type;
    }
}
