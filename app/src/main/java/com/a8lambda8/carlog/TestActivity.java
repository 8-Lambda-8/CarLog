package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.a8lambda8.carlog.MainActivity.DBDateFormat;
import static com.a8lambda8.carlog.MainActivity.DbInt;
import static com.a8lambda8.carlog.MainActivity.DbString;
import static com.a8lambda8.carlog.MainActivity.DbVal;
import static com.a8lambda8.carlog.MainActivity.TimeParser;
import static com.a8lambda8.carlog.MainActivity.mDatabase;

public class TestActivity extends AppCompatActivity {


    DatabaseReference mRef;



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

        mRef = FirebaseDatabase.getInstance().getReference().child("cars/0");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot key : dataSnapshot.getChildren()) {

                    if (!Objects.requireNonNull(key.getKey()).contains("!")) {
                        //Log.d(TAG,""+key);
                        list_Item item = new list_Item();

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
        });



    }

    private void postItem(list_Item Item){



        //mDatabase.child(originalStartTime.format(DBDateFormat)).setValue(null);

        @SuppressLint("DefaultLocale")
        DatabaseReference itemRef =
                mRef.child("data")
                .child(Item.gettStart().format("Y%y/M%m/D%d/%H:%M:%S"));


        if(Item.getRefuel())
            itemRef.child("Tanken").setValue(true);
        else
            itemRef.child("EndZeit").setValue(""+Item.gettEnd().format(DBDateFormat));

        itemRef.child("Start").setValue(Item.getStart());
        itemRef.child("Ziel").setValue(Item.getEnd());

        itemRef.child("StartOrt").setValue(Item.getStartLoc());
        itemRef.child("ZielOrt").setValue(Item.getEndLoc());

        itemRef.child("Geschwindigkeit").setValue(Item.getSpeed());
        itemRef.child("Verbrauch").setValue(Item.getDrain());

        itemRef.child("Fahrer").setValue(Item.getDriverName());

    }

}
