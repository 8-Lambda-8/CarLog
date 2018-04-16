package com.a8lambda8.carlog;

import android.text.format.Time;

/**
 * Created by Jakob Wasle on 15.09.2017.
 */

public class list_Item {


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



    public Time gettStart() {
        return tStart;
    }

    public void settStart(Time tStart) {
        this.tStart = tStart;
    }

    public Time gettEnd() {
        return tEnd;
    }

    public void settEnd(Time tEnd) {
        this.tEnd = tEnd;
    }



    public String getEndLoc() {
        return EndLoc;
    }

    public void setEndLoc(String endLoc) {
        EndLoc = endLoc;
    }

    public String getStartLoc() {
        return StartLoc;
    }

    public void setStartLoc(String startLoc) {
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



    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getDrain() {
        return drain;
    }

    public void setDrain(String drain) {
        this.drain = drain;
    }



    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }




    public Boolean getRefuel() {
        return refuel;
    }

    public void setRefuel(Boolean refuel) {
        this.refuel = refuel;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}