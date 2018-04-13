package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Analysis extends AppCompatActivity {

    ArrayList<String> user;

    DatabaseReference mDatabase;

    int anz_Fahrten = 0;
    int KM_Ges, KM_Heute;
    float dVerbrauch;
    int dGeschw;

    Spinner SP_User;

    TextView TV_anzFahrten, TV_KM_GES, TV_KM_Heute, TV_dGeschw, TV_dVerb;

    SharedPreferences SP;

    final String dateFormat = "%y-%m-%d";
    Time Heute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
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

        mDatabase = FirebaseDatabase.getInstance().getReference();
        SP = PreferenceManager.getDefaultSharedPreferences(this);

        Heute = new Time(Time.getCurrentTimezone());

        init();


    }

    void init(){
        ////Spinner
        SP_User = findViewById(R.id.sp_user);

        SP_User.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                analyse();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //get Users for spinner
        DatabaseReference userRef = mDatabase.child("!user");
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
        Log.i("xx",SP.getString("Fahrer","Kein Fahrer")+"="+user.lastIndexOf(SP.getString("Fahrer","Kein Fahrer")));
        SP_User.setSelection(user.lastIndexOf(SP.getString("Fahrer","Kein Fahrer")));


        ////TextViews
        TV_anzFahrten = findViewById(R.id.tv_anzFahrten);
        TV_KM_GES = findViewById(R.id.tv_KM_Ges);
        TV_KM_Heute = findViewById(R.id.tv_KM_Heute);
        TV_dGeschw = findViewById(R.id.tv_dGeschw);
        TV_dVerb = findViewById(R.id.tv_dVerb);

    }


    void analyse(){
        mDatabase.addValueEventListener(analyseListener);
    }

    ValueEventListener analyseListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            anz_Fahrten = 0;
            KM_Ges = 0;
            KM_Heute = 0;
            int prevZielKm = 0,ZielKm;
            dVerbrauch = 0;
            float VerbrauchSUM = 0;
            int GeschwSUM = 0;
            dGeschw = 0;
            Heute.setToNow();

            for (DataSnapshot key : dataSnapshot.getChildren()) {
                if (!key.getKey().contains("!")&&
                        (Objects.requireNonNull(key.child("Fahrer").getValue()).equals(user.get((int) SP_User.getSelectedItemId()))||SP_User.getSelectedItemId()==4) &&
                        key.child("Tanken").getValue()==null) {
                    anz_Fahrten++;

                    int startKM = Integer.parseInt(Objects.requireNonNull(key.child("Start").getValue()).toString());
                    ZielKm=Integer.parseInt(Objects.requireNonNull(key.child("Ziel").getValue()).toString());

                    int dist = ZielKm-startKM;

                    if(dist>200&&KM_Ges!=0){
                        Log.i("xx","ACHTUNG über 200 KM ---"+dist+"--"+key.getKey());
                    }

                    KM_Ges += dist;

                    //Log.i("xx",key.getKey().substring(0,8));

                    if(key.getKey().substring(0,8).equals(Heute.format(dateFormat))){
                       KM_Heute += dist;
                    }

                    float verbrauch = Float.parseFloat(Objects.requireNonNull(key.child("Verbrauch").getValue()).toString());
                    VerbrauchSUM += verbrauch*dist;

                    int Geschw = 0;
                    if(key.child("Geschwindigkeit").getValue()!=null)
                        Geschw = Integer.parseInt(Objects.requireNonNull(key.child("Geschwindigkeit").getValue()).toString());
                    else Log.i("xx",key.getKey());

                    GeschwSUM += Geschw*dist;
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
            Log.d("xx",GeschwSUM+"");
            if(KM_Ges!=0)
                dGeschw = GeschwSUM/KM_Ges;
            else dGeschw = 0;

            Log.i("xx","-");
            Log.i("xx",SP_User.getSelectedItem()+":");
            Log.i("xx","anz "+anz_Fahrten);
            Log.i("xx","KM  "+KM_Ges);
            Log.i("xx","KM Heutte "+KM_Heute);
            Log.i("xx","ØVerbr.  "+dVerbrauch);
            Log.i("xx","ØGeschw.  "+dGeschw);

            updateTV();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    @SuppressLint("SetTextI18n")
    void updateTV(){

        TV_anzFahrten.setText(""+anz_Fahrten);
        TV_KM_GES.setText(""+KM_Ges);
        TV_KM_Heute.setText(""+KM_Heute);
        TV_dGeschw.setText(""+dGeschw);
        TV_dVerb.setText(""+dVerbrauch);

    }

}
