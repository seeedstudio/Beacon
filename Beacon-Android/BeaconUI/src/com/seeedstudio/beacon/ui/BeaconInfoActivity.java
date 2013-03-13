package com.seeedstudio.beacon.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.seeedstudio.beacon.utility.BeaconApp;
import com.seeedstudio.library.Utility;

public class BeaconInfoActivity extends SherlockActivity {

    private static final String TAG = "BeaconInfoActivity";

    private Button reconfig, delete;
    private TextView device, sensorName, sensorFreq, sensorUnit, actName,
            actAction, actCompare, actCompareValue, actTrigger;
    private Cursor mCursor;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_Sherlock_Light);
        setTitle(R.string.beacon_info);
        setContentView(R.layout.activity_beacon_info);

        // set action bar background
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(
                        R.drawable.action_bar_gradient_background));

        mUri = getIntent().getData();

        if (mUri == null) {
            finish();
            return;
        } else {
            if (Utility.DEBUG)
                Log.d(TAG, "Uri: " + mUri);

            mCursor = getContentResolver().query(mUri,
                    BeaconApp.Beacon.PROJECTION, null, null, null);
            
            if (Utility.DEBUG) {
                
            }

        }

        initUI();

    }

    private void initUI() {
        reconfig = (Button) findViewById(R.id.info_reconfig);
        delete = (Button) findViewById(R.id.info_delete);
        reconfig.setOnClickListener(new ClickEvent());
        delete.setOnClickListener(new ClickEvent());

        // TODO all textview must be setup

        device = (TextView) findViewById(R.id.device_name);
        sensorName = (TextView) findViewById(R.id.sensor_name);
        sensorFreq = (TextView) findViewById(R.id.sensor_freq);
        sensorUnit = (TextView) findViewById(R.id.sensor_uint);
        actName = (TextView) findViewById(R.id.act_name);
        actTrigger = (TextView) findViewById(R.id.actuator_trigger);
        actAction = (TextView) findViewById(R.id.act_action);
        actCompare = (TextView) findViewById(R.id.actuator_compare);
        actCompare = (TextView) findViewById(R.id.actuator_compare_value);

        getBeaconInfo();

    }

    private void getBeaconInfo() {
        mCursor.moveToFirst();
        Log.d(TAG, mCursor.getColumnCount() + "");
        
        // device name
        int index = mCursor.getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_TITLE);
        device.setText(mCursor.getString(index));
        
        // sensor title
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_SENSOR_TITLE);
        sensorName.setText(mCursor.getString(index));
        
        // sensorFrequency
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_SENSOR_FREQ);
        sensorFreq.setText(mCursor.getString(index));
        
        // sensor unit
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_SENSOR_UNIT);
        sensorUnit.setText(mCursor.getString(index) + " second");
        
        // actuator title
        index = mCursor
                .getColumnIndexOrThrow(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TITLE);
        if (Utility.DEBUG)
            Log.d(TAG, "actuator title index is: " + index);
        // actName.setText(mCursor.getString(index));
        
        // actuator trigger source
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_TRIGGER_SOURCE);
        if (Utility.DEBUG)
            Log.d(TAG, "index is: " + index);
        // actTrigger.setText(mCursor.getString(index));
        
        // action
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_ACTION);
        if (Utility.DEBUG)
            Log.d(TAG, "index is: " + index);
        // actAction.setText(mCursor.getString(index));
        
        // compare
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE);
        if (Utility.DEBUG)
            Log.d(TAG, "index is: " + index);
        // actCompare.setText(mCursor.getString(index));
        
        // compare value
        index = mCursor
                .getColumnIndex(BeaconApp.Beacon.COLUMN_NAME_ACTUATOR_COMPARE_VALUE);
        if (Utility.DEBUG)
            Log.d(TAG, "index is: " + index);
        // actCompareValue.setText(mCursor.getString(index));

    }

    private class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO reconfigure and delete beacon which be store in database
            switch (v.getId()) {
            case R.id.info_reconfig:
                Toast.makeText(getApplicationContext(), "Reconfigure",
                        Toast.LENGTH_SHORT).show();
                // TODO jump to setup activity
                reconfigureBeacon();
                BeaconInfoActivity.this.finish();
                break;
            case R.id.info_delete:
                Toast.makeText(getApplicationContext(), "Delete it",
                        Toast.LENGTH_SHORT).show();
                deleteBeacon();
                BeaconInfoActivity.this.finish();
                break;
            default:
                break;
            }
        }

    }

    private void reconfigureBeacon() {
        startActivity(new Intent(Intent.ACTION_EDIT, mUri));
    }

    private final void deleteBeacon() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
