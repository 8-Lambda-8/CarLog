package com.a8lambda8.carlog;

import android.os.Bundle;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class trip_Item implements Serializable {

    private String ID;

    private Date tStart;
    private Date tEnd;

    private String StartLoc = null;
    private String EndLoc = null;

    private int start = 0;
    private int end = 0;

    private String speed = null;
    private String drain = null;

    private String driverName = null;
    private String driverId = null;

    private Boolean refuel = false;
    private String price = null;


    trip_Item(){

    }

    trip_Item(long tStart, long tEnd, String StartLoc, String EndLoc, int start, int end, String speed, String drain, String driverName, Boolean refuel, String price){
        //this.tStart = new Time(Time.getCurrentTimezone());
        this.tStart.setTime(tStart);

        //this.tEnd = new Time(Time.getCurrentTimezone());
        this.tEnd.setTime(tEnd);

        this.StartLoc = StartLoc;
        this.EndLoc = EndLoc;

        this.start = start;
        this.end = end;

        this.speed = speed;
        this.drain = drain;

        this.driverName = driverName;

        this.refuel = refuel;
        this.price = price;
    }

    trip_Item(Bundle bundle){
        //tStart = new Time(Time.getCurrentTimezone());
        tStart.setTime(bundle.getLong("tStart"));

        //tEnd = new Time(Time.getCurrentTimezone());
        tEnd.setTime(bundle.getLong("tEnd"));

        StartLoc = bundle.getString("StartLoc");
        EndLoc = bundle.getString("EndLoc");

        start = bundle.getInt("start");
        end = bundle.getInt("end");

        speed = bundle.getString("speed");
        drain = bundle.getString("drain");

        driverName = bundle.getString("driverName");
        driverId = bundle.getString("driverId");

        refuel = bundle.getBoolean("refuel");
        price = bundle.getString("price");
    }

    trip_Item(HashMap map){
        //tStart = new Time(Time.getCurrentTimezone());

        tStart.setTime((Long) map.get("tStart"));


        //tEnd = new Time(Time.getCurrentTimezone());
        tEnd.setTime((long) map.get("tEnd"));

        StartLoc = (String) map.get("StartLoc");
        EndLoc = (String) map.get("EndLoc");

        start = (Integer) map.get("start");
        end = (Integer) map.get("end");

        speed = (String) map.get("speed");
        drain = (String) map.get("drain");

        driverName = (String) map.get("driverName");
        driverId = (String) map.get("driverId");

        refuel = (boolean) map.get("refuel");
        price = (String) map.get("price");
    }

    Date gettStart() {
        return tStart;
    }

    void settStart(Date tStart) {
        this.tStart = tStart;
    }

    Date gettEnd() {
        return tEnd;
    }

    void settEnd(Date tEnd) {
        this.tEnd = tEnd;
    }



    String getEndLoc() {
        return EndLoc;
    }

    void setEndLoc(String endLoc) {
        EndLoc = endLoc;
    }

    String getStartLoc() {
        return StartLoc;
    }

    void setStartLoc(String startLoc) {
        StartLoc = startLoc;
    }



    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }



    String getSpeed() {
        return speed;
    }

    void setSpeed(String speed) {
        this.speed = speed;
    }

    String getDrain() {
        return drain;
    }

    void setDrain(String drain) {
        this.drain = drain;
    }



    String getDriverName() {
        return driverName;
    }

    void setDriverName(String driverName) {
        this.driverName = driverName;
    }




    Boolean getRefuel() {
        return refuel;
    }

    void setRefuel(Boolean refuel) {
        this.refuel = refuel;
    }

    String getPrice() {
        return price;
    }

    void setPrice(String price) {
        this.price = price;
    }

    Map<String, Object> getMap(){
        Map<String, Object> map = new HashMap<>();

        if (tStart != null) {
            map.put("startTime",DBDateFormat.format(tStart));
        }
        if (tEnd != null) {
            map.put("endTime",DBDateFormat.format(tEnd));
        }
        if (StartLoc != null) {
            map.put("startLoc", StartLoc);
        }
        if (EndLoc != null) {
            map.put("endLoc",EndLoc);
        }
        if (start != 0) {
            map.put("startKm",start);
        }
        if (end != 0) {
            map.put("endKm",end);
        }
        if (speed != null) {
            map.put("speed",speed);
        }
        if (drain != null) {
            map.put("drain",drain);
        }
        if (driverName != null) {
            map.put("driver",driverName);
        }
        if (driverId != null) {
            map.put("driverId",driverId);
        }
        if (refuel != null) {
            map.put("refuel",refuel);
        }
        if (price != null) {
            map.put("price",price);
        }

        return map;
    }

    public String getDriverId() {
        return driverId;
    }

    void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    String getID() {
        return ID;
    }

    void setID(String ID) {
        this.ID = ID;
    }
}