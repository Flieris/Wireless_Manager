/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.telephony.MyPhoneStateListener;
import com.example.sebastian.wirelessmanager.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TelephonyFragment extends Fragment {
    Context context;
    TelephonyManager tm;
    TextView textView, cellView, abcView;
    CardView dataView;
    public TelephonyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);

    }
    /*
        TODO:
        -> implement expandable cards
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_telephony, container, false);
        context = super.getContext();
        textView=(TextView)view.findViewById(R.id.system_info);
        cellView=(TextView)view.findViewById(R.id.cell_tv);
        abcView=(TextView)view.findViewById(R.id.cell_data);
        dataView=(CardView)view.findViewById(R.id.cell_info);
        dataView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (abcView.getVisibility() == View.GONE) {
                    abcView.setVisibility(View.VISIBLE);
                } else {
                    abcView.setVisibility(View.GONE);
                }
                ObjectAnimator animation = ObjectAnimator.ofInt(abcView, "maxLines", abcView.getMaxLines());
                animation.setDuration(200).start();
            }
        });
        context = super.getContext();
        //System.out.println(R.id.telephony_tv);
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String IMEINumber=tm.getDeviceId();
        String subscriberID=tm.getDeviceId();
        String SIMSerialNumber=tm.getSimSerialNumber();
        String strphoneType="";
        //int phoneType=tm.getPhoneType();
        int networkType=tm.getNetworkType();
        switch (networkType)
        {
            case (TelephonyManager.NETWORK_TYPE_CDMA):
                strphoneType="CDMA";
                break;
            case (TelephonyManager.NETWORK_TYPE_GSM):
                strphoneType="GSM";
                break;
            case (TelephonyManager.NETWORK_TYPE_LTE):
                strphoneType="LTE";
                break;
            case (TelephonyManager.NETWORK_TYPE_UMTS):
                strphoneType="UMTS";
                break;
            default:
                strphoneType="NONE";
                break;
        }

        String info="";
        info+="\n IMEI Number:"+IMEINumber;
        info+="\n SubscriberID:"+subscriberID;
        info+="\n Sim Serial Number:"+SIMSerialNumber;
        info+="\n Phone Network Type:"+strphoneType;
        textView.setText(info);
        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener(context,view);
        myPhoneStateListener.start();
        return view;
    }

}
