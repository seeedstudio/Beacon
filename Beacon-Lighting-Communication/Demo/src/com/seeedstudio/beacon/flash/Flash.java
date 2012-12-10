package com.seeedstudio.beacon.flash;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Flash extends Activity {

    private static final String TAG = "Flash";
    private static final boolean D = true;

    // private final static int START_DOWN_BIT = 1;
    // private final static int START_UP_BIT = 2;
    //
    // private final static int HIGH_DOWN_BIT = 3;
    // private final static int HIGH_UP_BIT = 4;
    //
    // private final static int LOW_DOWN_BIT = 5;
    // private final static int LOW_UP_BIT = 6;

    private static int START_DOWN_TIME = 90;
    private static int START_UP_TIME = 45;
    private static int HIGH_TIME = 165;
    private static int LOW_TIME = 60;

    private ImageView imageView;
    private Button setup, turn, red, green, blue, yellow, cyan, white,
            startDown, startUp, highTime, lowTime;
    private ToggleButton turnKey;
    private EditText editText, startDownEditText, startUpEditText,
            highTimeEditText, lowTimeEditText;

    Timer timer = new Timer();
    boolean flip = false;
    private boolean[] stateBits = { false, false, false, false, false, false,
            false, false };
    private ArrayList<Integer> stateList = new ArrayList<Integer>();
    private ArrayList<Integer> timeList = new ArrayList<Integer>();
    // private HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    State state;
    private int[] nextState = null;
    private int mColor = Color.BLUE;
    private LinearLayout mLinearLayout;

    // final Handler delayHandler = new mHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash);

        mLinearLayout = (LinearLayout) findViewById(R.id.linear1);
        mLinearLayout.setBackgroundColor(Color.BLACK);

        imageView = (ImageView) findViewById(R.id.view);
        imageView.setBackgroundColor(mColor);

        state = new State(this.getApplicationContext(), mHandler);

        red = (Button) findViewById(R.id.set_red);
        green = (Button) findViewById(R.id.set_green);
        blue = (Button) findViewById(R.id.set_blue);
        yellow = (Button) findViewById(R.id.set_yellow);
        cyan = (Button) findViewById(R.id.set_cyan);
        white = (Button) findViewById(R.id.set_white);
        startDown = (Button) findViewById(R.id.start_down_time);
        startUp = (Button) findViewById(R.id.start_up_time);
        highTime = (Button) findViewById(R.id.high_time);
        lowTime = (Button) findViewById(R.id.low_time);

        red.setBackgroundColor(Color.RED);
        green.setBackgroundColor(Color.GREEN);
        blue.setBackgroundColor(Color.BLUE);
        yellow.setBackgroundColor(Color.YELLOW);
        cyan.setBackgroundColor(Color.CYAN);
        white.setBackgroundColor(Color.WHITE);

        red.setOnClickListener(new ClikEvent());
        green.setOnClickListener(new ClikEvent());
        blue.setOnClickListener(new ClikEvent());
        yellow.setOnClickListener(new ClikEvent());
        cyan.setOnClickListener(new ClikEvent());
        white.setOnClickListener(new ClikEvent());
        startDown.setOnClickListener(new ClikEvent());
        startUp.setOnClickListener(new ClikEvent());
        highTime.setOnClickListener(new ClikEvent());
        lowTime.setOnClickListener(new ClikEvent());

        editText = (EditText) findViewById(R.id.edit);
        startDownEditText = (EditText) findViewById(R.id.start_down_time_edit);
        startUpEditText = (EditText) findViewById(R.id.start_up_time_edit);
        highTimeEditText = (EditText) findViewById(R.id.high_time_edit);
        lowTimeEditText = (EditText) findViewById(R.id.low_time_edit);

        setup = (Button) findViewById(R.id.set_frq);
        setup.setOnClickListener(new ClikEvent());

        turn = (Button) findViewById(R.id.button);
        turn.setOnClickListener(new ClikEvent());

        turnKey = (ToggleButton) findViewById(R.id.toggle);
        turnKey.setOnClickListener(new ClikEvent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        imageView.setBackgroundColor(mColor);
        mLinearLayout.setBackgroundColor(Color.BLACK);
    }

    public static int getSTART_DOWN_TIME() {
        return START_DOWN_TIME;
    }

    public static void setSTART_DOWN_TIME(int sTART_DOWN_TIME) {
        START_DOWN_TIME = sTART_DOWN_TIME;
    }

    public static int getSTART_UP_TIME() {
        return START_UP_TIME;
    }

    public static void setSTART_UP_TIME(int sTART_UP_TIME) {
        START_UP_TIME = sTART_UP_TIME;
    }

    public static int getHIGH_TIME() {
        return HIGH_TIME;
    }

    public static void setHIGH_TIME(int hIGH_TIME) {
        HIGH_TIME = hIGH_TIME;
    }

    public static int getLOW_TIME() {
        return LOW_TIME;
    }

    public static void setLOW_TIME(int lOW_TIME) {
        LOW_TIME = lOW_TIME;
    }

    private boolean turnOn(boolean[] state) {
        if (D)
            Log.d(TAG, "down time: " + getSTART_DOWN_TIME() + "\nup time: "
                    + getSTART_UP_TIME() + "\nhigh time: " + getHIGH_TIME()
                    + "\nlow time: " + getLOW_TIME());

        boolean[] temp;
        temp = new boolean[state.length];

        if (stateBits == null || stateBits.length < 8) {
            Log.d(TAG, "Data error");
            return false;
        }

        // 小字节端开始，所以要反过来发。
        for (int i = 0; i < state.length; i++) {
            temp[i] = state[state.length - i - 1];
            if (D)
                Log.d(TAG, "temp[" + i + "] = " + temp[i]);
        }

        if (!timeList.isEmpty()) {
            timeList.clear();
        }

        if (!stateList.isEmpty()) {
            stateList.clear();
        }

        timeList.add(10);
        timeList.add(START_DOWN_TIME);
        timeList.add(START_UP_TIME);

        stateList.add(State.START_DOWN_BIT);
        stateList.add(State.START_UP_BIT);

        for (int i = 0; i < temp.length; i++) {
            if (temp[i]) {
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + "state add true");
                stateList.add(State.HIGH_DOWN_BIT);
                timeList.add(HIGH_TIME);
                stateList.add(State.HIGH_UP_BIT);
                timeList.add(LOW_TIME);
            } else {
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + "state add false");
                stateList.add(State.LOW_DOWN_BIT);
                timeList.add(LOW_TIME);
                stateList.add(State.LOW_UP_BIT);
                timeList.add(LOW_TIME);
            }
        }

        return true;
    }

    private void start(ArrayList<Integer> state, ArrayList<Integer> time) {
        if (D)
            Log.d(TAG, "state size is: " + state.size() + ", time size is: "
                    + time.size());

        int tempTime = 0;
        for (int j = 0; j < state.size(); j++) {

            tempTime = tempTime + time.get(j).intValue();
            timer.schedule(new FlipTask(mHandler, state.get(j)), tempTime);

            if (D)
                Log.d(TAG, "state [" + j + "] is: " + state.get(j) + "\n"
                        + "time [" + j + "] is: " + tempTime);
        }

    }

    class FlipTask extends TimerTask {

        Handler mHandler = null;
        private int nextState;
        boolean next = false;

        public FlipTask() {
        }

        public FlipTask(Handler mHandler, int nextState) {
            this.mHandler = mHandler;
            this.nextState = nextState;
            next = true;
        }

        @Override
        public void run() {
            // Log.d(TAG, "bitState: " + nextState);
            if (next) {
                mHandler.obtainMessage(nextState).sendToTarget();
            }

        }

    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // super.handleMessage(msg);
            switch (msg.what) {

            case State.START_DOWN_BIT:
                imageView.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "start down bit .....");
                }
                break;
            case State.START_UP_BIT:
                imageView.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "start up bit .....");
                }
                break;
            case State.HIGH_DOWN_BIT:
                imageView.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "high down bit .....");
                }
                break;
            case State.HIGH_UP_BIT:
                imageView.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "high up bit .....");
                }
                break;
            case State.LOW_DOWN_BIT:
                imageView.setBackgroundColor(Color.BLACK);
                if (D) {
                    Log.d(TAG, "low down bit .....");
                }
                break;
            case State.LOW_UP_BIT:
                imageView.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "low up bit .....");
                }
                break;
            case State.STAE_NONE:
                imageView.setBackgroundColor(mColor);
                if (D) {
                    Log.d(TAG, "state none bit .....");
                }
                break;
            }
        }

    };

    private class ClikEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
            case R.id.set_red:
                mColor = Color.RED;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.set_green:
                mColor = Color.GREEN;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.set_blue:
                mColor = Color.BLUE;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.set_yellow:
                mColor = Color.YELLOW;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.set_cyan:
                mColor = Color.CYAN;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.set_white:
                mColor = Color.WHITE;
                imageView.setBackgroundColor(mColor);
                break;
            case R.id.start_down_time:
                String down = startDownEditText.getText().toString();
                if (down != null) {
//                    setSTART_DOWN_TIME(Integer.parseInt(down));
                    state.setSTART_DOWN_TIME(Integer.parseInt(down));
                } else {
                    return;
                }
                break;
            case R.id.start_up_time:
                String up = startUpEditText.getText().toString();
                if (up != null) {
                    state.setSTART_UP_TIME(Integer.parseInt(up));
                } else {
                    return;
                }
                break;
            case R.id.high_time:
                String high = highTimeEditText.getText().toString();
                if (high != null) {
                    state.setHIGH_TIME(Integer.parseInt(high));
                } else {
                    return;
                }
                break;
            case R.id.low_time:
                String low = lowTimeEditText.getText().toString();
                if (low != null) {
                    state.setLOW_TIME(Integer.parseInt(low));
                } else {
                    return;
                }
                break;
            case R.id.button:
                flip = !flip;
                if (flip) {
                    imageView.setBackgroundColor(Color.BLACK);
                } else {
                    imageView.setBackgroundColor(mColor);
                }
                break;
            case R.id.set_frq:
                String temp = editText.getText().toString();
                if (temp != null) {
                    stateBits = state.convertToByte(Integer.parseInt(temp));
                } else {
                    return;
                }
                break;
            case R.id.toggle:
                if (turnKey.isChecked()) {
                    // if (turnOn(stateBits)) {
                    // start(stateList, timeList);
                    // }
                    // flipColor(HIGH_DOWN_BIT, 90);
                    String temp2 = editText.getText().toString();
                    if (temp2 != null) {
                        stateBits = state
                                .convertToByte(Integer.parseInt(temp2));
                    } else {
                        Toast.makeText(getApplicationContext(), "No data",
                                Toast.LENGTH_SHORT).show();
                    }

                    if (stateBits != null) {
                        state.setStateAndTime(stateBits);
                        state.start(state.stateList, state.timeList);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Must 0 to 255", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    imageView.setBackgroundColor(mColor);
                }
                break;
            default:
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flash, menu);
        return true;
    }

}
