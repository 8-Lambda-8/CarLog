package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
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

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    SharedPreferences.Editor SPedit;

    static DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    private static final int RC_SIGN_IN = 123;

    private static final String TAG = "xx";

    final String DBdateFormat = "%y-%m-%d_%H-%M-%S";

    String username = "";

    static boolean TestDevice;

    public static final String[] AutoComplete = new String[]{
            "Bach", "Berwang", "Schattwald", "Stanzach",
            "Biberwier", "Bichlbach", "Breitenwang",
            "Ehenbichl", "Ehrwald", "Elbigenalp", "Elmen",
            "Forchach", "Grän", "Gramais", "Häselgehr",
            "Heiterwang", "Hinterhornbach", "Höfen", "Holzgau",
            "Jungholz", "Kaisers", "Lechaschau", "Lermoos",
            "Musau", "Namlos", "Nesselwängle", "Pfafflar",
            "Pflach", "Pinswang", "Reutte", "Steeg",
            "Tannheim", "Vils", "Vorderhornbach", "Wängle",
            "Weißenbach am Lech", "Zöblen", "Innsbruck", "Imst",
            "Grünau", "Stockach",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        Log.d(TAG,"user: "+currentUser);
        if(currentUser==null) {

            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    //new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    //new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }


        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d(TAG, "EMAIL: "+ currentUser.getEmail());
        TestDevice = !("j.wasle111@gmail.com;leow707@gmail.com;??".contains(Objects.requireNonNull(currentUser.getEmail())));
        Log.d(TAG, "is test Device:"+ TestDevice);
        if(TestDevice)mDatabase = mDatabase.child("!Test");

        fab();

        timeStart = initTime();
        timeEnd = initTime();
        duration = initTime();
        currTime = initTime();

        SP = PreferenceManager.getDefaultSharedPreferences(this);
        SPedit = SP.edit();
        SPedit.apply();

        username = SP.getString("Fahrer","Kein Fahrer");

        if (Objects.equals(username, "")){
            changeUsername(false);
            username = SP.getString("Fahrer","Kein Fahrer");
        }


        mDatabase.child("!SP_Sync").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SPedit.putString("lastRefuel", String.valueOf(dataSnapshot.child("lastRefuel").getValue()));
                SPedit.putString("lastKm", String.valueOf(dataSnapshot.child("lastKm").getValue()));
                SPedit.putString("lastLoc", String.valueOf(dataSnapshot.child("lastLoc").getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        init();

        started = SP.getBoolean("started",false);

        timeStart.set(SP.getLong("timeStart",2000));

        if(timeStart.toMillis(false)!=2000) {
            started = true;
            Log.i("xxx",""+"timeStart.toMillis(false)!=2000");
            TV_start.setText(timeStart.format("Start Zeit: %d.%m.  %H:%M:%S"));
        }

        timeEnd.set(SP.getLong("timeEnd",2000));
        if(timeEnd.toMillis(false)!=2000) {
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
        if (id == R.id.action_changeUsername) {
            changeUsername(true);
            return true;
        }
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
        if(id == R.id.action_logOut){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            System.exit(1);
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }

    private void fab(){
        FloatingActionButton fab = findViewById(R.id.fab);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, AutoComplete);
        ET_endLoc.setAdapter(adapter);


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

                SPedit.putString("StartLoc",""+editable);
                SPedit.apply();
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

                SPedit.putString("EndLoc",""+editable);
                SPedit.apply();
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

                SPedit.putString("StartKm",""+editable);
                SPedit.apply();
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

                SPedit.putString("EndKm",""+editable);
                SPedit.apply();
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

                SPedit.putString("drain",""+editable);
                SPedit.apply();
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

                SPedit.putString("speed",""+editable);
                SPedit.apply();
                addable();

            }
        });

        ET_startKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new KMpicker(MainActivity.this,Integer.parseInt(ET_startKm.getText().toString()), Handler, "Start KM eingeben:",2);
            }
        });
        ET_endKm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int in = 0;
                Log.i("xxx","asd "+ET_endKm.getText());
                if(ET_endKm.getText()!=null&& !Objects.equals(ET_endKm.getText().toString(), "")){
                    in = Integer.parseInt(ET_endKm.getText().toString());
                }
                Log.i("xxx","in:"+in);
                if (in<=0){
                    in = Integer.parseInt(ET_startKm.getText().toString());
                }
                new KMpicker(MainActivity.this,in, Handler, "End KM eingeben:",3);

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
                SPedit.putLong("timeStart",timeStart.toMillis(true));
                SPedit.apply();
                TV_start.setText(timeStart.format("Start Zeit: %d.%m.  %H:%M:%S"));

                ET_startKm.setText(SP.getString("lastKm",""));
                ET_startLoc.setText(SP.getString("lastLoc",""));

                B_start.setEnabled(false);

                startDurUpdater();
            }
        });
        B_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeEnd.setToNow();
                SPedit.putLong("timeEnd",timeEnd.toMillis(true));
                SPedit.apply();
                TV_end.setText(timeEnd.format("End  Zeit: %d.%m.  %H:%M:%S"));
                started = false;
                addable();
            }
        });

        B_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SPedit.putString("lastKm",ET_endKm.getText().toString());
                mDatabase.child("!SP_Sync").child("lastKm").setValue(ET_endKm.getText().toString());
                SPedit.putString("lastLoc",ET_endLoc.getText().toString());
                mDatabase.child("!SP_Sync").child("lastLoc").setValue(ET_endLoc.getText().toString());
                SPedit.apply();

                mDatabase.child(timeStart.format(DBdateFormat)).child("EndZeit").setValue(""+timeEnd.format(DBdateFormat));

                mDatabase.child(timeStart.format(DBdateFormat)).child("Start").setValue(""+ET_startKm.getText());
                mDatabase.child(timeStart.format(DBdateFormat)).child("Ziel").setValue(""+ET_endKm.getText());

                mDatabase.child(timeStart.format(DBdateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                mDatabase.child(timeStart.format(DBdateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(timeStart.format(DBdateFormat)).child("Geschwindigkeit").setValue(""+ET_speed.getText());
                mDatabase.child(timeStart.format(DBdateFormat)).child("Verbrauch").setValue(""+ET_drain.getText());

                mDatabase.child(timeStart.format(DBdateFormat)).child("Fahrer").setValue(SP.getString("Fahrer","Kein Fahrer"));

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

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemList.clear();

                for (DataSnapshot key : dataSnapshot.getChildren()) {
                    if (!key.getKey().contains("!")){
                        //Log.d(TAG,""+key);
                        list_Item item = new list_Item();

                        Time tS = TimeParser(key.getKey(),DBdateFormat);
                        item.settStart(tS);

                        if (DbVal(key,"Tanken") == null) {
                            item.setStartLoc(DbString(key,"StartOrt"));
                            item.setEndLoc(DbString(key,"ZielOrt"));

                            Time tE = TimeParser(""+DbString(key,"EndZeit"),DBdateFormat);
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addable();

    }

    public Time initTime(){
        return new Time(Time.getCurrentTimezone());
    }

    private void startDurUpdater(){

        started = true;
        @SuppressLint("HandlerLeak") final Handler updater = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                duration.set(currTime.toMillis(false) - timeStart.toMillis(false)-3600000);

                TV_dur.setText(duration.format("Dauer:       %H:%M:%S"));
            }
        };

        /*static class updater extends Handler{

            @Override
            public void handleMessage(Message msg){

            }

        }*/

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

        SPedit.putLong("timeStart",timeStart.toMillis(true));
        SPedit.putLong("timeEnd",timeStart.toMillis(true));

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
                Objects.equals(ET_speed.getText().toString(), "")
                ){
            B_add.setEnabled(false);
        }else{
            B_add.setEnabled(true);
        }

    }

    private void changeUsername(Boolean abortable) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Name ändern");

        final EditText edittext = new EditText(getApplicationContext());
        alert.setView(edittext);

        edittext.setText(SP.getString("Fahrer",""));

        alert.setPositiveButton("Bestätigen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                SPedit.putString("Fahrer",edittext.getText().toString());
                SPedit.apply();

                if(edittext.getText().toString().length()<3){
                    changeUsername(false);
                }

            }
        });

        alert.setCancelable(false);

        if(abortable) {
            alert.setCancelable(true);
            alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // what ever you want to do with No option.
                }
            });
        }

        alert.show();

    }

    private void refuel(){

        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Tanken");

        View alertView = getLayoutInflater().inflate(R.layout.refuel,null);

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

                //mDatabase.child(t.format(dateFormat)).child("EndZeit").setValue(""+timeEnd.format(dateFormat));

                mDatabase.child(t.format(DBdateFormat)).child("Start").setValue(""+startKm.getText());
                mDatabase.child(t.format(DBdateFormat)).child("Ziel").setValue(""+endKm.getText());

                //mDatabase.child(t.format(dateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                //mDatabase.child(t.format(dateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(t.format(DBdateFormat)).child("Geschwindigkeit").setValue(""+speed.getText());
                mDatabase.child(t.format(DBdateFormat)).child("Verbrauch").setValue(""+drain.getText());

                mDatabase.child(t.format(DBdateFormat)).child("Fahrer").setValue(SP.getString("Fahrer","Kein Fahrer"));
                mDatabase.child(t.format(DBdateFormat)).child("Preis").setValue(""+price.getText());
                mDatabase.child(t.format(DBdateFormat)).child("Tanken").setValue(true);

                SPedit.putString("lastRefuel",endKm.getText().toString());
                SPedit.apply();
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

    Time TimeParser(String time, String format){
        Time t = new Time(Time.getCurrentTimezone());
        if(format.charAt(1)=='y')
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(6,8)),Integer.parseInt(time.substring(3,5))-1,2000+Integer.parseInt(time.substring(0,2)));
        else if(format.charAt(1)=='d')
            t.set(Integer.parseInt(time.substring(15)),Integer.parseInt(time.substring(12,14)),Integer.parseInt(time.substring(9,11)),
                    Integer.parseInt(time.substring(0,2)),Integer.parseInt(time.substring(3,5))-1,2000+Integer.parseInt(time.substring(6,8)));
        return t;
    }

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
                // ...
            } else {
                // Sign in failed, check response for error code
                System.exit(1);
            }
        }
    }


}