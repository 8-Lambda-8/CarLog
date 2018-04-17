package com.a8lambda8.carlog;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

public class List extends AppCompatActivity {

    Spinner SP_driver,SP_refuel;

    TextView TV_ZB_Beg, TV_ZB_End;

    ListView LV;
    list_Item_list ItemList;
    list_adapter listAdapter;

    static Time zb_beg, zb_end;

    DatabaseReference mDatabase;

    final String DBdateFormat = "%y-%m-%d_%H-%M-%S";
    final String dateFormat = "%d.%m.%y  %H:%M";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
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

        zb_beg=new Time(Time.getCurrentTimezone());
        zb_end=new Time(Time.getCurrentTimezone());
        zb_beg.set(1,0,2017);
        zb_end.setToNow();

        init();

    }

    void init(){
        SP_driver = findViewById(R.id.sp_driver);
        SP_refuel = findViewById(R.id.sp_refuel);
        AdapterView.OnItemSelectedListener ISL = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLV();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        SP_driver.setOnItemSelectedListener(ISL);
        SP_refuel.setOnItemSelectedListener(ISL);



        TV_ZB_Beg = findViewById(R.id.tv_zb_beg);
        TV_ZB_End = findViewById(R.id.tv_zb_end);

        TV_ZB_Beg.setText(zb_beg.format(dateFormat));
        TV_ZB_End.setText(zb_end.format(dateFormat));

        TV_ZB_Beg.setOnClickListener(TV_zb_OnClickListener);
        TV_ZB_End.setOnClickListener(TV_zb_OnClickListener);

        ////List View
        LV = findViewById(R.id.LV_filter);
        ItemList = new list_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems());
        LV.setAdapter(listAdapter);

        ViewCompat.setNestedScrollingEnabled(LV, true);


    }

    View.OnClickListener TV_zb_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView tv = findViewById(v.getId());

            new DateTimePicker(tv, tv.getContext()).show();
        }
    };

    Time TimeParser(String time, String format){
        Time t = new Time(Time.getCurrentTimezone());
        if(format.charAt(1)=='y')
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(6,8)),Integer.parseInt(time.substring(3,5))-1,Integer.parseInt(time.substring(0,2)));
        else if(format.charAt(1)=='d')
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(0,2)),Integer.parseInt(time.substring(3,5))-1,Integer.parseInt(time.substring(6,8)));
        return t;
    }

    void updateLV(){
        mDatabase.removeEventListener(VEL);
        mDatabase.addValueEventListener(VEL);

    }

    ValueEventListener VEL = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int n = 0;
            ItemList.clear();

            for (DataSnapshot key : dataSnapshot.getChildren()) {

                boolean refuel = false;
                if (key.child("Tanken").getValue()!=null)
                    refuel = (boolean) key.child("Tanken").getValue();

                if (!key.getKey().contains("!")&&
                        (Objects.requireNonNull(key.child("Fahrer").getValue()).equals(SP_driver.getSelectedItem().toString())||SP_driver.getSelectedItemId()==0) &&
                        showRefuel(refuel)) {
                    Log.d("xx","nr: "+n+ "     refuel_Result:"+showRefuel(refuel));
                    n++;

                    list_Item item = new list_Item();

                    Time tS = TimeParser(key.getKey(),DBdateFormat);
                    Time tE = new Time(Time.getCurrentTimezone());

                    if (key.child("EndZeit").getValue() != null)
                        tE = TimeParser(""+DbVal(key,"EndZeit").toString(),DBdateFormat);

                    item.settStart(tS);
                    item.settEnd(tE);


                    if (DbVal(key,"Tanken") == null) {
                        //if (DbVal(key,"StartOrt") != null)
                            item.setStartLoc(DbString(key,"StartOrt"));

                        //if (key.child("ZielOrt").getValue() != null)
                            item.setEndLoc(DbString(key,"ZielOrt"));
                    }

                    if (key.child("Start").getValue() != null)
                        item.setStart(DbInt(key,"Start"));
                    if (key.child("Ziel").getValue() != null)
                        item.setEnd(DbInt(key,"Ziel"));

                    if (key.child("Geschwindigkeit").getValue() != null)
                        item.setSpeed(DbString(key,"Geschwindigkeit"));
                    if (key.child("Verbrauch").getValue() != null)
                        item.setDrain(DbString(key,"Verbrauch"));


                    if (key.child("Fahrer").getValue() != null)
                        item.setDriverName(DbString(key,"Fahrer"));

                    if (key.child("Tanken").getValue() != null)
                        item.setRefuel(Boolean.valueOf(DbString(key,"Tanken")));

                    if (key.child("Preis").getValue() != null)
                        item.setPrice(DbString(key,"Preis"));

                    ItemList.addItem(item);

                }
                listAdapter.notifyDataSetInvalidated();
            }
            //listAdapter.notifyDataSetInvalidated();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    boolean showRefuel(boolean refuel){
        Log.d("xx","refuel Bool: "+refuel+"   spinner: "+SP_refuel.getSelectedItemId());

        switch ((int) SP_refuel.getSelectedItemId()){
            case 0:{
                return !refuel;
            }
            case 1:{
                return refuel;
            }
            case 2:{
                return true;
            }
        }
        return true;
    }

    Object DbVal(DataSnapshot key,String child){
        return key.child(child).getValue();
    }

    int DbInt(DataSnapshot key,String child){
        if (DbVal(key,child)!=null)
            return Integer.valueOf(DbString(key,child));
        else return 0;
    }

    String DbString(DataSnapshot key,String child){
        if (DbVal(key,child)!=null)
            return DbVal(key,child).toString();
        else return  "";
    }

}
