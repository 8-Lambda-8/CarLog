package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.ArraySet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EntryEditor extends AppCompatActivity {

    EditText ET_startLoc, ET_startKm, ET_endKm, ET_drain, ET_speed, ET_driver;
    AutoCompleteTextView ET_endLoc;
    TextView TV_start, TV_end, TV_dur;

    trip_Item Item;

    /*Time originalStartTime;

    Time duration;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_editor);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_ok = findViewById(R.id.fab_ok);
        FloatingActionButton fab_cancel = findViewById(R.id.fab_cancel);

        fab_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Log.d(MainActivity.TAG, String.valueOf(mDatabase.getPath()));

                postItem();


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 100);

            }
        });
        fab_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        Item = new trip_Item(Objects.requireNonNull(b));

        /*duration = new Time(Time.getCurrentTimezone());
        originalStartTime = new Time(Time.getCurrentTimezone());
        originalStartTime.set(Item.gettStart().toMillis(false));*/

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);



        ////EditText

        ET_startLoc = findViewById(R.id.et_startLoc2);
        ET_endLoc = findViewById(R.id.et_endLoc2);
        ET_startKm = findViewById(R.id.et_startKm2);
        ET_endKm = findViewById(R.id.et_endKm2);
        ET_drain = findViewById(R.id.et_drain2);
        ET_speed = findViewById(R.id.et_speed2);
        ET_driver = findViewById(R.id.et_driver);

        final Set<String> def = new ArraySet<>();
        def.add("Elbigenalp");
        def.add("!!Default Values");

        List<String> AutoComplete = new ArrayList<>(Objects.requireNonNull(Objects.requireNonNull(SP.getStringSet("!locations", def))));

        ArrayAdapter<String> autoCompleteAdapter;
        autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, AutoComplete);

        ET_endLoc.setAdapter(autoCompleteAdapter);


        ET_startLoc.setText(Item.getStartLoc());
        ET_endLoc.setText(Item.getEndLoc());
        ET_startKm.setText(Item.getStart());
        ET_endKm.setText(Item.getEnd());
        ET_drain.setText(Item.getDrain());
        ET_speed.setText(Item.getSpeed());
        ET_driver.setText(Item.getDriverName());


        ////TextViews
        TV_start = findViewById(R.id.tv_start2);
        TV_end = findViewById(R.id.tv_end2);
        TV_dur = findViewById(R.id.tv_dur2);


        /*TV_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Item.gettStart().toMillis(true)>5000){
                    new TIME_picker(EntryEditor.this,Item.gettStart(), Handler,"Start Zeit eingeben:",0);
                }
            }
        });

        TV_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Item.gettEnd().toMillis(true)>5000){
                    new TIME_picker(EntryEditor.this,Item.gettEnd(), Handler,"End Zeit eingeben:",1);
                }

            }
        });

        TV_start.setText(Item.gettStart().format("Start Zeit: %d.%m.  %H:%M:%S"));
        TV_end.setText(Item.gettEnd().format("End  Zeit: %d.%m.  %H:%M:%S"));*/

        updateDur();

    }


    private void postItem(){

        /*mDatabase.child(originalStartTime.format(DBDateFormat)).setValue(null);



        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("EndZeit").setValue(""+Item.gettEnd().format(DBDateFormat));

        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("Start").setValue(""+ET_startKm.getText());
        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("Ziel").setValue(""+ET_endKm.getText());

        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("Geschwindigkeit").setValue(""+ET_speed.getText());
        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("Verbrauch").setValue(""+ET_drain.getText());

        mDatabase.child(Item.gettStart().format(DBDateFormat)).child("Fahrer").setValue(""+ET_driver.getText());*/


    }


    @SuppressLint("HandlerLeak")
    private android.os.Handler Handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if(msg!=null){
                switch (msg.arg1){
                    case 0:{
                        /*Item.gettStart().set((Time) msg.obj);
                        TV_start.setText(((Time) msg.obj).format("Start Zeit: %d.%m.  %H:%M:%S"));*/
                        updateDur();
                        break;
                    }
                    case 1:{
                        /*Item.gettEnd().set((Time) msg.obj);
                        TV_end.setText(((Time) msg.obj).format("End Zeit: %d.%m.  %H:%M:%S"));*/

                        updateDur();
                        break;
                    }/*
                    case 2:{
                        ET_startKm.setText(String.valueOf(msg.arg2));
                        //fahrt.setZielKM(msg.arg2);
                        //ref.child("ZielKM").setValue(msg.arg2);
                        break;
                    }
                    case 3:{
                        ET_endKm.setText(String.valueOf(msg.arg2));
                        //fahrt.setRückAbholKM(msg.arg2);
                        //ref.child("RückAbholKM").setValue(msg.arg2);
                        break;
                    }*/
                }
            }
        }
    };

    private void updateDur(){

        /*duration.set(Item.gettEnd().toMillis(false) - Item.gettStart().toMillis(false)-3600000);

        TV_dur.setText(duration.format("Dauer:       %H:%M:%S"));*/
    }


}
