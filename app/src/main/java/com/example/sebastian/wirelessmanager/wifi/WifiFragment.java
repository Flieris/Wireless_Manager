/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.wifi;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.sebastian.wirelessmanager.R;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 */
public class WifiFragment extends Fragment {
    View view;
    Context context;
    IntentFilter intentFilter = new IntentFilter();
    ImageView wifi_state_image;
    TextView state_text, basic_info_tv, singal_strength;
    Switch wifi_control;
    WifiManager wifiManager;
    boolean wifi_state = false;
    public WifiFragment() {
        // Required empty public constructor
    }
    private BroadcastReceiver WifiBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            basic_info_tv = (TextView)view.findViewById(R.id.wifi_basic_content);
            singal_strength = (TextView)view.findViewById(R.id.wifi_signal_strength);
            String basic_info = "", signal_ = "No Wi-Fi signal";
            int state = wifiManager.getWifiState();
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            switch(state) {
                case WifiManager.WIFI_STATE_DISABLED:
                    wifi_state = false;
                    wifi_state_image.setImageResource(R.drawable.ic_wifi_off);
                    state_text.setText(getString(R.string.wifi_off));
                    basic_info = "Mac address: " + wifiInfo.getMacAddress();
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    wifi_state = true;
                    wifi_state_image.setImageResource(R.drawable.ic_wifi_on);
                    state_text.setText(getString(R.string.wifi_on));
                    int ip = wifiInfo.getIpAddress();
                    int gateway = wifiManager.getDhcpInfo().gateway;
                    int netmask = wifiManager.getDhcpInfo().netmask;
                    basic_info = "Mac address: " + wifiInfo.getMacAddress() + "\n"
                            + "BSSI: " + wifiInfo.getBSSID() + "\n"
                            + "SSSI: " + wifiInfo.getSSID() + "\n"
                            + "Ip address: " + getIpfromInt(ip) + "\n"
                            + "Default Gateway: " + getIpfromInt(gateway) + "\n"
                            + "Netmask: " + getIpfromInt(netmask);
                    signal_ = "" + wifiInfo.getRssi() + " [dBm]";
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    wifi_state = false;
                    wifi_state_image.setImageResource(R.drawable.ic_wifi_off);
                    state_text.setText(getString(R.string.wifi_disabling));
                    basic_info = "Mac address: " + wifiInfo.getMacAddress();
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    wifi_state = true;
                    wifi_state_image.setImageResource(R.drawable.ic_wifi_on);
                    state_text.setText(getString(R.string.wifi_enabling));
                    basic_info = "Mac address: " + wifiInfo.getMacAddress();
                    break;
            }
            singal_strength.setText(signal_);
            basic_info_tv.setText(basic_info);
        }
    };
    public String getIpfromInt(int ip_){
        return String.format("%d.%d.%d.%d",(ip_ & 0xff),(ip_ >> 8 & 0xff), (ip_ >> 16 & 0xff), (ip_ >> 24 & 0xff));
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_wifi, container, false);
        context = super.getContext();
        wifi_state_image = (ImageView)view.findViewById(R.id.wifi_off_on_icon);
        state_text = (TextView)view.findViewById(R.id.wifi_state_text);
        wifi_control = (Switch)view.findViewById(R.id.wifi_control);
        checkWifi();
        if (wifi_state){
            wifi_control.setChecked(true);
        }
        else{
            wifi_control.setChecked(false);
        }
        wifi_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(isChecked){
                    wifiManager.setWifiEnabled(true);
                    //checkWifi();
                } else{
                    wifiManager.setWifiEnabled(false);
                    //checkWifi();
                }
            }
        });
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.EXTRA_NEW_RSSI);
        intentFilter.addAction(WifiManager.EXTRA_NETWORK_INFO);
        context.registerReceiver(WifiBroadcast, intentFilter);
        return view;
    }
    public void checkWifi(){
        wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        basic_info_tv = (TextView)view.findViewById(R.id.wifi_basic_content);
        singal_strength = (TextView)view.findViewById(R.id.wifi_signal_strength);
        String basic_info = "", signal_ = "No Wi-Fi signal";
        int state = wifiManager.getWifiState();
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        switch(state) {
            case WifiManager.WIFI_STATE_DISABLED:
                wifi_state = false;
                wifi_state_image.setImageResource(R.drawable.ic_wifi_off);
                state_text.setText(getString(R.string.wifi_off));
                basic_info = "Mac address: " + wifiInfo.getMacAddress();
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                wifi_state = true;
                wifi_state_image.setImageResource(R.drawable.ic_wifi_on);
                state_text.setText(getString(R.string.wifi_on));
                int ip = wifiInfo.getIpAddress();
                int gateway = wifiManager.getDhcpInfo().gateway;
                int netmask = wifiManager.getDhcpInfo().netmask;
                basic_info = "Mac address: " + wifiInfo.getMacAddress() + "\n"
                        + "BSSI: " + wifiInfo.getBSSID() + "\n"
                        + "SSSI: " + wifiInfo.getSSID() + "\n"
                        + "Ip address: " + getIpfromInt(ip) + "\n"
                        + "Default Gateway: " + getIpfromInt(gateway) + "\n"
                        + "Netmask: " + getIpfromInt(netmask);
                signal_ = "" + wifiInfo.getRssi() + " [dBm]";
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                wifi_state = false;
                wifi_state_image.setImageResource(R.drawable.ic_wifi_off);
                state_text.setText(getString(R.string.wifi_disabling));
                basic_info = "Mac address: " + wifiInfo.getMacAddress();
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                wifi_state = true;
                wifi_state_image.setImageResource(R.drawable.ic_wifi_on);
                state_text.setText(getString(R.string.wifi_enabling));
                basic_info = "Mac address: " + wifiInfo.getMacAddress();
                signal_ = "" + wifiInfo.getRssi() + " [dBm]";
                break;
        }
        singal_strength.setText(signal_);
        basic_info_tv.setText(basic_info);

    }
    @Override
    public void onDestroy(){
        context.unregisterReceiver(WifiBroadcast);
        super.onDestroy();
    }
    @Override
    public void onPause(){
        //context.unregisterReceiver(wifiReceiver);
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
        context.registerReceiver(WifiBroadcast, intentFilter);
    }
}
