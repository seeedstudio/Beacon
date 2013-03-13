package com.seeedstudio.beacon.ui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.seeedstudio.beacon.ui.ConfigureActivity;
import com.seeedstudio.beacon.ui.MainActivity;
import com.seeedstudio.beacon.ui.R;
import com.seeedstudio.library.Atom;

public class SensorListFragment extends ListFragment {

    public interface OnSensorSelectedLister {
        public void onSensorSelected(Atom atom);
    }

    private List<Map<String, String>> listData = null;
    private SimpleAdapter adapter = null;

    OnSensorSelectedLister mSensorListener;

    public SensorListFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mSensorListener = (OnSensorSelectedLister) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implementOnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sensor_frame, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListData();
        // init adapter
        adapter = new SimpleAdapter(getActivity(), listData,
                R.layout.corner_listview, new String[] { "title", "subtitle" },
                new int[] { R.id.corner_listview_title,
                        R.id.corner_listview_subtitle });
        setListAdapter(adapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // the meat of switching the above fragment
    private void switchFragment(Atom atom) {
        if (getActivity() == null)
            return;

        // if (getActivity() instanceof MainActivity) {
        // MainActivity ma = (MainActivity) getActivity();
        // ma.switchContent(atom);
        // SensorListFragment.this.onDestroyView();
        // }
        Intent intent = new Intent(getActivity().getApplicationContext(),
                ConfigureActivity.class);

        Bundle mBundle = new Bundle();
        mBundle.putSerializable(ConfigureActivity.CONFIG_KEY, atom);
        intent.putExtras(mBundle);
        startActivityForResult(intent, MainActivity.CONFIG_REQUEST_CODE);

        mSensorListener.onSensorSelected(atom);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Toast.makeText(getActivity(), "Selected " + position,
                Toast.LENGTH_SHORT).show();
//        switchFragment(new Atom(v.getResources()
//                .getText(R.id.corner_listview_title).toString(), v
//                .getResources().getText(R.id.corner_listview_subtitle)
//                .toString()));
    }

    // ////////////////////////////////////////////////////////
    // data solution
    // ////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("list fragment", "onActivityResult");

        switch (requestCode) {
        case MainActivity.CONFIG_REQUEST_CODE:

            if (data != null && data.hasExtra(ConfigureActivity.CONFIG_KEY)) {
                Log.d("list fragment", "data is added");
                Atom atom = (Atom) data
                        .getSerializableExtra(ConfigureActivity.CONFIG_KEY);

                Toast.makeText(this.getActivity().getApplicationContext(),
                        atom.getName() + " is Added", Toast.LENGTH_LONG).show();

            } else {
                Log.d("list fragment", "Intent data is Null");
                Toast.makeText(this.getActivity().getApplicationContext(),
                        "Intent data is Null", Toast.LENGTH_LONG).show();
                return;
            }
            break;

        default:
            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setListData() {
        listData = new ArrayList<Map<String, String>>();

        Map<String, String> map = new HashMap<String, String>();
        map.put("title", "Beacon Sensor Test");
        map.put("subtitle", "description");
        listData.add(map);

        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("title", "Beacon Sensor Test 2");
        map1.put("subtitle", "description");
        listData.add(map1);

    };
}
