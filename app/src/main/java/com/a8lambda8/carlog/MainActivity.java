package com.a8lambda8.carlog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    Button B_start, B_stop, B_last, B_add, B_cls;
    EditText ET_startLoc, ET_endLoc, ET_startKm, ET_endKm, ET_drain, ET_speed;
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
        ET_endLoc = (EditText) findViewById(R.id.et_endLoc);
        ET_startKm = (EditText) findViewById(R.id.et_startKm);
        ET_endKm = (EditText) findViewById(R.id.et_endKm);
        ET_drain = (EditText) findViewById(R.id.et_drain);
        ET_speed = (EditText) findViewById(R.id.et_speed);

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

        ////TextViews
        TV_start = (TextView) findViewById(R.id.tv_start);
        TV_end = (TextView) findViewById(R.id.tv_end);
        TV_dur = (TextView) findViewById(R.id.tv_dur);

        ////Buttons
        B_start = (Button) findViewById(R.id.b_start);
        B_stop = (Button) findViewById(R.id.b_end);
        B_last = (Button) findViewById(R.id.b_lastKm);
        B_add = (Button) findViewById(R.id.b_add);
        B_cls = (Button) findViewById(R.id.b_cls);

        B_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeStart.setToNow();
                SPedit.putLong("timeStart",timeStart.toMillis(true));
                SPedit.apply();
                TV_start.setText(timeStart.format("Start Zeit: %d.%m.  %H:%M:%S"));
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
        B_last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ET_startKm.setText(SP.getString("lastKm",""));
            }
        });
        B_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SPedit.putString("lastKm",ET_endKm.getText().toString());
                SPedit.apply();

                mDatabase.child(timeStart.format(dateFormat)).child("EndZeit").setValue(""+timeEnd.format(dateFormat));

                mDatabase.child(timeStart.format(dateFormat)).child("Start").setValue(""+ET_startKm.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("Ziel").setValue(""+ET_endKm.getText());

                mDatabase.child(timeStart.format(dateFormat)).child("StartOrt").setValue(""+ET_startLoc.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("ZielOrt").setValue(""+ET_endLoc.getText());

                mDatabase.child(timeStart.format(dateFormat)).child("Geschwindigkeit").setValue(""+ET_speed.getText());
                mDatabase.child(timeStart.format(dateFormat)).child("Verbrauch").setValue(""+ET_drain.getText());

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

                clear();

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

                    tE.set(Integer.valueOf(map.get("EndZeit").toString().substring(15,17)),//sec
                            Integer.valueOf(map.get("EndZeit").toString().substring(12,14)),//min
                            Integer.valueOf(map.get("EndZeit").toString().substring(9,11)),//hr
                            Integer.valueOf(map.get("EndZeit").toString().substring(6,8)),//day
                            Integer.valueOf(map.get("EndZeit").toString().substring(3,5))-1,//month
                            2000+Integer.valueOf(map.get("EndZeit").toString().substring(0,2)));//year


                    item.settStart(tS);
                    item.settEnd(tE);

                    Log.i("xxx",""+map);
                    if(map.get("StartOrt")!=null)
                        item.setStartLoc(map.get("StartOrt").toString());

                    if(map.get("ZielOrt")!=null)
                        item.setEndLoc(map.get("ZielOrt").toString());

                    if(map.get("Start")!=null)
                        item.setStart(Integer.valueOf(map.get("Start").toString()));
                    if(map.get("Ziel")!=null)
                        item.setEnd(Integer.valueOf(map.get("Ziel").toString()));

                    if(map.get("Geschwindigkeit")!=null)
                    item.setSpeed(map.get("Geschwindigkeit").toString());
                    if(map.get("Verbrauch")!=null)
                        item.setDrain(map.get("Verbrauch").toString());

                    ItemList.addItem(item);


                }

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

}