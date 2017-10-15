/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager;

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
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Sebastian Lenkiewicz on 15.10.2017.
 */

public class MyPhoneStateListener extends PhoneStateListener {
    Context context;
    View view;
    public MyPhoneStateListener(Context c, View v){
        context = c;
        view = v;
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo){
        super.onCellInfoChanged(cellInfo);
    }

    @Override
    public void onCellLocationChanged(CellLocation location){
        super.onCellLocationChanged(location);
        TextView cellView = (TextView)view.findViewById(R.id.cell_tv);
        String cell_info = "Cell Information:\n";
        if (location instanceof GsmCellLocation){
            GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
            cell_info += "Cell Id: " + gsmCellLocation.getCid() + "\n"
                    + "Local Area Code: " + gsmCellLocation.getLac() + "\n"
                    + "Primary Scrambling Code: " + gsmCellLocation.getPsc() + "\n";
        }
        if (location instanceof CdmaCellLocation){
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation)location;
            cell_info += "BS Id: " + cdmaCellLocation.getBaseStationId() + "\n"
                    + "Network Id: " + cdmaCellLocation.getNetworkId() + "\n"
                    + "System Id: " + cdmaCellLocation.getSystemId() + "\n"
                    + "Latitude: " + cdmaCellLocation.getBaseStationLatitude()
                    + " Longitude" + cdmaCellLocation.getBaseStationLongitude() + "\n";
        }
        else {

        }

        cellView.setText(cell_info);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength){
        super.onSignalStrengthsChanged(signalStrength);
        TextView signalText = (TextView)view.findViewById(R.id.signal_strength);
        String signal_str = "";
        if(signalStrength.isGsm()){
            signal_str += "Signal Strength: " + (signalStrength.getGsmSignalStrength()*2-113)
                    + " [dBm]\nGSM Bit Error Rate: " + signalStrength.getGsmBitErrorRate() + "\n";
        }
        else if(signalStrength.getCdmaDbm() > 0){
            signal_str += "Signal Strength: " + signalStrength.getCdmaDbm() + "\n"
                    + "CDMA Ecio: " + signalStrength.getCdmaEcio() + "\n";
        }
        else {
            signal_str += "General Signal Strength: " + signalStrength.getEvdoDbm() + "\n"
                    + "General SNR: " + signalStrength.getEvdoSnr() + "\n"
                    + "General Ecio: " + signalStrength.getEvdoEcio() + "\n";
        }
        signalText.setText(signal_str);
    }
}
