package com.a8lambda8.carlog;

import android.os.Bundle;
import android.text.format.Time;

import java.io.Serializable;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class list_Item implements Serializable {


    private Time tStart;
    private Time tEnd;

    private String StartLoc = null;
    private String EndLoc = null;

    private int start = 0;
    private int end = 0;

    private String speed = null;
    private String drain = null;

    private String driverName = null;

    private Boolean refuel = false;
    private String price = null;


    list_Item(){

    }

    list_Item(long tStart, long tEnd, String StartLoc, String EndLoc, int start, int end, String speed, String drain, String driverName, Boolean refuel, String price){
        this.tStart = new Time(Time.getCurrentTimezone());
        this.tStart.set(tStart);

        this.tEnd = new Time(Time.getCurrentTimezone());
        this.tEnd.set(tEnd);

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

    list_Item(Bundle bundle){
        tStart = new Time(Time.getCurrentTimezone());
        tStart.set(bundle.getLong("tStart"));

        tEnd = new Time(Time.getCurrentTimezone());
        tEnd.set(bundle.getLong("tEnd"));

        StartLoc = bundle.getString("StartLoc");
        EndLoc = bundle.getString("EndLoc");

        start = bundle.getInt("start");
        end = bundle.getInt("end");

        speed = bundle.getString("speed");
        drain = bundle.getString("drain");

        driverName = bundle.getString("driverName");

        refuel = bundle.getBoolean("refuel");
        price = bundle.getString("price");
    }


    Time gettStart() {
        return tStart;
    }

    void settStart(Time tStart) {
        this.tStart = tStart;
    }

    Time gettEnd() {
        return tEnd;
    }

    void settEnd(Time tEnd) {
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
}