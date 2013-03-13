package com.seeedstudio.beacon.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.seeedstudio.beacon.utility.BeaconApp;
import com.seeedstudio.library.Atom;
import com.seeedstudio.library.State;

public class NormalSetupActivity extends Activity {
    // debugging
    private static final boolean D = false;
    private static final String TAG = "NormalSetupActivity";

    public static final String NORMAL_CONFIG_KEY = "com.seeedstudio.beacon.ui.normal.configure.atom";

    // This Activity can be started by more than one action. Each action is
    // represented
    // as a "state" constant
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private ViewSwitcher mViewSwitcher;
    private ScrollView mScrollView;
    private Button cancel, submit, yes, again;
    private Spinner sensorSpinner, sensorFrqSpinner, actuatorSpinner,
            triggerSpinner, compareAction, actionSpinner;
    private EditText deviceName, compareValue;
    private View flash, setupAll, setupSubmit;
    private Uri mUri;
    private Cursor mCursor = null, triggerCursor = null;
    private Atom atom = null;
    private State state;

    private int mColor = Color.WHITE;
    private int mContentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_setup);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (intent.hasExtra(MainActivity.SELECTED_BEACON)) {
            atom = (Atom) getIntent().getSerializableExtra(
                    MainActivity.SELECTED_BEACON);
            Toast.makeText(getApplicationContext(), "get atom from itent",
                    Toast.LENGTH_SHORT).show();
        }

        if (Intent.ACTION_INSERT.equals(action)) {
            if (D)
                Log.d(TAG, "action is " + action.toString());

            mContentState = STATE_INSERT;
            // mUri = getContentResolver().insert(BeaconApp.Beacon.CONTENT_URI,
            // null);
            mUri = intent.getData();
            if (D)
                Log.d(TAG, "getData Uri:" + mUri);

            if (mUri == null) {

                // Writes the log identifier, a message, and the URI that
                // failed.
                Log.e(TAG, "Failed to query the beacon paried database "
                        + getIntent().getData());

                // Closes the activity.
                finish();
                return;
            }

            // Since the new entry was created, this sets the result to be
            // returned
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

            // get the cursor for manager database
            mCursor = getContentResolver().query(mUri,
                    BeaconApp.Beacon.PROJECTION, null, null, null);
            if (mCursor.moveToFirst()) {
                if (D)
                    Log.d(TAG, "mCursor moveToFirst() ");

                int index = mCursor
                        .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID);
                int id = mCursor.getInt(index);

                atom = new Atom("Device Name", "new beacon", id + 1);

                if (D)
                    Log.d(TAG, "mCursor moveToFirst(), id: " + id
                            + ", and new atom id is: " + id + 1);
            } else {
                if (D)
                    Log.d(TAG, "mCursor is empty ");

                // insert "NULL"
                ContentValues v = new ContentValues();
                v.put(BeaconApp.Beacon.COLUMN_NAME_TITLE, "NULL");
                v.put(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID, "0");
                getContentResolver().insert(mUri, v);
                atom = new Atom("Device Name", "new beacon", 0);
            }

        } else if (Intent.ACTION_EDIT.equals(action)) {

            mContentState = STATE_EDIT;
            mUri = intent.getData();

            if (D)
                Log.d(TAG, "action is " + action.toString() + ", uri: " + mUri);

            // get the cursor for manager database
            mCursor = getContentResolver().query(mUri,
                    BeaconApp.Beacon.PROJECTION, null, null, null);
            reconfigureBeacon();
        } else {

            // Logs an error that the action was not understood, finishes the
            // Activity, and
            // returns RESULT_CANCELED to an originating Activity.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        initUI();

        state = new State(getApplicationContext(), mHandler);

    }

    private void initUI() {
        // flash layout
        flash = findViewById(R.id.setup_flash);
        // flash.setVisibility(View.VISIBLE);

        setupAll = findViewById(R.id.setup_all);
        // view switcher
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.setup_viewswitcher);
        // scrollView
        mScrollView = (ScrollView) findViewById(R.id.setup_scrollview);
        // setup submit
        setupSubmit = findViewById(R.id.setup_submit);

        // button
        cancel = (Button) findViewById(R.id.normal_cancel);
        submit = (Button) findViewById(R.id.normal_submit);
        cancel.setOnClickListener(new ClickEvent());
        submit.setOnClickListener(new ClickEvent());
        //
        yes = (Button) findViewById(R.id.yes);
        again = (Button) findViewById(R.id.again);
        yes.setOnClickListener(new ClickEvent());
        again.setOnClickListener(new ClickEvent());

        // edittext
        deviceName = (EditText) findViewById(R.id.setup_name_text);
        compareValue = (EditText) findViewById(R.id.setup_action_if_text);

        if (mContentState == STATE_EDIT) {
            deviceName.setText(atom.getName());
            compareValue
                    .setText(String.valueOf(atom.getActuatorCompareValue()));
        }

        // spinner
        triggerSpinner = (Spinner) findViewById(R.id.setup_trigger_spinner);
        compareAction = (Spinner) findViewById(R.id.setup_action_if_spinner);
        actionSpinner = (Spinner) findViewById(R.id.setup_action_then_spinner);
        sensorSpinner = (Spinner) findViewById(R.id.setup_sensor_spinner);
        sensorFrqSpinner = (Spinner) findViewById(R.id.setup_sensor_frq_spinner);
        actuatorSpinner = (Spinner) findViewById(R.id.setup_actuator_spinner);

        triggerCursor = getContentResolver().query(
                BeaconApp.Beacon.CONTENT_URI, BeaconApp.Beacon.PROJECTION,
                null, null, null);

        String[] dataColumns = { BeaconApp.Beacon.COLUMN_NAME_TITLE };
        int[] viewIDs = { android.R.id.text1 };
        // trigger get from database.
        if (D)
            Log.d(TAG, "triggerAdapter init");
        SimpleCursorAdapter triggerAdapter = new SimpleCursorAdapter(
                getApplicationContext(), R.layout.list, triggerCursor,
                dataColumns, viewIDs, 0);
        triggerAdapter.setDropDownViewResource(R.layout.spiner_dropdown);
        triggerSpinner.setAdapter(triggerAdapter);
        triggerSpinner.setOnItemSelectedListener(new SpinnerEvent());

        // sensor
        ArrayAdapter<String> sensorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                        .getStringArray(R.array.sensor));
        sensorAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorSpinner.setAdapter(sensorAdapter);
        sensorSpinner.setOnItemSelectedListener(new SpinnerEvent());
        
        // frequency
        ArrayAdapter<String> sensorFrqAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                        .getStringArray(R.array.frq));
        sensorFrqAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorFrqSpinner.setAdapter(sensorFrqAdapter);
        sensorFrqSpinner.setOnItemSelectedListener(new SpinnerEvent());

        // actuator
        ArrayAdapter<String> actuatorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                        .getStringArray(R.array.actuator));
        actuatorAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actuatorSpinner.setAdapter(actuatorAdapter);
        actuatorSpinner.setOnItemSelectedListener(new SpinnerEvent());

        // compare and action
        ArrayAdapter<String> compareAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                        .getStringArray(R.array.compare_type));
        compareAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        compareAction.setAdapter(compareAdapter);
        compareAction.setOnItemSelectedListener(new SpinnerEvent());

        ArrayAdapter<String> actionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, getResources()
                        .getStringArray(R.array.action_type));
        actionAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actionSpinner.setAdapter(actionAdapter);
        actionSpinner.setOnItemSelectedListener(new SpinnerEvent());

    }

    @Override
    protected void onPause() {
        if (D)
            Log.d(TAG, "onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (D)
            Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onResume() {
        if (D)
            Log.d(TAG, "onResume()");
        super.onResume();
    }

    private class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.normal_cancel:
                NormalSetupActivity.this.finish();
                break;
            case R.id.normal_submit:
                if (checkTextValue()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    if (imm.isActive()) {

                    }
                    startCountDown();
                }
                break;
            case R.id.yes:
                if (D)
                    Log.d(TAG, "yes, to insert data");
                insertData(atom);
                break;
            case R.id.again:
                if (D)
                    Log.d(TAG, "no, try again");
                // mScrollView.setVisibility(View.VISIBLE);
                // mViewSwitcher.setVisibility(View.VISIBLE);
                setupSubmit.setVisibility(View.GONE);
                // // setupAll.setVisibility(View.GONE);
                // // flash.setVisibility(View.VISIBLE);
                // startFlash(atom);

                mViewSwitcher.showPrevious();
                startCountDown();
                break;

            default:
                break;
            }
        }

    }

    private class SpinnerEvent implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position,
                long id) {
            switch (parent.getId()) {
            case R.id.setup_sensor_spinner:
                switch (position) {
                case 0:
                    atom.setSensorId(0);
                    break;
                case 1:
                    atom.setSensorId(48);
                    break;
                case 2:
                    atom.setSensorId(82);
                    break;
                case 3:
                    atom.setSensorId(102);
                    break;
                case 4:
                    atom.setSensorId(103);
                    break;
                case 5:
                    atom.setSensorId(1);
                    break;
                case 6:
                    atom.setSensorId(13);
                    break;
                case 7:
                    atom.setSensorId(4);
                    break;
                case 8:
                    atom.setSensorId(45);
                    break;
                case 9:
                    atom.setSensorId(53);
                    break;
                case 10:
                    atom.setSensorId(8);
                    break;
                case 11:
                    atom.setSensorId(44);
                    break;
                case 12:
                    atom.setSensorId(12);
                    break;
                default:
                    break;
                }
                break;
            case R.id.setup_actuator_spinner:
                switch (position) {
                case 0:
                    atom.setActuatorId(0);
                    break;
                case 1:
                    atom.setActuatorId(128);
                    break;
                case 2:
                    atom.setActuatorId(129);
                    break;
                case 3:
                    atom.setActuatorId(137);
                    break;
                case 4:
                    atom.setActuatorId(138);
                    break;
                case 5:
                    atom.setActuatorId(201);
                    break;
                default:
                    break;
                }
                break;
            case R.id.setup_sensor_frq_spinner:
                switch (position) {
                case 0:
                    atom.setSensorFrequency(Atom.FRQ_NULL);
                    break;
                case 1:
                    atom.setSensorFrequency(Atom.FRQ_STD);
                    break;
                case 2:
                    atom.setSensorFrequency(Atom.FRQ_FAST);
                    break;
                case 3:
                    atom.setSensorFrequency(Atom.FRQ_LOW);
                    break;
                case 4:
                    atom.setSensorFrequency(Atom.FRQ_MINI);
                    break;
                default:
                    break;
                }
                break;
            case R.id.setup_trigger_spinner:

                if (mContentState == STATE_INSERT) {
                    if (!mCursor.moveToFirst()) {
                        if (D)
                            Log.d(TAG, "mCursor is " + "empty");

                        atom.setActuatorTrigger(0);
                        return;
                    }

                    mCursor.moveToPosition(position);
                    int index = mCursor
                            .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID);
                    int deviceId = mCursor.getInt(index);
                    atom.setActuatorTrigger(deviceId);
                } else if (mContentState == STATE_EDIT) {

                    triggerCursor.moveToPosition(position);
                    int index = triggerCursor
                            .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID);
                    // int deviceId =
                    // Integer.parseInt(mCursor.getString(index));
                    int deviceId = triggerCursor.getInt(index);
                    atom.setActuatorTrigger(deviceId);
                }

                break;
            case R.id.setup_action_if_spinner:
                switch (position) {
                case 0:
                    atom.setActuatorCompare(Atom.COMPARE_TYEP_GREATER);
                    break;
                case 1:
                    atom.setActuatorCompare(Atom.COMPARE_TYEP_LESS);
                    break;
                case 2:
                    atom.setActuatorCompare(Atom.COMPARE_TYEP_EQUAL);
                    break;
                case 3:
                    atom.setActuatorCompare(Atom.COMPARE_TYEP_NULL);
                    break;
                default:
                    break;
                }
                break;
            case R.id.setup_action_then_spinner:
                switch (position) {
                case 0:
                    atom.setActuatorAction(Atom.ACTIN_TYPE_OFF);
                    break;
                case 1:
                    atom.setActuatorAction(Atom.ACTIN_TYPE_ON);
                    break;
                default:
                    break;
                }
                break;
            default:
                break;
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {

        }

    }

    private void startCountDown() {

        getApplication().setTheme(R.style.Theme_Configure);

        // Start countdown.
        View v = findViewById(R.id.record_overlay);
        v.setVisibility(View.VISIBLE);

        TextView tv = (TextView) findViewById(R.id.record_countdown);
        tv.setVisibility(View.VISIBLE);

        mScrollView.setVisibility(View.GONE);

        // Play countdown
        MediaPlayer mp = MediaPlayer.create(this, R.raw.countdown);
        mp.start();

        countDownStep(3);
    }

    private void countDownStep(final int step) {
        TextView tv = (TextView) findViewById(R.id.record_countdown);

        // If step has reached zero, hide all countdown-related views and start
        // recording.
        if (0 == step) {
            tv.setVisibility(View.GONE);

            View v = findViewById(R.id.record_overlay);
            v.setVisibility(View.GONE);

            mScrollView.setVisibility(View.VISIBLE);
            mViewSwitcher.showNext();
            mScrollView.setBackgroundColor(Color.BLACK);

            // mViewSwitcher.setVisibility(View.VISIBLE);
            // setupAll.setVisibility(View.GONE);
            // flash.bringToFront();
            // start communicating
            startFlash(atom);

            return;
        }

        // Else display the step..
        tv.setText(String.format("%d", step));

        // ... start a 1 second animation that ends up in this function again
        // ...
        Animation animation = AnimationUtils.loadAnimation(this,
                R.anim.countdown);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                // When the player finished fading out, stop capturing clicks.
                countDownStep(step - 1);
            }

            public void onAnimationRepeat(Animation animation) {
                // pass
            }

            public void onAnimationStart(Animation animation) {
                // pass
            }
        });
        tv.startAnimation(animation);

        // ... and vibrate, if we're in vibrate mode!
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (null != am) {
            if (AudioManager.RINGER_MODE_VIBRATE == am.getRingerMode()) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (null != v) {
                    v.vibrate(500);
                }
            }
        }
    }

    private void insertData(Atom atom) {

        if (D)
            Log.d(TAG, "insertData()");

        if (mContentState == STATE_INSERT) {
            insertBeacon(atom);
        } else if (mContentState == STATE_EDIT) {
            updateBeacon(atom);
        }

        this.finish();

    }

    private void setupOk() {
        // Play countdown
        // MediaPlayer mp = MediaPlayer.create(this, R.raw.countdown);
        // mp.start();
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (null != v) {
            v.vibrate(500);
        }

        mScrollView.setVisibility(View.GONE);
        // flash.setVisibility(View.GONE);
        setupSubmit.setVisibility(View.VISIBLE);
    }

    private void reconfigureBeacon() {
        if (mCursor != null) {
            mCursor.moveToFirst();

            int index = mCursor
                    .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_TITLE);
            String name = mCursor.getString(index);

            index = mCursor
                    .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID);
            int id = mCursor.getInt(index);
            
            index = mCursor.getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_SENSOR_FREQ);
            int frq = mCursor.getInt(index);

            index = mCursor
                    .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE_VALUE);
            int compareValue = mCursor.getInt(index);

            atom = new Atom(name, "new Beacon", id);
            atom.setActuatorCompareValue(compareValue);
            atom.setSensorFrequency(frq);

        }

    }

    private void startFlash(Atom atom) {
        state.addAtom(atom);
        state.prepare();
        state.start();
    }

    private boolean checkTextValue() {

        if (deviceName.getText().toString().equals("")) {
            atom.setName("Defalut Beacon");
        } else {
            String name = deviceName.getText().toString();
            atom.setName(name);
        }

        if (compareValue.getText().toString().equals("")) {
            atom.setActuatorCompareValue(0);
        } else {
            String value = compareValue.getText().toString();
            atom.setActuatorCompareValue(Integer.parseInt(value));
        }

        return true;
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            switch (msg.what) {

            case State.START_DOWN_BIT:
                flash.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "start down bit .....");
                }
                break;
            case State.START_UP_BIT:
                flash.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "start up bit .....");
                }
                break;
            case State.HIGH_DOWN_BIT:
                flash.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "high down bit .....");
                }
                break;
            case State.HIGH_UP_BIT:
                flash.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "high up bit .....");
                }
                break;
            case State.LOW_DOWN_BIT:
                flash.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "low down bit .....");
                }
                break;
            case State.LOW_UP_BIT:
                flash.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "low up bit .....");
                }
                break;
            case State.STATE_NONE:
                flash.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "state none bit .....");
                }
                break;
            case State.STATE_DONE:
                flash.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "state done bit .....");
                }
                // insertData(atom);
                setupOk();
                break;
            }
        }

    };

    //

    private final void insertBeacon(Atom atom) {
        // Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        values.put(BeaconApp.Beacon.COLUMN_NAME_MODIFICATION_DATE,
                System.currentTimeMillis());

        // If the action is to insert a new note, this creates an initial title
        // for it.
        if (mContentState == STATE_INSERT) {
            // In the values map, sets the value of the title
            values.put(BeaconApp.Beacon.COLUMN_NAME_TITLE, atom.getName());
            // This puts the desired notes text into the map.
            values.put(BeaconApp.Beacon.COLUMN_NAME_DESC, atom.getDescription());
            // device id from groove sensor, orgnaizate by Seeed Studio
            values.put(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID, atom.getId());

            // sensor
            values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_TITLE,
                    atom.getSensorId());
            values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_FREQ,
                    atom.getSensorFrequency());
            values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_UNIT,
                    atom.getSensorUnit());

            // actuator
            values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TITLE,
                    atom.getActuatorId());
            values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TRIGGER_SOURCE,
                    atom.getActuatorTrigger());
            values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_ACTION,
                    atom.getActuatorAction());
            values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE,
                    atom.getActuatorCompare());
            values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE_VALUE,
                    atom.getActuatorCompareValue());
        }

        if (D)
            Log.d(TAG, values.toString());

        if (D)
            Log.d(TAG, "mUri: " + mUri);

        Uri u = getContentResolver().insert(mUri, values);

        if (D)
            Log.d(TAG, "new Uri: " + u);
    }

    private final void updateBeacon(Atom atom) {
        // Sets up a map to contain values to be updated in the provider.
        ContentValues values = new ContentValues();
        values.put(BeaconApp.Beacon.COLUMN_NAME_MODIFICATION_DATE,
                System.currentTimeMillis());

        // If the action is to insert a new note, this creates an initial title
        // for it.
        // In the values map, sets the value of the title
        values.put(BeaconApp.Beacon.COLUMN_NAME_TITLE, atom.getName());
        // This puts the desired notes text into the map.
        values.put(BeaconApp.Beacon.COLUMN_NAME_DESC, atom.getDescription());
        // device id from groove sensor, orgnaizate by Seeed Studio
        values.put(BeaconApp.Beacon.COLUMN_NAME_DEVICE_ID, atom.getId());

        // sensor
        values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_TITLE,
                atom.getSensorId());
        values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_FREQ,
                atom.getSensorFrequency());
        values.put(BeaconApp.Beacon.COLUMN_NAME_SENSOR_UNIT,
                atom.getSensorUnit());

        // actuator
        values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TITLE,
                atom.getActuatorId());
        values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TRIGGER_SOURCE,
                atom.getActuatorTrigger());
        values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_ACTION,
                atom.getActuatorAction());
        values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE,
                atom.getActuatorCompare());
        values.put(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE_VALUE,
                atom.getActuatorCompareValue());

        if (D)
            Log.d(TAG, values.toString());

        if (D)
            Log.d(TAG, "mUri: " + mUri);

        int u = getContentResolver().update(mUri, values, null, null);

        if (D)
            Log.d(TAG, "new Uri: " + u);
    }

    private final void cancelBeacon(Atom atom) {

    }
}
