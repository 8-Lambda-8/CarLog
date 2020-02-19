package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;
import static com.a8lambda8.carlog.myUtils.DBDateFormat_start;
import static com.a8lambda8.carlog.myUtils.RC_SIGN_IN;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.TestDevice;
import static com.a8lambda8.carlog.myUtils.TimeParser;
import static com.a8lambda8.carlog.myUtils.currentCarDataRef;
import static com.a8lambda8.carlog.myUtils.currentCarRef;
import static com.a8lambda8.carlog.myUtils.db;
import static com.a8lambda8.carlog.myUtils.mAuth;
import static com.a8lambda8.carlog.myUtils.postItem;
import static com.a8lambda8.carlog.myUtils.updateCarRefs;

//import static com.a8lambda8.carlog.myUtils.mDatabase;
//import static com.a8lambda8.carlog.myUtils.mDatabase_selectedCar;


public class MainActivity extends AppCompatActivity {

    Button B_start, B_stop, B_add, B_cls;
    EditText ET_startLoc, ET_startKm, ET_endKm, ET_drain, ET_speed;
    AutoCompleteTextView ET_endLoc;
    TextView TV_start, TV_end, TV_dur;
    ListView LV;
    trip_Item_list ItemList;
    list_adapter listAdapter;
    Spinner SP_CarSelect;

    Boolean started = false;

    Time timeStart, timeEnd, duration, currTime;

    SharedPreferences SP;
    SharedPreferences.Editor SPEdit;

    FirebaseUser currentUser;

    String username = "";

    public List<String> AutoComplete = new ArrayList<>();
    ArrayAdapter<String> autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        DBDateFormat = getString(R.string.db_dateformat);
        DBDateFormat_start = getString(R.string.db_dateformat_start);

        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {

            //List<AuthUI.IdpConfig> providers = Arrays.asList(
            @SuppressLint("WrongConstant") List<AuthUI.IdpConfig> providers = Collections.singletonList(
                    //new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    //new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
            return;
        }

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SPEdit = SP.edit();
        SPEdit.apply();

        db = FirebaseFirestore.getInstance();
        updateCarRefs();

        if(currentUser!=null) {
            //Log.d(TAG,"Display Name: "+currentUser.getDisplayName());
            //Log.d(TAG, "EMAIL: " + currentUser.getEmail());
            if (currentUser.getEmail() != null && !Objects.equals(currentUser.getEmail(), ""))
                TestDevice = !("j.wasle111@gmail.com;leow707@gmail.com;??".contains(Objects.requireNonNull(currentUser.getEmail())));
            username = Objects.requireNonNull(currentUser.getDisplayName()).split(" ")[0];
        }    //Log.d(TAG, "is test Device:"+ TestDevice);
        //if (TestDevice) mDatabase = mDatabase.child("!Test");

        fab();

        timeStart = initTime();
        timeEnd = initTime();
        duration = initTime();
        currTime = initTime();

        autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, AutoComplete);


        final Set<String> def = new ArraySet<>();
        def.add("Elbigenalp");
        def.add("!!Default Values");

        AutoComplete.addAll(Objects.requireNonNull(Objects.requireNonNull(SP.getStringSet("!locations", def))));
        autoCompleteAdapter.setNotifyOnChange(true);

        currentCarRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {

                    AutoComplete.clear();

                    if(snapshot.get("locations")!=null) {
                        Set<String> loc = new ArraySet<>();
                        loc.addAll((Collection<? extends String>) snapshot.get("locations"));

                        SPEdit.putStringSet("!locations",loc);

                        AutoComplete.addAll(Objects.requireNonNull(Objects.requireNonNull(SP.getStringSet("!locations", def))));
                    }


                    updateAutoCompleteAdapter();

                    if(snapshot.get("SP_sync")!=null) {
                        Map<String, Object> map = (Map<String, Object>) snapshot.get("SP_sync");

                        SPEdit.putString("lastRefuel", (String) map.get("lastRefuel"));
                        SPEdit.putString("lastKm", (String) map.get("lastKm"));
                        SPEdit.putString("lastLoc", (String) map.get("lastLoc"));

                        SPEdit.apply();
                    }

                } else {
                    Log.d(TAG, source + " data: null");
                }
            }


        });

        init();


        started = SP.getBoolean("started",false);

        timeStart.set(SP.getLong("timeStart",2000));

        if(timeStart.toMillis(false)>2000) {
            started = true;
            TV_start.setText(timeStart.format("Start Zeit: %d.%m.  %H:%M:%S"));
        }

        timeEnd.set(SP.getLong("timeEnd",2000));
        if(timeEnd.toMillis(false)>5000) {
            started = false;
            TV_end.setText(timeEnd.format("End  Zeit: %d.%m.  %H:%M:%S"));
            duration.set(timeEnd.toMillis(false) - timeStart.toMillis(false)-3600000);
            TV_dur.setText(duration.format("Dauer:      %H:%M:%S"));
        }

        if(started)startDurUpdater();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_refuel) {
            refuel();
            return true;
        }
        /*if (id == R.id.action_changeUsername) {
            changeUsername(true);
            return true;
        }*/
        if (id == R.id.action_analysis){
            Intent analysis_i = new Intent(this, AnalysisActivity.class);
            startActivity(analysis_i);
            return true;
        }
        if (id == R.id.action_List){
            Intent analysis_i = new Intent(this, FilterListActivity.class);
            startActivity(analysis_i);
            return true;
        }
        if (id == R.id.action_testMenu&& currentUser.getUid().equals("faCZuGYR27MDEvN65ojT7QSCELk1")){
            Intent analysis_i = new Intent(this, TestActivity.class);
            startActivity(analysis_i);
            return true;
        }
        if (id == R.id.action_test2&& currentUser.getUid().equals("faCZuGYR27MDEvN65ojT7QSCELk1")){
            db.collection("cars").document("tUumoKgiA77OHX7JUTpY")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (Objects.requireNonNull(document).exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
            return true;
        }
        if(id == R.id.action_logOut){
            AuthUI.getInstance()
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {

                            try {
                                mAuth.signOut();
                            } catch (Exception e){
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                            }

                            /*System.exit(1);*/
                        }
                    });
        }
        if(id == R.id.action_carSettings){
            Intent action_carSettings_i = new Intent(this, CarSettingsActivity.class);
            startActivity(action_carSettings_i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fab(){
        FloatingActionButton fab = findViewById(R.id.fab_ok);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
    }

    private void init(){

        ////EditText
        ET_startLoc = findViewById(R.id.et_startLoc);
        ET_endLoc = findViewById(R.id.et_endLoc);
        ET_startKm = findViewById(R.id.et_startKm);
        ET_endKm = findViewById(R.id.et_endKm);
        ET_drain = findViewById(R.id.et_drain);
        ET_speed = findViewById(R.id.et_speed);

        ET_endLoc.setAdapter(autoCompleteAdapter);

        ET_startLoc.setText(SP.getString("StartLoc",""));
        ET_endLoc.setText(SP.getString("EndLoc",""));
        ET_startKm.setText(SP.getString("StartKm",""));
        ET_endKm.setText(SP.getString("EndKm",""));
        ET_drain.setText(SP.getString("drain",""));
        ET_speed.setText(SP.getString("speed",""));

        ET_startLoc.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("StartLoc",""+editable);
                SPEdit.apply();
                addable();

            }
        });
        ET_endLoc.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("EndLoc",""+editable);
                SPEdit.apply();
                addable();

            }
        });
        ET_startKm.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("StartKm",""+editable);
                SPEdit.apply();
                addable();

            }
        });
        ET_endKm.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("EndKm",""+editable);
                SPEdit.apply();
                addable();

            }
        });
        ET_drain.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("drain",""+editable);
                SPEdit.apply();
                addable();

            }
        });
        ET_speed.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                SPEdit.putString("speed",""+editable);
                SPEdit.apply();
                addable();

            }
        });

        ET_startKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!B_start.isEnabled()) {
                    new KMpicker(MainActivity.this, Integer.parseInt(ET_startKm.getText().toString()), Handler, "Start KM eingeben:", 2);
                }
            }
        });
        ET_endKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in = 0;
                if(!B_start.isEnabled()) {
                    if (ET_endKm.getText() != null && !Objects.equals(ET_endKm.getText().toString(), "")) {
                        in = Integer.parseInt(ET_endKm.getText().toString());
                    }
                    if (in <= 0) {
                        in = Integer.parseInt(ET_startKm.getText().toString());
                    }
                    new KMpicker(MainActivity.this, in, Handler, "End KM eingeben:", 3);
                }
            }
        });

        ////TextViews
        TV_start = findViewById(R.id.tv_start);
        TV_end = findViewById(R.id.tv_end);
        TV_dur = findViewById(R.id.tv_dur);


        TV_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeStart.toMillis(true)>5000){
                    new TIME_picker(MainActivity.this,timeStart, Handler,"Start Zeit eingeben:",0);
                }
            }
        });

        TV_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!started&&timeEnd.toMillis(true)>5000){
                    new TIME_picker(MainActivity.this,timeEnd, Handler,"End Zeit eingeben:",1);
                }

            }

        });

        ////Buttons
        B_start = findViewById(R.id.b_start);
        B_stop = findViewById(R.id.b_end);
        B_add = findViewById(R.id.b_add);
        B_cls = findViewById(R.id.b_cls);

        B_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeStart.setToNow();
                SPEdit.putLong("timeStart",timeStart.toMillis(true));
                SPEdit.apply();
                TV_start.setText(timeStart.format("Start Zeit: %d.%m.  %H:%M:%S"));

                ET_startKm.setText(SP.getString("lastKm",""));
                ET_startLoc.setText(SP.getString("lastLoc",""));

                B_start.setEnabled(false);
                B_stop.setEnabled(true);

                startDurUpdater();
            }
        });
        Log.d(TAG,"started: " + started);
        if (started||(timeStart.toMillis(false)>2000)&&!(timeStart.toMillis(false)>2000)){
            B_start.setEnabled(false);
            B_stop.setEnabled(true);
        }

        B_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeEnd.setToNow();
                SPEdit.putLong("timeEnd",timeEnd.toMillis(true));
                SPEdit.apply();
                TV_end.setText(timeEnd.format("End  Zeit: %d.%m.  %H:%M:%S"));
                started = false;
                addable();
            }
        });

        B_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                trip_Item item = new trip_Item();

                item.settStart(timeStart);
                item.settEnd(timeEnd);

                item.setStartLoc(ET_startLoc.getText().toString());
                item.setEndLoc(ET_endLoc.getText().toString());

                item.setStart(Integer.parseInt(ET_startKm.getText().toString()));
                item.setEnd(Integer.parseInt(ET_endKm.getText().toString()));

                item.setSpeed(ET_speed.getText().toString());
                item.setDrain(ET_drain.getText().toString());

                item.setDriverName(username);
                item.setDriverId(mAuth.getUid());

                postItem(item);

                SPEdit.putString("lastKm",ET_endKm.getText().toString());
                SPEdit.putString("lastLoc",ET_endLoc.getText().toString());
                SPEdit.apply();

                currentCarRef.update("SP_sync",
                        "lastLoc",ET_endLoc.getText().toString(),
                        "lastKm",ET_endKm.getText().toString());

                currentCarRef.update("locations", FieldValue.arrayUnion(ET_endLoc.getText().toString()));


                try {
                        Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                clear();

            }
        });
        B_cls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(
                        MainActivity.this);
                alert.setTitle("Alert!!");
                alert.setMessage("Are you sure to delete ");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do your work here
                        dialog.dismiss();

                        clear();
                        started = false;

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();

            }
        });

        ////ListView
        LV = findViewById(R.id.fahrten);
        ItemList = new trip_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems(),true);
        LV.setAdapter(listAdapter);

        Time t = initTime();
        t.setToNow();
        t.set(t.toMillis(false)-((long) 1000*60*60*24*30*6*5));

        currentCarDataRef
                .whereGreaterThan("startTime", t.format(DBDateFormat))
                //.orderBy("startTime")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                            trip_Item item = new trip_Item();

                            item.setID(doc.getId());

                            Time tS = TimeParser((String) doc.get("startTime"), DBDateFormat);
                            item.settStart(tS);

                            item.setRefuel((Boolean) doc.get("refuel"));

                            if (!item.getRefuel()) {
                                item.setStartLoc((String) doc.get("startLoc"));
                                item.setEndLoc((String) doc.get("endLoc"));
                                Time tE = TimeParser((String) doc.get("endTime"), DBDateFormat);
                                item.settEnd(tE);
                            }

                            item.setStart(Math.toIntExact((long)doc.get("startKm")));
                            item.setEnd(Math.toIntExact((long)doc.get("endKm")));

                            item.setSpeed((String) doc.get("speed"));
                            item.setDrain((String) doc.get("drain"));

                            item.setDriverName((String) doc.get("driver"));

                            item.setPrice((String) doc.get("price"));

                            ItemList.addItem(item);

                            listAdapter.notifyDataSetInvalidated();
                        }

                    }
                });

        addable();

        SP_CarSelect = findViewById(R.id.SP_carSelect);
        SP_CarSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final List<CarSpinnerItem> cars = new ArrayList<>();

        final ArrayAdapter<CarSpinnerItem> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cars);

        SP_CarSelect.setAdapter(dataAdapter);

        /*mDatabase.child("user").child(currentUser.getUid()).child("cars").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "cars of User: "+dataSnapshot.getValue());

                //int[] carIds = dataSnapshot.getValue();

                cars.clear();

                for (DataSnapshot key : dataSnapshot.getChildren()) {
                    cars.add(new CarSpinnerItem((long)key.getValue(),""));
                }

                dataAdapter.notifyDataSetInvalidated();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

    }

    public Time initTime(){
        return new Time(Time.getCurrentTimezone());
    }

    private void startDurUpdater(){

        started = true;

        B_stop.setEnabled(true);
        B_start.setEnabled(false);

        @SuppressLint("HandlerLeak") final Handler updater = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                duration.set(currTime.toMillis(false) - timeStart.toMillis(false)-3600000);

                TV_dur.setText(duration.format("Dauer:       %H:%M:%S"));
            }
        };

        new Thread( new Runnable() {
            @Override
            public void run() {
                int last = 0;
                while (started) {

                    currTime.setToNow();
                    if (currTime.second != last) {
                        updater.sendMessage(new Message());
                        last = currTime.second;
                    }
                }
            }
        }).start();
    }

    private void updateDur(){

        duration.set(timeEnd.toMillis(false) - timeStart.toMillis(false)-3600000);

        TV_dur.setText(duration.format("Dauer:       %H:%M:%S"));
    }

    private void clear(){

        timeStart.set(2000);
        timeEnd.set(2000);
        duration.set(2000);
        currTime.set(2000);

        SPEdit.putLong("timeStart",timeStart.toMillis(true));
        SPEdit.putLong("timeEnd",timeStart.toMillis(true));

        ET_startLoc.setText("");
        ET_endLoc.setText("");
        ET_startKm.setText("");
        ET_endKm.setText("");
        ET_drain.setText("");
        ET_speed.setText("");

        TV_start.setText(timeStart.format("Start Zeit: 00.00.  00:00:00"));
        TV_end.setText(timeEnd.format("End  Zeit: 00.00.  00:00:00"));
        TV_dur.setText(duration.format("Dauer:      00:00:00"));

        B_start.setEnabled(true);

        addable();

    }

    private void addable(){

        if(started||
                Objects.equals(ET_startLoc.getText().toString(), "")||
                Objects.equals(ET_endLoc.getText().toString(), "")||
                Objects.equals(ET_startKm.getText().toString(), "")||
                Objects.equals(ET_endKm.getText().toString(), "")||
                Objects.equals(ET_drain.getText().toString(), "")||
                Objects.equals(ET_speed.getText().toString(), "")||
                timeEnd.toMillis(false)<=20000||
                timeStart.toMillis(false)<=20000
                ){
            B_add.setEnabled(false);
        }else{
            B_add.setEnabled(true);
        }

    }

    private void refuel(){

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Tanken");

        @SuppressLint("InflateParams") View alertView = getLayoutInflater().inflate(R.layout.refuel,null);

        final EditText startKm = alertView.findViewById(R.id.etStart);
        final EditText endKm = alertView.findViewById(R.id.etEnd);

        final EditText drain = alertView.findViewById(R.id.etDrain);
        final EditText speed = alertView.findViewById(R.id.etSpeed);

        final EditText price = alertView.findViewById(R.id.etPrice);

        startKm.setText(SP.getString("lastRefuel","0"));

        alert.setView(alertView);
        alert.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                Time t = initTime();
                t.setToNow();

                trip_Item item = new trip_Item();

                item.settStart(t);

                item.setStart(Integer.parseInt(startKm.getText().toString()));
                item.setEnd(Integer.parseInt(endKm.getText().toString()));

                item.setSpeed(speed.getText().toString());
                item.setDrain(drain.getText().toString());

                item.setDriverName(username);
                item.setDriverId(mAuth.getUid());

                item.setRefuel(true);

                item.setPrice(price.getText().toString());

                postItem(item);

            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    @SuppressLint("HandlerLeak")
    private android.os.Handler Handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            if(msg!=null){
                switch (msg.arg1){
                    case 0:{
                        timeStart.set((Time) msg.obj);
                        TV_start.setText(((Time) msg.obj).format("Start Zeit: %d.%m.  %H:%M:%S"));
                        updateDur();
                        break;
                    }
                    case 1:{
                        timeEnd.set((Time) msg.obj);
                        TV_end.setText(((Time) msg.obj).format("End Zeit: %d.%m.  %H:%M:%S"));
                        updateDur();
                        break;
                    }
                    case 2:{
                        ET_startKm.setText(String.valueOf(msg.arg2));
                        break;
                    }
                    case 3:{
                        ET_endKm.setText(String.valueOf(msg.arg2));
                        break;
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG,""+response);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG,""+user);

                Intent intent = getIntent();
                finish();
                startActivity(intent);

            } else {
                // Sign in failed, check response for error code
                System.exit(1);
            }
        }
    }

    void updateAutoCompleteAdapter(){
        autoCompleteAdapter.clear();
        autoCompleteAdapter.addAll(AutoComplete);
    }

}