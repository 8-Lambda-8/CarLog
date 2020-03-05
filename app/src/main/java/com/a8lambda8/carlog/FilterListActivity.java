package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;
import static com.a8lambda8.carlog.myUtils.DateFormat_dmyhm;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.currentCarDataRef;

public class FilterListActivity extends AppCompatActivity {

    Spinner SP_driver,SP_refuel;

    TextView TV_ZB_Beg, TV_ZB_End;

    CheckBox CB_Order;

    ListView LV;
    trip_Item_list ItemList;
    list_adapter listAdapter;

    List<Boolean> refuelCompareList;

    static Date zb_beg, zb_end;

    private ListenerRegistration registration;


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

        zb_beg=new Date();
        zb_end=new Date();

        currentCarDataRef.orderBy("startTime", Query.Direction.ASCENDING ).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = Objects.requireNonNull(task.getResult()).getDocuments().get(0);

                    try {
                        zb_beg.setTime(Objects.requireNonNull(DBDateFormat.parse((String) Objects.requireNonNull(document.get("startTime")))).getTime());
                        TV_ZB_Beg.setText(DateFormat_dmyhm.format(zb_beg));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        refuelCompareList = new ArrayList<>();

        init();

        updateListener();
        Log.d(TAG, "onCreate: ?");

    }

    void init(){

        ////Spinner
        SP_driver = findViewById(R.id.sp_driver);
        SP_refuel = findViewById(R.id.sp_refuel);
        AdapterView.OnItemSelectedListener ISL = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                switch (position){
                    case 0:
                        refuelCompareList.clear();
                        refuelCompareList.add(false);
                    break;
                    case 1:
                        refuelCompareList.clear();
                        refuelCompareList.add(true);
                    break;
                    case 2:
                        refuelCompareList.clear();
                        refuelCompareList.add(true);
                        refuelCompareList.add(false);
                    break;

                }

                updateListener();

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

        TV_ZB_Beg.setText(DateFormat_dmyhm.format(zb_beg));
        TV_ZB_End.setText(DateFormat_dmyhm.format(zb_end));

        TV_ZB_Beg.setOnClickListener(TV_zb_OnClickListener);
        TV_ZB_End.setOnClickListener(TV_zb_OnClickListener);

        ////Checkbox
        CB_Order = findViewById(R.id.cb_order);
        CB_Order.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listAdapter.setReverse(isChecked);

                updateListener();
                Log.d(TAG, "onCheckedChanged: "+ItemList.size());
            }
        });

        ////List View
        LV = findViewById(R.id.LV_filter);
        ItemList = new trip_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems(),false);
        listAdapter.setNotifyOnChange(true);
        LV.setAdapter(listAdapter);

        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent EntryEditor_i = new Intent(FilterListActivity.this, EntryEditor.class);

                trip_Item item = listAdapter.getItem(position);

                /*EntryEditor_i.putExtra("tStart", item.gettStart().toMillis(false));
                EntryEditor_i.putExtra("tEnd", item.gettEnd().toMillis(false));

                EntryEditor_i.putExtra("StartLoc", item.getStartLoc());
                EntryEditor_i.putExtra("EndLoc", item.getEndLoc());

                EntryEditor_i.putExtra("start", item.getStart());
                EntryEditor_i.putExtra("end", item.getEnd());

                EntryEditor_i.putExtra("speed", item.getSpeed());
                EntryEditor_i.putExtra("drain", item.getDrain());

                EntryEditor_i.putExtra("driverName", item.getDriverName());

                EntryEditor_i.putExtra("refuel", item.getRefuel());
                EntryEditor_i.putExtra("price", item.getPrice());*/

                EntryEditor_i.putExtra("id", Objects.requireNonNull(item).getID());

                //startActivity(EntryEditor_i);

            }
        });

        ViewCompat.setNestedScrollingEnabled(LV, true);

    }

    View.OnClickListener TV_zb_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            TextView tv = findViewById(v.getId());
            Log.d(TAG, "onClick: "+tv.getText());

            new DateTimePicker(tv, tv.getContext(),handler).show();
        }
    };

    void updateListener(){
        if(registration != null)
            registration.remove();

        Log.d(TAG, "zb_beg: " + DBDateFormat.format(zb_beg));
        Log.d(TAG, "zb_end: " + DBDateFormat.format(zb_end));

        registration = currentCarDataRef
                .whereGreaterThanOrEqualTo("startTime",DBDateFormat.format(zb_beg))
                .whereLessThanOrEqualTo("startTime",DBDateFormat.format(zb_end))
                //.orderBy("startTime", CB_Order.isChecked()? Query.Direction.ASCENDING:Query.Direction.DESCENDING)
                //.whereIn("refuel",refuelCompareList)
                //.whereEqualTo("driver",SP_driver.getSelectedItem())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        ItemList.clear();
                        listAdapter.notifyDataSetChanged();

                        Log.d(TAG, "documents: "+ Objects.requireNonNull(queryDocumentSnapshots).getDocuments().size());

                        for (DocumentSnapshot doc : Objects.requireNonNull(queryDocumentSnapshots).getDocuments()) {
                            Log.d(TAG, "data: "+doc.getData());
                            trip_Item item = new trip_Item();

                            item.setID(doc.getId());

                            //Time tS = TimeParser((String) doc.get("startTime"), DBDateFormat);
                            try {
                                item.settStart(DBDateFormat.parse((String) Objects.requireNonNull(doc.get("startTime"))));
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }

                            item.setRefuel((Boolean) doc.get("refuel"));

                            if (!item.getRefuel()) {
                                item.setStartLoc((String) doc.get("startLoc"));
                                item.setEndLoc((String) doc.get("endLoc"));

                                try {
                                    item.settEnd(DBDateFormat.parse((String) Objects.requireNonNull(doc.get("endTime"))));
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                            }

                            item.setStart(Math.toIntExact((long) doc.get("startKm")));
                            item.setEnd(Math.toIntExact((long) doc.get("endKm")));

                            item.setSpeed((String) doc.get("speed"));
                            item.setDrain((String) doc.get("drain"));

                            item.setDriverName((String) doc.get("driver"));

                            item.setPrice((String) doc.get("price"));

                            ItemList.addItem(item);

                        }
                    }
                });
    }

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

    @SuppressLint("HandlerLeak")
    android.os.Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            if(msg!=null) {
                switch (msg.arg1) {
                    case 0: {
                        Log.d(TAG, "handleMessage: ?");
                        updateListener();
                    }
                }
            }

        }

    };

}
