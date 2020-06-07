package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.a8lambda8.carlog.myUtils.DBDateFormat;
import static com.a8lambda8.carlog.myUtils.DateFormat_dmhms;
import static com.a8lambda8.carlog.myUtils.DateFormat_dmyhm;
import static com.a8lambda8.carlog.myUtils.RC_SIGN_IN;
import static com.a8lambda8.carlog.myUtils.SP;
import static com.a8lambda8.carlog.myUtils.SPEdit;
import static com.a8lambda8.carlog.myUtils.TAG;
import static com.a8lambda8.carlog.myUtils.TestDevice;
import static com.a8lambda8.carlog.myUtils.TimeFormat_hm;
import static com.a8lambda8.carlog.myUtils.TimeFormat_hms;
import static com.a8lambda8.carlog.myUtils.currentCarDataRef;
import static com.a8lambda8.carlog.myUtils.currentCarRef;
import static com.a8lambda8.carlog.myUtils.currentUserRef;
import static com.a8lambda8.carlog.myUtils.db;
import static com.a8lambda8.carlog.myUtils.mAuth;
import static com.a8lambda8.carlog.myUtils.postItem;
import static com.a8lambda8.carlog.myUtils.selectedCarId;


public class MainActivity extends AppCompatActivity {

    Button B_start, B_stop, B_add, B_cls;
    EditText ET_startLoc, ET_startKm, ET_endKm, ET_drain, ET_speed;
    AutoCompleteTextView ET_endLoc;
    TextView TV_start, TV_end, TV_dur, TV_speed, TV_drain;
    ListView LV;
    trip_Item_list ItemList;
    list_adapter listAdapter;
    Spinner SP_CarSelect;

    Boolean started = false;
    Boolean speedEnabled = true;
    Boolean drainEnabled = true;

    GregorianCalendar timeStart, timeEnd, duration, currTime;

    List<CarSpinnerItem> carSpinnerItemList;
    CarSpinner_adapter carSpinner_adapter;

    FirebaseUser currentUser;
    private ListenerRegistration registration_data, registration_2;


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

        DBDateFormat = new SimpleDateFormat(getString(R.string.db_date_format), Locale.GERMAN);
        DateFormat_dmyhm = new SimpleDateFormat(getString(R.string.date_format_dmyhm),Locale.GERMAN);
        DateFormat_dmhms = new SimpleDateFormat(getString(R.string.date_format_dmhms),Locale.GERMAN);
        TimeFormat_hms = new SimpleDateFormat(getString(R.string.time_format_hms),Locale.GERMAN);
        TimeFormat_hm = new SimpleDateFormat(getString(R.string.time_format_hm),Locale.GERMAN);

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
        currentUserRef = db.collection("user").document(Objects.requireNonNull(mAuth.getUid()));
        selectedCarId = SP.getString("selectedCarId","x");
        updateCarRefsAndListener();

        if(currentUser!=null) {
            //Log.d(TAG,"Display Name: "+currentUser.getDisplayName());
            //Log.d(TAG, "EMAIL: " + currentUser.getEmail());
            if (currentUser.getEmail() != null && !Objects.equals(currentUser.getEmail(), ""))
                TestDevice = !("j.wasle111@gmail.com;leow707@gmail.com;??".contains(Objects.requireNonNull(currentUser.getEmail())));
            username = Objects.requireNonNull(currentUser.getDisplayName()).split(" ")[0];
        }

        fab();

        timeStart = (GregorianCalendar) GregorianCalendar.getInstance();
        timeEnd = (GregorianCalendar) GregorianCalendar.getInstance();
        duration = (GregorianCalendar) GregorianCalendar.getInstance();
        currTime = (GregorianCalendar) GregorianCalendar.getInstance();

        autoCompleteAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, AutoComplete);


        final Set<String> def = new ArraySet<>();

        AutoComplete.addAll(Objects.requireNonNull(Objects.requireNonNull(SP.getStringSet("!locations", def))));
        autoCompleteAdapter.setNotifyOnChange(true);

        init();

        currentUserRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed. (user)", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    @SuppressWarnings("unchecked")
                    List<String> cars = (List<String>) snapshot.get("cars");

                    if (Objects.requireNonNull(cars).size()>0) {
                        int i = 0;
                        for (String x:cars){
                            //final boolean setSelectedCar = (i++ == cars.size() - 1);

                            final int finalI = i;
                            db.collection("cars").document(x).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (Objects.requireNonNull(document).exists()) {

                                                    Log.d(TAG, "Add car Name: " + document.get("name"));
                                                    carSpinnerItemList.add(new CarSpinnerItem(document.getId(), (String) document.get("name")));

                                                    carSpinner_adapter.notifyDataSetChanged();

                                                    if(/*setSelectedCar&&*/ SP.getString("selectedCarId", "x").equals(document.getId()))
                                                        SP_CarSelect.setSelection(finalI);

                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                        }
                    }else{

                        carSpinnerItemList.add(new CarSpinnerItem("x", "Create new Car"));
                        carSpinner_adapter.notifyDataSetChanged();
                        SP_CarSelect.setSelection(0);
                    }
                }
            }
        });

        started = SP.getBoolean("started",false);

        timeStart.setTimeInMillis(SP.getLong("timeStart",2000));

        if(timeStart.getTimeInMillis()>2000) {
            started = true;
            TV_start.setText(String.format("Start Zeit: %s", DateFormat_dmhms.format(timeStart.getTime())));

        }

        timeEnd.setTimeInMillis(SP.getLong("timeEnd",2000));
        if(timeEnd.getTimeInMillis()>5000) {
            started = false;
            TV_end.setText(String.format("End  Zeit:%s", DateFormat_dmhms.format(timeEnd.getTime())));
            duration.setTimeInMillis(timeEnd.getTimeInMillis() - timeStart.getTimeInMillis()-3600000);
            TV_dur.setText(String.format("Dauer:      %s", TimeFormat_hms.format(duration.getTime())));
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

            Log.d(TAG, ""+carSpinner_adapter);

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
        TV_speed = findViewById(R.id.tv_speed);
        TV_drain = findViewById(R.id.tv_drain);





        TV_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timeStart.getTimeInMillis()>5000){
                    new TIME_picker(MainActivity.this,timeStart.getTime(), Handler,"Start Zeit eingeben:",0);
                }
            }
        });

        TV_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!started&&timeEnd.getTimeInMillis()>5000){
                    new TIME_picker(MainActivity.this,timeEnd.getTime(), Handler,"End Zeit eingeben:",1);
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

                timeStart.setTime(new Date());
                SPEdit.putLong("timeStart",timeStart.getTimeInMillis());
                SPEdit.apply();
                TV_start.setText(String.format("Start Zeit: %s", DateFormat_dmhms.format(timeStart.getTime())));

                ET_startKm.setText(SP.getString("lastKm",""));
                ET_startLoc.setText(SP.getString("lastLoc",""));

                B_start.setEnabled(false);
                B_stop.setEnabled(true);

                startDurUpdater();
            }
        });
        Log.d(TAG,"started: " + started);
        if (started||(timeStart.getTimeInMillis()>2000)&&!(timeStart.getTimeInMillis()>2000)){
            B_start.setEnabled(false);
            B_stop.setEnabled(true);
        }

        B_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeEnd.setTime(new Date());
                SPEdit.putLong("timeEnd",timeEnd.getTimeInMillis());
                SPEdit.apply();
                TV_end.setText(String.format("End  Zeit: %s", DateFormat_dmhms.format(timeEnd.getTime())));
                started = false;
                updateDur();
                addable();
            }
        });

        B_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                trip_Item item = new trip_Item();

                item.settStart(timeStart.getTime());
                item.settEnd(timeEnd.getTime());

                item.setStartLoc(ET_startLoc.getText().toString());
                item.setEndLoc(ET_endLoc.getText().toString());

                item.setStart(Integer.parseInt(ET_startKm.getText().toString()));
                item.setEnd(Integer.parseInt(ET_endKm.getText().toString()));

                if (speedEnabled)
                    item.setSpeed(ET_speed.getText().toString());
                if (drainEnabled)
                    item.setDrain(ET_drain.getText().toString());

                item.setDriverName(username);
                item.setDriverId(mAuth.getUid());

                postItem(item);

                SPEdit.putString("lastKm",ET_endKm.getText().toString());
                SPEdit.putString("lastLoc",ET_endLoc.getText().toString());
                SPEdit.apply();

                currentCarRef.update(
                        "SP_sync.lastLoc",ET_endLoc.getText().toString(),
                        "SP_sync.lastKm",ET_endKm.getText().toString());

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
        LV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                trip_Item x = listAdapter.getItem(position);
                Log.d(TAG, "debug OnItem Click: "+ Objects.requireNonNull(x).getID()+" "+x.getMap());
                Log.d(TAG, String.format("LINK to firestore: https://console.firebase.google.com/u/0/project/carlog-14eed/database/firestore/data~2Fcars~2F%s~2Fdata~2F%s",
                        selectedCarId,x.getID()));
            }
        });

        addable();

        SP_CarSelect = findViewById(R.id.SP_carSelect);
        SP_CarSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(carSpinnerItemList.get(position).id.equals("x")){
                    Log.d(TAG, "Create New Car");
                    Intent newCar_i = new Intent(getBaseContext(), NewCarActivity.class);
                    startActivity(newCar_i);
                }else {
                    selectedCarId = carSpinnerItemList.get(position).id;
                    updateCarRefsAndListener();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        carSpinnerItemList = new ArrayList<>();
        carSpinner_adapter = new CarSpinner_adapter(this,android.R.layout.simple_spinner_item,carSpinnerItemList);
        carSpinner_adapter.setNotifyOnChange(true);

        SP_CarSelect.setAdapter(carSpinner_adapter);

    }

    private void startDurUpdater(){

        started = true;

        B_stop.setEnabled(true);
        B_start.setEnabled(false);

        @SuppressLint("HandlerLeak") final Handler updater = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                duration.setTimeInMillis(currTime.getTimeInMillis() - timeStart.getTimeInMillis()-3600000);

                //Log.d(TAG, String.format("dur updater: %d - %d = %d", currTime.getTimeInMillis(), timeStart.getTimeInMillis(),currTime.getTimeInMillis() - timeStart.getTimeInMillis()));

                TV_dur.setText(String.format("Dauer:       %s", TimeFormat_hms.format(duration.getTime())));
            }
        };

        new Thread( new Runnable() {
            @Override
            public void run() {
                int last = 0;
                while (started) {

                    currTime.setTime(new Date());
                    if (currTime.get(Calendar.SECOND)!= last) {
                        updater.sendMessage(new Message());
                        last = currTime.get(Calendar.SECOND);
                    }
                }
            }
        }).start();
    }

    private void updateDur(){

        duration.setTimeInMillis(timeEnd.getTimeInMillis() - timeStart.getTimeInMillis()-3600000);

        TV_dur.setText(String.format("Dauer:       %s", TimeFormat_hms.format(duration.getTime())));
    }

    private void clear(){

        timeStart.setTimeInMillis(2000);
        timeEnd.setTimeInMillis(2000);
        duration.setTimeInMillis(2000);
        currTime.setTimeInMillis(2000);

        SPEdit.putLong("timeStart",timeStart.getTimeInMillis());
        SPEdit.putLong("timeEnd",timeStart.getTimeInMillis());

        ET_startLoc.setText("");
        ET_endLoc.setText("");
        ET_startKm.setText("");
        ET_endKm.setText("");
        ET_drain.setText("");
        ET_speed.setText("");

        TV_start.setText("Start Zeit: 00.00.  00:00:00");//timeStart.format("Start Zeit: 00.00.  00:00:00"));
        TV_end.setText("End  Zeit: 00.00.  00:00:00");//timeEnd.format("End  Zeit: 00.00.  00:00:00"));
        TV_dur.setText("Dauer:      00:00:00");//duration.format("Dauer:      00:00:00"));

        B_start.setEnabled(true);

        addable();

    }

    private void addable(){

        if(started||
                Objects.equals(ET_startLoc.getText().toString(), "")||
                Objects.equals(ET_endLoc.getText().toString(), "")||
                Objects.equals(ET_startKm.getText().toString(), "")||
                Objects.equals(ET_endKm.getText().toString(), "")||
                (drainEnabled && Objects.equals(ET_drain.getText().toString(), ""))||
                (speedEnabled && Objects.equals(ET_speed.getText().toString(), ""))|
                timeEnd.getTimeInMillis()<=20000||
                timeStart.getTimeInMillis()<=20000
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
        final TextView tv_drain = alertView.findViewById(R.id.tvDrain);
        final TextView tv_speed = alertView.findViewById(R.id.tvSpeed);

        final EditText price = alertView.findViewById(R.id.etPrice);

        if(!drainEnabled) {
            drain.setVisibility(View.GONE);
            tv_drain.setVisibility(View.GONE);
        }

        if(!speedEnabled) {
            speed.setVisibility(View.GONE);
            tv_speed.setVisibility(View.GONE);
        }

        startKm.setText(SP.getString("lastRefuel","0"));

        alert.setView(alertView);
        alert.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                trip_Item item = new trip_Item();

                item.settStart(new Date());

                item.setStart(Integer.parseInt(startKm.getText().toString()));
                item.setEnd(Integer.parseInt(endKm.getText().toString()));

                if(drainEnabled)
                    item.setDrain(drain.getText().toString());
                if(speedEnabled)
                    item.setSpeed(speed.getText().toString());

                item.setDriverName(username);
                item.setDriverId(mAuth.getUid());

                item.setRefuel(true);

                item.setPrice(price.getText().toString());

                postItem(item);
                SPEdit.putString("lastRefuel",startKm.getText().toString());
                SPEdit.apply();
                currentCarRef.update(
                        "SP_sync.lastRefuel",startKm.getText().toString());

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
                        timeStart.setTime(((GregorianCalendar) msg.obj).getTime());
                        TV_end.setText(String.format("End Zeit: %s", DateFormat_dmhms.format(timeStart.getTime())));

                        SPEdit.putLong("timeStart",timeStart.getTimeInMillis());
                        SPEdit.apply();

                        updateDur();
                        break;
                    }
                    case 1:{
                        timeEnd.setTime(((GregorianCalendar) msg.obj).getTime());
                        TV_end.setText(String.format("End Zeit: %s", DateFormat_dmhms.format(timeEnd.getTime())));

                        SPEdit.putLong("timeEnd",timeEnd.getTimeInMillis());
                        SPEdit.apply();

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
                //Log.d(TAG,""+user);

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

    @Override
    protected void onDestroy() {

        if (registration_data != null)
            registration_data.remove();
        if (registration_2 != null)
            registration_2.remove();
        super.onDestroy();
    }

    void updateCarRefsAndListener(){

        if (registration_data != null)
            registration_data.remove();
        if (registration_2 != null)
            registration_2.remove();

        currentCarRef = db.collection("cars").document(selectedCarId);
        currentCarDataRef = currentCarRef.collection("data");
        SPEdit.putString("selectedCarId",selectedCarId);
        SPEdit.apply();

        if(!selectedCarId.equals("x")) {

            Date d = new Date();
            d.setTime(d.getTime()-(long) 1000 * 60 * 60 * 24 * 30 * 6);

            registration_data = currentCarDataRef
                    .whereGreaterThan("startTime", DBDateFormat.format(d))
                    //.orderBy("startTime")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                            ItemList.clear();

                            for (DocumentSnapshot doc : Objects.requireNonNull(queryDocumentSnapshots).getDocuments()) {
                                trip_Item item = new trip_Item();

                                item.setID(doc.getId());

                                //Time tS = TimeParser((String) doc.get("startTime"), DBDateFormat);
                                try {
                                    item.settStart(DBDateFormat.parse((String) doc.get("startTime")));
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }

                                item.setRefuel((Boolean) doc.get("refuel"));

                                if (!item.getRefuel()) {
                                    item.setStartLoc((String) doc.get("startLoc"));
                                    item.setEndLoc((String) doc.get("endLoc"));
                                    //Time tE = TimeParser((String) doc.get("endTime"), DBDateFormat);
                                    try {
                                        item.settEnd(DBDateFormat.parse((String) doc.get("endTime")));
                                    } catch (ParseException ex) {
                                        ex.printStackTrace();
                                    }
                                }

                                if(doc.get("startKm")!=null)
                                    item.setStart(Math.toIntExact((long) doc.get("startKm")));
                                else
                                    item.setStart(0);
                                if(doc.get("endKm")!=null)
                                    item.setEnd(Math.toIntExact((long) doc.get("endKm")));
                                else{
                                    item.setEnd(0);
                                }

                                item.setSpeed((String) doc.get("speed"));
                                item.setDrain((String) doc.get("drain"));

                                item.setDriverName((String) doc.get("driver"));

                                item.setPrice((String) doc.get("price"));

                                ItemList.addItem(item);

                                listAdapter.notifyDataSetInvalidated();
                            }

                        }
                    });

            registration_2 = currentCarRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                            AutoComplete.addAll(loc);
                        }

                        updateAutoCompleteAdapter();

                        if(snapshot.get("SP_sync")!=null) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> map = (Map<String, Object>) snapshot.get("SP_sync");

                            SPEdit.putString("lastRefuel", (String) Objects.requireNonNull(map).get("lastRefuel"));
                            SPEdit.putString("lastKm", (String) map.get("lastKm"));
                            SPEdit.putString("lastLoc", (String) map.get("lastLoc"));

                        }
                        if(snapshot.get("owner")!=null) {
                            SPEdit.putString("owner", (String) snapshot.get("owner"));
                        }
                        if(snapshot.get("drivers")!=null) {
                            Set<String> drivers = new ArraySet<>();
                            drivers.addAll((Collection<? extends String>) snapshot.get("drivers"));

                            SPEdit.putStringSet("drivers",drivers);
                        }
                        if(snapshot.get("name")!=null) {
                            SPEdit.putString("name", (String) snapshot.get("name"));
                        }
                        if(snapshot.get("type")!=null) {
                            SPEdit.putString("type", (String) snapshot.get("type"));
                        }
                        if(snapshot.get("speedEnabled")!=null) {
                            SPEdit.putBoolean("speedEnabled", (Boolean) snapshot.get("speedEnabled"));
                        }
                        if(snapshot.get("drainEnabled")!=null) {
                            SPEdit.putBoolean("drainEnabled", (Boolean) snapshot.get("drainEnabled"));
                        }

                        SPEdit.apply();

                        drainEnabled = SP.getBoolean("drainEnabled",true);
                        speedEnabled = SP.getBoolean("speedEnabled",true);

                        if (speedEnabled) {
                            ET_speed.setVisibility(View.VISIBLE);
                            TV_speed.setVisibility(View.VISIBLE);
                        }else {
                            ET_speed.setVisibility(View.GONE);
                            TV_speed.setVisibility(View.GONE);
                        }
                        if (drainEnabled) {
                            ET_drain.setVisibility(View.VISIBLE);
                            TV_drain.setVisibility(View.VISIBLE);
                        }else {
                            ET_drain.setVisibility(View.GONE);
                            TV_drain.setVisibility(View.GONE);
                        }

                    } else {
                        Log.d(TAG, source + " data: null");
                    }
                }
            });
        }

    }

}