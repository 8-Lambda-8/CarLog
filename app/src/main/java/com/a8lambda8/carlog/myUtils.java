package com.a8lambda8.carlog;

import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

class myUtils {

    static String DBDateFormat,DBDateFormat_start;

    static DatabaseReference mDatabase, mDatabase_selectedCar;
    static FirebaseFirestore db;
    static DocumentReference currentCarRef, currentUserRef;
    static CollectionReference currentCarDataRef;
    static FirebaseAuth mAuth;

    static SharedPreferences SP;
    static SharedPreferences.Editor SPEdit;

    static final int RC_SIGN_IN = 123;
    static String selectedCarId;// =  "tUumoKgiA77OHX7JUTpY"; // "MNicRziL9DRfawDxS8l5"; //

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

    static void postItem(trip_Item Item){

        db.collection("cars").document(selectedCarId).collection("data")
            .add(Item.getMap())
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                }
            });

    }


}
