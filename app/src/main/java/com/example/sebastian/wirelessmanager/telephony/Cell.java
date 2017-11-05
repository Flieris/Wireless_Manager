/*
 * Copyright (c) Sebastian Lenkiewicz 2017.
 */

package com.example.sebastian.wirelessmanager.telephony;

/**
 * Created by Sebastian Lenkiewicz on 05.11.2017.
 */

public class Cell {
    public int cellId;
    public int lac;
    public int signalStrength;
    public double longitude;
    public double latitude;
    public String cellType;
    public Cell(){
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
     Cell(int id, int lac_, int dbm, double lon, double lat, String type){
        this.cellId = id;
        this.lac = lac_;
        this.signalStrength = dbm;
        this.longitude = lon;
        this.latitude = lat;
        this.cellType = type;
    }
    @Override
    public String toString(){
         return new StringBuilder()
                 .append("{Cell: ")
                 .append(" type= ").append(cellType)
                 .append(" id= ").append(cellId)
                 .append(" lac= ").append(lac)
                 .append(" signalStrength= ").append(signalStrength)
                 .append(" longitude= ").append(longitude)
                 .append(" latitude= ").append(latitude)
                 .append("}").toString();
    }
}
