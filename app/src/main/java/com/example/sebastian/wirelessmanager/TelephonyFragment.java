/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class TelephonyFragment extends Fragment {
    Context mcontext;
    TelephonyManager tm;
    TextView textView, cellView;
    public TelephonyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_telephony, container, false);
        mcontext = super.getContext();
        textView=(TextView)view.findViewById(R.id.telephony_tv);
        cellView=(TextView)view.findViewById(R.id.cell_tv);
        mcontext = super.getContext();
        System.out.println(R.id.telephony_tv);
        tm = (TelephonyManager)mcontext.getSystemService(Context.TELEPHONY_SERVICE);
        String IMEINumber=tm.getDeviceId();
        String subscriberID=tm.getDeviceId();
        String SIMSerialNumber=tm.getSimSerialNumber();
        String networkCountryISO=tm.getNetworkCountryIso();
        String SIMCountryISO=tm.getSimCountryIso();
        String softwareVersion=tm.getDeviceSoftwareVersion();
        String voiceMailNumber=tm.getVoiceMailNumber();
        String strphoneType="";
        List<CellInfo> cellInfoList = tm.getAllCellInfo();
        int phoneType=tm.getPhoneType();
        String cell_info = "Cell Information:\n";
        for (CellInfo cellInfos : cellInfoList){
            if (cellInfos instanceof CellInfoGsm){
                CellInfoGsm cellInfoGsm = (CellInfoGsm)cellInfos;
                CellIdentityGsm cellIdentityGsm = cellInfoGsm.getCellIdentity();
                cell_info += "Cell Id: " + cellIdentityGsm.getCid() + "\n"
                        + "Mobile Country Code: " + cellIdentityGsm.getMcc() + "\n"
                        + "Mobile Network Code: " + cellIdentityGsm.getMnc() + "\n"
                        + "Local Area Code: " + cellIdentityGsm.getLac() + "\n"
                        + "Signal Str: " + cellInfoGsm.getCellSignalStrength().getDbm() + " [dBm]\n";
                break;
            }
            if (cellInfos instanceof CellInfoCdma){
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos;
                CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
                cell_info += "BS Id: " + cellIdentityCdma.getBasestationId() + "\n"
                        + "Network Id: " + cellIdentityCdma.getNetworkId() + "\n"
                        + "System Id: " + cellIdentityCdma.getSystemId() + "\n"
                        + "Latitude: " + cellIdentityCdma.getLatitude()
                        + " Longitude" + cellIdentityCdma.getLongitude() + "\n";
                break;
            }
            if (cellInfos instanceof CellInfoLte){
                CellInfoLte cellInfoLte = (CellInfoLte)cellInfos;
                CellIdentityLte cellIdentityLte = cellInfoLte.getCellIdentity();
                cell_info += "Cell Id: " + cellIdentityLte.getCi() + "\n"
                        + "Mobile Country Code: " + cellIdentityLte.getMcc() + "\n"
                        + "Mobile Network Code: " + cellIdentityLte.getMnc() + "\n"
                        + "Local Area Code: " + cellIdentityLte.getPci() + "\n"
                        + "Tracking Area Code: " + cellIdentityLte.getTac() + "\n";
                break;
            }
            if (cellInfos instanceof CellInfoWcdma){
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma)cellInfos;
                CellIdentityWcdma cellIdentityWcdma = cellInfoWcdma.getCellIdentity();
                cell_info += "Cell Id:" + cellIdentityWcdma.getCid() + "\n"
                        + "Mobile Country Code: " + cellIdentityWcdma.getMcc() + "\n"
                        + "Mobile Network Code: " + cellIdentityWcdma.getMnc() + "\n"
                        + "Local Area Code: " + cellIdentityWcdma.getLac() + "\n"
                        + "Signal Str: " + cellInfoWcdma.getCellSignalStrength().getDbm() + " [dBm]\n";
                break;
            }
        }
        cellView.setText(cell_info);
        switch (phoneType)
        {
            case (TelephonyManager.PHONE_TYPE_CDMA):
                strphoneType="CDMA";
                break;
            case (TelephonyManager.PHONE_TYPE_GSM):
                strphoneType="GSM";
                break;
            case (TelephonyManager.PHONE_TYPE_NONE):
                strphoneType="NONE";
                break;
        }
        boolean isRoaming=tm.isNetworkRoaming();
        String info="Phone Details:\n";
        info+="\n IMEI Number:"+IMEINumber;
        info+="\n SubscriberID:"+subscriberID;
        info+="\n Sim Serial Number:"+SIMSerialNumber;
        info+="\n Network Country ISO:"+networkCountryISO;
        info+="\n SIM Country ISO:"+SIMCountryISO;
        info+="\n Software Version:"+softwareVersion;
        info+="\n Voice Mail Number:"+voiceMailNumber;
        info+="\n Phone Network Type:"+strphoneType;
        info+="\n In Roaming? :"+isRoaming;
        textView.setText(info);
        return view;
    }

}
