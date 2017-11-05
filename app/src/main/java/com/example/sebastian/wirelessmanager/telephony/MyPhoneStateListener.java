/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony;

import android.content.Context;
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

import org.w3c.dom.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Sebastian Lenkiewicz on 15.10.2017.
 */

class MyPhoneStateListener extends PhoneStateListener {
    private Context context;
    private View view;
    private TelephonyManager telephonyManager;
    MyPhoneStateListener(Context c, View v){
        context = c;
        view = v;
    }

    void start(){
        telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        CellLocation.requestLocationUpdate();
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
        String cell_identity = "";
        String cell_data = "";
        int cell_ = 0, lac_ = 0, mnc_ = 0, mcc_ = 0;
        if (cellInfo != null){
            for (CellInfo c: cellInfo){
                if (c instanceof CellInfoGsm){
                    CellInfoGsm cellInfoGsm = (CellInfoGsm)c;
                    CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                    cell_identity = "GSM";
                    cell_ = cellIdentityGsm.getCid();
                    lac_ = cellIdentityGsm.getLac();
                    mnc_ = cellIdentityGsm.getMnc();
                    mcc_ = cellIdentityGsm.getMcc();
                    break;
                }
                else if (c instanceof CellInfoCdma){
                    CellInfoCdma cellInfoCdma = (CellInfoCdma)c;
                    CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                    cell_identity = "CDMA";
                    cell_ = cellIdentityCdma.getBasestationId();
                    lac_ = cellIdentityCdma.getSystemId();
                    mnc_ = cellIdentityCdma.getNetworkId();
                    break;
                }
                else if (c instanceof CellInfoWcdma){
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma)c;
                    CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                    cell_identity = "WCDMA";
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
                    cell_identity = "LTE";
                    System.out.println(cellInfoLte.toString());
                    cell_ = cellIdentityLte.getCi();
                    mcc_ = cellIdentityLte.getMcc();
                    mnc_ = cellIdentityLte.getMnc();
                    lac_ = cellIdentityLte.getTac();
                    break;
                }
            }
        }
        cell_data = "Cell id: " + cell_ + "\nMobile Country Code: " + mcc_
                + "\nMobile Network Code: " + mnc_ + "\nLocation Area Code: " + lac_;
        cellView.setText("Operator: " + operator + "\t" + "Cell: " + cell_identity);// + "\n" + cell_data);
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
                    signal_str += "Gsm: " + cellInfoGsm.getCellSignalStrength().getDbm() + " [dBm]";
                    break;
                } else if (c instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) c;
                    signal_str += "Cdma: " + cellInfoCdma.getCellSignalStrength().getDbm() + " [dBm]";
                    break;
                } else if (c instanceof CellInfoWcdma) {
                    CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) c;
                    signal_str += "Wcdma: " + cellInfoWcdma.getCellSignalStrength().getDbm()+ " [dBm]";
                    break;
                } else if (c instanceof CellInfoLte) {
                    CellInfoLte cellInfoLte = (CellInfoLte) c;
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
}
