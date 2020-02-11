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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;
import static com.a8lambda8.carlog.myUtils.DBDateFormat_start;
import static com.a8lambda8.carlog.myUtils.DbInt;
import static com.a8lambda8.carlog.myUtils.DbString;
import static com.a8lambda8.carlog.myUtils.DbVal;
import static com.a8lambda8.carlog.myUtils.RC_SIGN_IN;
import static com.a8lambda8.carlog.myUtils.StartTimeStringParser;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.TestDevice;
import static com.a8lambda8.carlog.myUtils.TimeParser;
import static com.a8lambda8.carlog.myUtils.mDatabase;
import static com.a8lambda8.carlog.myUtils.mDatabase_selectedCar;
import static com.a8lambda8.carlog.myUtils.selectedCarId;


public class MainActivity extends AppCompatActivity {

    Button B_start, B_stop, B_add, B_cls;
    EditText ET_startLoc, ET_startKm, ET_endKm, ET_drain, ET_speed;
    AutoCompleteTextView ET_endLoc;
    TextView TV_start, TV_end, TV_dur;
    ListView LV;
    list_Item_list ItemList;
    list_adapter listAdapter;

    Boolean started = false;

    Time timeStart, timeEnd, duration, currTime;

    SharedPreferences SP;
    SharedPreferences.Editor SPEdit;

    FirebaseAuth mAuth;

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

        Intent intent = getIntent();

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

        mDatabase = FirebaseDatabase.getInstance().getReference();//.child("old");

        selectedCarId = SP.getInt("selectedCarId",2);

        mDatabase_selectedCar = mDatabase.child("cars/"+selectedCarId);

        //Log.d(TAG, "onCreate: "+mDatabase_selectedCar.getPath());

        if(currentUser!=null) {
            //Log.d(TAG,"Display Name: "+currentUser.getDisplayName());
            //Log.d(TAG, "EMAIL: " + currentUser.getEmail());
            if (currentUser.getEmail() != null && !Objects.equals(currentUser.getEmail(), ""))
                TestDevice = !("j.wasle111@gmail.com;leow707@gmail.com;??".contains(Objects.requireNonNull(currentUser.getEmail())));
            username = Objects.requireNonNull(currentUser.getDisplayName()).split(" ")[0];
        }    //Log.d(TAG, "is test Device:"+ TestDevice);
        if (TestDevice) mDatabase = mDatabase.child("!Test");

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


        mDatabase_selectedCar.child("locations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AutoComplete.clear();
                //autoCompleteAdapter.clear();
                Set<String> loc = new ArraySet<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    loc.add((String) postSnapshot.getValue());
                }

                SPEdit.putStringSet("!locations",loc);
                SPEdit.apply();

                AutoComplete.addAll(Objects.requireNonNull(Objects.requireNonNull(SP.getStringSet("!locations", def))));

                updateAutoCompleteAdapter();

                /*Log.d(TAG,"Loaded Locations:\n"+AutoComplete);
                Log.d(TAG,"AutoComplete cnt: "+AutoComplete.size());
                Log.d(TAG,"Adapter cnt: "+autoCompleteAdapter.getCount());*/


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabase_selectedCar.child("SP_Sync").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                SPEdit.putString("lastRefuel", String.valueOf(dataSnapshot.child("lastRefuel").getValue()));
                SPEdit.putString("lastKm", String.valueOf(dataSnapshot.child("lastKm").getValue()));
                SPEdit.putString("lastLoc", String.valueOf(dataSnapshot.child("lastLoc").getValue()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        init();


        started = SP.getBoolean("started",false);

        timeStart.set(SP.getLong("timeStart",2000));

        if(timeStart.toMillis(false)>2000) {
            started = true;
            //Log.i(TAG,""+"timeStart.toMillis(false)>2000");
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
            Intent analysis_i = new Intent(this, Analysis.class);
            startActivity(analysis_i);
            return true;
        }
        if (id == R.id.action_List){
            Intent analysis_i = new Intent(this, FilterList.class);
            startActivity(analysis_i);
            return true;
        }
        if (id == R.id.action_testMenu&& currentUser.getUid().equals("faCZuGYR27MDEvN65ojT7QSCELk1")){
            /*Intent analysis_i = new Intent(this, TestActivity.class);
            startActivity(analysis_i);*/


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
                    Log.i(TAG, "asd " + ET_endKm.getText());
                    if (ET_endKm.getText() != null && !Objects.equals(ET_endKm.getText().toString(), "")) {
                        in = Integer.parseInt(ET_endKm.getText().toString());
                    }
                    Log.i(TAG, "in:" + in);
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

                DatabaseReference startTimeRef = mDatabase_selectedCar.child("data/"+timeStart.format(DBDateFormat_start));

                SPEdit.putString("lastKm",ET_endKm.getText().toString());
                mDatabase_selectedCar.child("SP_Sync").child("lastKm").setValue(ET_endKm.getText().toString());
                SPEdit.putString("lastLoc",ET_endLoc.getText().toString());
                mDatabase_selectedCar.child("SP_Sync").child("lastLoc").setValue(ET_endLoc.getText().toString());
                SPEdit.apply();

                startTimeRef.child("endTime").setValue(""+timeEnd.format(DBDateFormat));

                startTimeRef.child("startKm").setValue(""+ET_startKm.getText());
                startTimeRef.child("endKm").setValue(""+ET_endKm.getText());

                startTimeRef.child("startLoc").setValue(""+ET_startLoc.getText());
                startTimeRef.child("endLoc").setValue(""+ET_endLoc.getText());

                startTimeRef.child("speed").setValue(""+ET_speed.getText());
                startTimeRef.child("drain").setValue(""+ET_drain.getText());

                startTimeRef.child("driver").setValue(username);

                if (AutoComplete.contains(ET_endLoc.getText().toString())) {
                    Log.d(TAG,"AutoComplete has "+ET_endLoc.getText());
                } else {
                    Log.d(TAG,"AutoComplete hasn't "+ET_endLoc.getText());

                    AutoComplete.add(ET_endLoc.getText().toString());
                    //autoCompleteAdapter.notify();

                    mDatabase_selectedCar.child("locations").setValue(AutoComplete);
                }

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
        ItemList = new list_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems(),true);
        LV.setAdapter(listAdapter);

        /*mDatabase.child("old").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ItemList.clear();

                for (DataSnapshot key : dataSnapshot.getChildren()) {
                    if (!Objects.requireNonNull(key.getKey()).contains("!")){
                        //Log.d(TAG,""+key);
                        list_Item item = new list_Item();

                        Time tS = TimeParser(key.getKey(),DBDateFormat);
                        item.settStart(tS);

                        if (DbVal(key,"Tanken") == null) {
                            item.setStartLoc(DbString(key,"StartOrt"));
                            item.setEndLoc(DbString(key,"ZielOrt"));
                            Time tE = TimeParser(""+DbString(key,"EndZeit"),DBDateFormat);
                            item.settEnd(tE);
                        }

                        item.setStart(DbInt(key,"Start"));
                        item.setEnd(DbInt(key,"Ziel"));

                        item.setSpeed(DbString(key,"Geschwindigkeit"));
                        item.setDrain(DbString(key,"Verbrauch"));

                        item.setDriverName(DbString(key,"Fahrer"));

                        boolean refuel = false;
                        if (DbVal(key,"Tanken")!=null)
                            refuel = (boolean) DbVal(key,"Tanken");

                        item.setRefuel(refuel);

                        item.setPrice(DbString(key,"Preis"));

                        ItemList.addItem(item);

                    }

                    listAdapter.notifyDataSetInvalidated();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        mDatabase_selectedCar.child("data/Y20").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ItemList.clear();

                //for (DataSnapshot key_Y : dataSnapshot.getChildren()) {
                    for (DataSnapshot key_M : dataSnapshot.getChildren()) {
                        for (DataSnapshot key_D : key_M.getChildren()) {
                            for (DataSnapshot key_t : key_D.getChildren()) {

                                //Log.d(TAG, "onDataChange: "+key_t);

                                //Log.d(TAG,""+key);
                                list_Item item = new list_Item();

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

                                ItemList.addItem(item);

                                listAdapter.notifyDataSetInvalidated();
                            }
                        }
                    }
                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        addable();

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

                mDatabase.child(t.format(DBDateFormat)).child("Tanken").setValue(true);


                //mDatabase.child(t.format(dateFormat)).child("EndZeit").setValue(""+timeEnd.format(dateFormat));

                mDatabase.child(t.format(DBDateFormat)).child("Start").setValue(""+startKm.getText());
                mDatabase.child(t.format(DBDateFormat)).child("Ziel").setValue(""+endKm.getText());

                //mDatabase.child(t.format(dateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                //mDatabase.child(t.format(dateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(t.format(DBDateFormat)).child("Geschwindigkeit").setValue(""+speed.getText());
                mDatabase.child(t.format(DBDateFormat)).child("Verbrauch").setValue(""+drain.getText());

                mDatabase.child(t.format(DBDateFormat)).child("Fahrer").setValue(username);
                mDatabase.child(t.format(DBDateFormat)).child("Preis").setValue(""+price.getText());


                SPEdit.putString("lastRefuel",endKm.getText().toString());
                SPEdit.apply();
                mDatabase.child("!SP_Sync").child("lastRefuel").setValue(endKm.getText().toString());

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
                        //fahrt.setZielKM(msg.arg2);
                        //ref.child("ZielKM").setValue(msg.arg2);
                        break;
                    }
                    case 3:{
                        ET_endKm.setText(String.valueOf(msg.arg2));
                        //fahrt.setRückAbholKM(msg.arg2);
                        //ref.child("RückAbholKM").setValue(msg.arg2);
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