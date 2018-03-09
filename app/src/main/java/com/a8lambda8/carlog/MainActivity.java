package com.a8lambda8.carlog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    Button B_start, B_stop, B_last, B_add, B_cls;
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

    private DatabaseReference mDatabase;

    final String dateFormat = "%y-%m-%d_%H-%M-%S";

    String username = "";

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
            "Grünau",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

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

        return super.onOptionsItemSelected(item);
    }

    private void fab(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        ET_startLoc = (EditText) findViewById(R.id.et_startLoc);
        ET_endLoc = (AutoCompleteTextView) findViewById(R.id.et_endLoc);
        ET_startKm = (EditText) findViewById(R.id.et_startKm);
        ET_endKm = (EditText) findViewById(R.id.et_endKm);
        ET_drain = (EditText) findViewById(R.id.et_drain);
        ET_speed = (EditText) findViewById(R.id.et_speed);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
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







        /*ET_startKm.setEnabled(false);
        ET_endKm.setEnabled(false);*/

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
        TV_start = (TextView) findViewById(R.id.tv_start);
        TV_end = (TextView) findViewById(R.id.tv_end);
        TV_dur = (TextView) findViewById(R.id.tv_dur);


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
        B_start = (Button) findViewById(R.id.b_start);
        B_stop = (Button) findViewById(R.id.b_end);
        B_add = (Button) findViewById(R.id.b_add);
        B_cls = (Button) findViewById(R.id.b_cls);

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
                SPedit.putString("lastLoc",ET_endLoc.getText().toString());
                SPedit.apply();

                mDatabase.child(timeStart.format(dateFormat)).child("EndZeit").setValue(""+timeEnd.format(dateFormat));

                mDatabase.child(timeStart.format(dateFormat)).child("Start").setValue(""+ET_startKm.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("Ziel").setValue(""+ET_endKm.getText());

                mDatabase.child(timeStart.format(dateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(timeStart.format(dateFormat)).child("Geschwindigkeit").setValue(""+ET_speed.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("Verbrauch").setValue(""+ET_drain.getText());

                mDatabase.child(timeStart.format(dateFormat)).child("Fahrer").setValue(SP.getString("Fahrer","Kein Fahrer"));


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
        LV = (ListView) findViewById(R.id.fahrten);
        ItemList = new list_Item_list();
        listAdapter = new list_adapter(getApplicationContext(),R.id.fahrten, ItemList.getAllItems());
        LV.setAdapter(listAdapter);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ItemList.clear();

                final Map<String, Object> MAP = (HashMap<String,Object>) dataSnapshot.getValue();
                if(MAP==null)return;

                SortedSet<String> keys = new TreeSet<String>(MAP.keySet());
                for (String key : keys) {

                    list_Item item = new list_Item();

                    Time tS =  new Time(Time.getCurrentTimezone());
                    Time tE =  new Time(Time.getCurrentTimezone());

                    tS.set(Integer.valueOf(key.substring(15,17)),//sec
                            Integer.valueOf(key.substring(12,14)),//min
                            Integer.valueOf(key.substring(9,11)),//hr
                            Integer.valueOf(key.substring(6,8)),//day
                            Integer.valueOf(key.substring(3,5))-1,//month
                            2000+Integer.valueOf(key.substring(0,2)));//year

                    Map map = (Map) MAP.get(key);

                    if(map.get("EndZeit")!=null) {
                        tE.set(Integer.valueOf(map.get("EndZeit").toString().substring(15, 17)),//sec
                                Integer.valueOf(map.get("EndZeit").toString().substring(12, 14)),//min
                                Integer.valueOf(map.get("EndZeit").toString().substring(9, 11)),//hr
                                Integer.valueOf(map.get("EndZeit").toString().substring(6, 8)),//day
                                Integer.valueOf(map.get("EndZeit").toString().substring(3, 5)) - 1,//month
                                2000 + Integer.valueOf(map.get("EndZeit").toString().substring(0, 2)));//year
                    }

                    item.settStart(tS);
                    item.settEnd(tE);

                    //Log.i("xxx",""+map);

                    if(map.get("Tanken")==null) {
                        if (map.get("StartOrt") != null)
                            item.setStartLoc(map.get("StartOrt").toString());

                        if (map.get("ZielOrt") != null)
                            item.setEndLoc(map.get("ZielOrt").toString());
                    }

                    if(map.get("Start")!=null)
                        item.setStart(Integer.valueOf(map.get("Start").toString()));
                    if(map.get("Ziel")!=null)
                        item.setEnd(Integer.valueOf(map.get("Ziel").toString()));

                    if(map.get("Geschwindigkeit")!=null)
                        item.setSpeed(map.get("Geschwindigkeit").toString());
                    if(map.get("Verbrauch")!=null)
                        item.setDrain(map.get("Verbrauch").toString());


                    if(map.get("Fahrer")!=null)
                        item.setDriverName(map.get("Fahrer").toString());

                    if(map.get("Tanken")!=null)
                        item.setRefuel(Boolean.valueOf(map.get("Tanken").toString()));

                    if(map.get("Preis")!=null)
                        item.setPrice(map.get("Preis").toString());

                    ItemList.addItem(item);

                }
                //Collections.reverse((List<?>) ItemList);

                listAdapter.notifyDataSetInvalidated();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addable();

    }

    public Time initTime(){
        Time t = new Time(Time.getCurrentTimezone());
        return t;
    }

    private void startDurUpdater(){

        started = true;
        final Handler updater = new Handler(){
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

                mDatabase.child(t.format(dateFormat)).child("Start").setValue(""+startKm.getText());
                mDatabase.child(t.format(dateFormat)).child("Ziel").setValue(""+endKm.getText());

                //mDatabase.child(t.format(dateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                //mDatabase.child(t.format(dateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(t.format(dateFormat)).child("Geschwindigkeit").setValue(""+speed.getText());
                mDatabase.child(t.format(dateFormat)).child("Verbrauch").setValue(""+drain.getText());

                mDatabase.child(t.format(dateFormat)).child("Fahrer").setValue(SP.getString("Fahrer","Kein Fahrer"));
                mDatabase.child(t.format(dateFormat)).child("Preis").setValue(""+price.getText());
                mDatabase.child(t.format(dateFormat)).child("Tanken").setValue(true);


                SPedit.putString("lastRefuel",endKm.getText().toString());
                SPedit.apply();

            }
        });

        alert.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

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

}