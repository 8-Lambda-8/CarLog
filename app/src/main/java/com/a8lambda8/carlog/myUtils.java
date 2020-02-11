package com.a8lambda8.carlog;

import android.text.format.Time;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

class myUtils {

    static String DBDateFormat,DBDateFormat_start;

    static DatabaseReference mDatabase, mDatabase_selectedCar;

    static final int RC_SIGN_IN = 123;
    static int selectedCarId;

    static final String TAG = "xx";

    static boolean TestDevice = true;

    static Object DbVal(DataSnapshot key, String child){
        return key.child(child).getValue();
    }

    static int DbInt(DataSnapshot key,String child){
        if (DbVal(key,child)!=null)
            return Integer.valueOf(DbString(key,child));
        else return 0;
    }

    static String DbString(DataSnapshot key,String child){
        if (DbVal(key,child)!=null)
            return DbVal(key,child).toString();
        else return  "";
    }

    static Time TimeParser(String time, String format){
        Time t = new Time(Time.getCurrentTimezone());

        if(format.equals(DBDateFormat))     //%y-%m-%d_%H-%M-%S
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(6,8)),Integer.parseInt(time.substring(3,5))-1,2000+Integer.parseInt(time.substring(0,2)));
        else if (format.equals(DBDateFormat_start))   //Y%y/M%m/D%d/%H-%M-%S
            t.set(Integer.parseInt(time.substring(18)),Integer.parseInt(time.substring(15,17)),Integer.parseInt(time.substring(12,14)),
                    Integer.parseInt(time.substring(9,11)),Integer.parseInt(time.substring(5,7))-1,2000+Integer.parseInt(time.substring(1,3)));
        else if(format.charAt(1)=='d')
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(0,2)),Integer.parseInt(time.substring(3,5))-1,2000+Integer.parseInt(time.substring(6,8)));
        return t;
    }

    static String StartTimeStringParser(DataSnapshot time){

        DatabaseReference d = time.getRef().getParent();
        DatabaseReference m = Objects.requireNonNull(d).getParent();
        DatabaseReference y = Objects.requireNonNull(m).getParent();

        return Objects.requireNonNull(y).getKey()+"/"+m.getKey()+"/"+d.getKey()+"/"+time.getKey();
    }


}
