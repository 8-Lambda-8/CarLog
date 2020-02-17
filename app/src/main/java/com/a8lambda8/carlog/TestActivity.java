package com.a8lambda8.carlog;

import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.Objects;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;
import static com.a8lambda8.carlog.myUtils.DBDateFormat_start;
import static com.a8lambda8.carlog.myUtils.DbInt;
import static com.a8lambda8.carlog.myUtils.DbString;
import static com.a8lambda8.carlog.myUtils.DbVal;
import static com.a8lambda8.carlog.myUtils.StartTimeStringParser;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.TimeParser;
import static com.a8lambda8.carlog.myUtils.db;
import static com.a8lambda8.carlog.myUtils.mAuth;
import static com.a8lambda8.carlog.myUtils.mDatabase_selectedCar;

public class TestActivity extends AppCompatActivity {


    //DatabaseReference mRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        //mRef = FirebaseDatabase.getInstance().getReference().child("cars/0");

        /*mDatabase.child("old").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot key : dataSnapshot.getChildren()) {

                    if (!Objects.requireNonNull(key.getKey()).contains("!")) {
                        //Log.d(TAG,""+key);
                        trip_Item item = new trip_Item();

                        Time tS = TimeParser(key.getKey(), DBDateFormat);
                        item.settStart(tS);

                        if (DbVal(key, "Tanken") == null) {
                            item.setStartLoc(DbString(key, "StartOrt"));
                            item.setEndLoc(DbString(key, "ZielOrt"));
                            Time tE = TimeParser("" + DbString(key, "EndZeit"), DBDateFormat);
                            item.settEnd(tE);
                        }

                        item.setStart(DbInt(key, "Start"));
                        item.setEnd(DbInt(key, "Ziel"));

                        item.setSpeed(DbString(key, "Geschwindigkeit"));
                        item.setDrain(DbString(key, "Verbrauch"));

                        item.setDriverName(DbString(key, "Fahrer"));

                        boolean refuel = false;
                        if (DbVal(key, "Tanken") != null)
                            refuel = (boolean) DbVal(key, "Tanken");


                        item.setRefuel(refuel);

                        item.setPrice(DbString(key, "Preis"));


                        postItem(item);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        Log.d(TAG, "onCreate: "+mAuth.getUid());

        mDatabase_selectedCar.child("data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {


                for (DataSnapshot key_Y : snap.getChildren()) {
                    for (DataSnapshot key_M : key_Y.getChildren()) {
                        for (DataSnapshot key_D : key_M.getChildren()) {
                            for (DataSnapshot key_t : key_D.getChildren()) {

                                trip_Item item = new trip_Item();

                                Time tS = TimeParser(StartTimeStringParser(key_t),DBDateFormat_start);
                                item.settStart(tS);

                                if (DbVal(key_t,"refuel") == null) {
                                    item.setStartLoc(DbString(key_t,"startLoc"));
                                    item.setEndLoc(DbString(key_t,"endLoc"));
                                    Time tE = TimeParser(""+DbString(key_t,"endTime"),DBDateFormat);
                                    item.settEnd(tE);
                                }

                                item.setStart(DbInt(key_t,"startKm"));
                                item.setEnd(DbInt(key_t,"endKm"));

                                item.setSpeed(DbString(key_t,"speed"));
                                item.setDrain(DbString(key_t,"drain"));

                                item.setDriverName(DbString(key_t,"driver"));

                                boolean refuel = false;
                                if (DbVal(key_t,"refuel")!=null)
                                    refuel = (boolean) DbVal(key_t,"refuel");

                                item.setRefuel(refuel);

                                item.setPrice(DbString(key_t,"Preis"));

                                postItem(item);

                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void postItem(trip_Item Item){

        /*@SuppressLint("DefaultLocale")
        DatabaseReference itemRef =
                mDatabase_selectedCar.child("data")
                .child(Item.gettStart().format(DBDateFormat_start));

        if(Item.getRefuel()) {
            itemRef.child("refuel").setValue(true);
            itemRef.child("price").setValue(Item.getPrice());
        }
        else
            itemRef.child("endTime").setValue(""+Item.gettEnd().format(DBDateFormat));

        itemRef.child("startKm").setValue(Item.getStart());
        itemRef.child("endKm").setValue(Item.getEnd());

        itemRef.child("startLoc").setValue(Item.getStartLoc());
        itemRef.child("endLoc").setValue(Item.getEndLoc());

        itemRef.child("speed").setValue(Item.getSpeed());
        itemRef.child("drain").setValue(Item.getDrain());

        itemRef.child("driver").setValue(Item.getDriverName());*/


        db.collection("cars").document("tUumoKgiA77OHX7JUTpY").collection("data")
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
