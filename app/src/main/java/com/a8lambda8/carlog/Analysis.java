package com.a8lambda8.carlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class Analysis extends AppCompatActivity {

    ArrayList<String> user;

    DatabaseReference mDatabase;

    int anz_Fahrten = 0;
    int KM_Ges, KM_Heute;

    Spinner SP_User;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference userRef = mDatabase.child("!user");


        init();



        //get Users for spinner

        Log.i("xx","asd");
        user = new ArrayList<>();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user.clear();
                for (DataSnapshot key : dataSnapshot.getChildren()) {
                    user.add(Integer.parseInt(key.getKey()),""+key.getValue());
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(Analysis.this, android.R.layout.simple_spinner_item, user);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                SP_User.setAdapter(spinnerArrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void init(){

        SP_User = (Spinner) findViewById(R.id.sp_user);
        SP_User.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                analyse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



    }


    void analyse(){
        //mDatabase.orderByChild()
        mDatabase.addValueEventListener(analyseListener);
    }


    ValueEventListener analyseListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            anz_Fahrten = 0;
            KM_Ges = 0;
            int prevZielKm = 0,ZielKm = 0;
            float dVerbrauch = 0, VerbrauchSUM = 0;
            for (DataSnapshot key : dataSnapshot.getChildren()) {
                if (!key.getKey().contains("!")&&
                        (key.child("Fahrer").getValue().equals(user.get((int) SP_User.getSelectedItemId()))||SP_User.getSelectedItemId()==4) &&
                        key.child("Tanken").getValue()==null) {
                    anz_Fahrten++;

                    int startKM = Integer.parseInt(key.child("Start").getValue().toString());
                    ZielKm=Integer.parseInt(key.child("Ziel").getValue().toString());

                    int dist = ZielKm-startKM;

                    if(dist>200&&KM_Ges!=0){
                        Log.i("xx","ACHTUNG über 200 KM ---"+dist+"--"+key.getKey());
                    }

                    KM_Ges += dist;
                    float verbrauch = Float.parseFloat(key.child("Verbrauch").getValue().toString());
                    VerbrauchSUM += verbrauch*dist;
                    //Log.i("xx",""+VerbrauchSUM+"---"+key.getKey());


                    if(verbrauch>20)Log.i("xx","ACHTUNG Verbrauch > 20 --- "+verbrauch+"--"+key.getKey());

                    if(dist<0){
                        Log.i("xx","ACHTUNG NEGATIV ---"+dist+"--"+key.getKey());
                    }

                    if(startKM!=prevZielKm&&SP_User.getSelectedItemId()==4){
                        Log.i("xx","ACHTUNG Start != letstes Ziel "+key.getKey()+"---"+(dist));
                    }
                    prevZielKm=ZielKm;


                }
            }
            dVerbrauch = VerbrauchSUM/(float)KM_Ges;


            Log.i("xx","-");
            Log.i("xx",SP_User.getSelectedItem()+":");
            Log.i("xx","anz "+anz_Fahrten);
            Log.i("xx","KM  "+KM_Ges);
            Log.i("xx","ØVerbr.  "+dVerbrauch);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

}
