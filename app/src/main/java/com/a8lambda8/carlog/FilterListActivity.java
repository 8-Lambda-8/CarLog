package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.a8lambda8.carlog.myUtils.DBDateFormat_start;
import static com.a8lambda8.carlog.myUtils.StartTimeStringParser;
import static com.a8lambda8.carlog.myUtils.TimeParser;
import static com.a8lambda8.carlog.myUtils.mDatabase_selectedCar;

public class FilterListActivity extends AppCompatActivity {

    Spinner SP_driver,SP_refuel;

    TextView TV_ZB_Beg, TV_ZB_End;

    CheckBox CB_Order;

    ListView LV;
    trip_Item_list ItemList;
    list_adapter listAdapter;

    static Time zb_beg, zb_end;

    final String DBdateFormat = "%y-%m-%d_%H-%M-%S";
    final String dateFormat = "%d.%m.%y  %H:%M";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab_ok);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        zb_beg=new Time(Time.getCurrentTimezone());
        zb_end=new Time(Time.getCurrentTimezone());
        zb_beg.set(1,0,2017);
        zb_end.setToNow();

        init();

    }

    void init(){

        ////Spinner
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

        ////TextViews
        TV_ZB_Beg = findViewById(R.id.tv_zb_beg);
        TV_ZB_End = findViewById(R.id.tv_zb_end);

        TV_ZB_Beg.setText(zb_beg.format(dateFormat));
        TV_ZB_End.setText(zb_end.format(dateFormat));

        TV_ZB_Beg.setOnClickListener(TV_zb_OnClickListener);
        TV_ZB_End.setOnClickListener(TV_zb_OnClickListener);

        ////Checkbox
        CB_Order = findViewById(R.id.cb_order);
        CB_Order.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listAdapter.setReverse(isChecked);
                updateLV();
            }
        });

        ////List View
        LV = findViewById(R.id.LV_filter);
        ItemList = new trip_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems(),false);
        LV.setAdapter(listAdapter);

        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent EntryEditor_i = new Intent(FilterListActivity.this, EntryEditor.class);

                /*if (listAdapter.reverse)
                    position = listAdapter.ge*/

                trip_Item item = listAdapter.getItem(position);

                EntryEditor_i.putExtra("tStart",item.gettStart().toMillis(false));
                EntryEditor_i.putExtra("tEnd",item.gettEnd().toMillis(false));

                EntryEditor_i.putExtra("StartLoc",item.getStartLoc());
                EntryEditor_i.putExtra("EndLoc",item.getEndLoc());

                EntryEditor_i.putExtra("start",item.getStart());
                EntryEditor_i.putExtra("end",item.getEnd());

                EntryEditor_i.putExtra("speed",item.getSpeed());
                EntryEditor_i.putExtra("drain",item.getDrain());

                EntryEditor_i.putExtra("driverName",item.getDriverName());


                EntryEditor_i.putExtra("refuel",item.getRefuel());
                EntryEditor_i.putExtra("price",item.getPrice());



                startActivity(EntryEditor_i);

            }
        });

        ViewCompat.setNestedScrollingEnabled(LV, true);

    }

    View.OnClickListener TV_zb_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView tv = findViewById(v.getId());

            new DateTimePicker(tv, tv.getContext(),handler).show();
        }
    };

    void updateLV(){
        mDatabase_selectedCar.child("data").removeEventListener(VEL);
        mDatabase_selectedCar.child("data").addValueEventListener(VEL);
    }

    ValueEventListener VEL = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            //int n = 0;
            ItemList.clear();

            for (DataSnapshot key_Y : dataSnapshot.getChildren()) {
                for (DataSnapshot key_M : key_Y.getChildren()) {
                    for (DataSnapshot key_D : key_M.getChildren()) {
                        for (DataSnapshot key_t : key_D.getChildren()) {

                            boolean refuel = false;
                            if (DbVal(key_t, "refuel") != null)
                                refuel = (boolean) DbVal(key_t, "refuel");


                            if (key_t.child("refuel").getValue() == null)
                                continue;

                            if (!(Objects.equals(key_t.child("driver").getValue(), SP_driver.getSelectedItem().toString())
                                    ||
                                    SP_driver.getSelectedItemId() == 0))
                                continue;

                            if ((zb_beg.toMillis(false) <= TimeParser(key_t.getKey()
                                    , DBDateFormat_start).toMillis(false) && zb_end.toMillis(false) > TimeParser(key_t.getKey()
                                    , DBdateFormat).toMillis(false)) &&
                                    showRefuel(refuel)) {

                                trip_Item item = new trip_Item();

                                Time tS = TimeParser(StartTimeStringParser(key_t),DBDateFormat_start);

                                item.settStart(tS);

                                if (!refuel) {
                                    item.setStartLoc(DbString(key_t, "startLoc"));
                                    item.setEndLoc(DbString(key_t, "endLoc"));

                                    Time tE = TimeParser("" + DbString(key_t, "endTime"), DBdateFormat);
                                    item.settEnd(tE);
                                }

                                item.setStart(DbInt(key_t, "startKm"));
                                item.setEnd(DbInt(key_t, "endKm"));

                                item.setSpeed(DbString(key_t, "speed"));
                                item.setDrain(DbString(key_t, "drain"));

                                item.setDriverName(DbString(key_t, "driver"));

                                item.setRefuel(refuel);

                                item.setPrice(DbString(key_t, "price"));

                                ItemList.addItem(item);

                            }
                            listAdapter.notifyDataSetInvalidated();
                        }
                    }
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    boolean showRefuel(boolean refuel){
        //Log.d("xx","refuel Bool: "+refuel+"   spinner: "+SP_refuel.getSelectedItemId());

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

    @SuppressLint("HandlerLeak")
    android.os.Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            if(msg!=null) {
                switch (msg.arg1) {
                    case 0: {
                        updateLV();
                    }
                }
            }

        }

    };

}
