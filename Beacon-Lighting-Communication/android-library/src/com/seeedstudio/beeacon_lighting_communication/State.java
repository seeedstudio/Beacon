package com.seeedstudio.beeacon_lighting_communication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Set up the start, high, low state. Using to convert integer to boolean. Got
 * the state and time of the Value which be send.
 * 
 * @author xiaobo
 * 
 */
public class State {

    // debugging
    private static final String TAG = "State";
    private static final boolean D = true;

    // *********************************************************** //
    // static data
    // *********************************************************** //
    public static final int STAE_NONE = 7;
    public static final int START_BIT = 2;
    public static final int HIGH_BIT = 3;
    public static final int LOW_BIT = 4;

    public final static int START_DOWN_BIT = 1;
    public final static int START_UP_BIT = 2;
    public final static int HIGH_DOWN_BIT = 3;
    public final static int HIGH_UP_BIT = 4;
    public final static int LOW_DOWN_BIT = 5;
    public final static int LOW_UP_BIT = 6;

    // flash time for data
    public int START_DOWN_TIME = 90;
    public int START_UP_TIME = 45;
    public int HIGH_TIME = 165;
    public int LOW_TIME = 60;

    // default data to send.
    private int number = 0;
    // state and time list for timer
    public ArrayList<Integer> stateList = new ArrayList<Integer>();
    public ArrayList<Integer> timeList = new ArrayList<Integer>();
    // Timer for data
    Timer timer = new Timer();
    private Handler mHandler;
    private int mState;

    /**
     * Constructor for State. It need a Handler for IPC with Main UI Activity.
     * 
     * @param context
     * @param mHandler
     */
    public State(Context context, Handler mHandler) {
        this.mHandler = mHandler;
        this.mState = STAE_NONE;
    }

    // *********************************************************** //
    // getter and setter
    // *********************************************************** //
    public synchronized int getmState() {
        return mState;
    }

    public synchronized void setmState(int mState) {
        this.mState = mState;
    }

    public int getSTART_DOWN_TIME() {
        return START_DOWN_TIME;
    }

    public void setSTART_DOWN_TIME(int sTART_DOWN_TIME) {
        START_DOWN_TIME = sTART_DOWN_TIME;
    }

    public int getSTART_UP_TIME() {
        return START_UP_TIME;
    }

    public void setSTART_UP_TIME(int sTART_UP_TIME) {
        START_UP_TIME = sTART_UP_TIME;
    }

    public int getHIGH_TIME() {
        return HIGH_TIME;
    }

    public void setHIGH_TIME(int hIGH_TIME) {
        HIGH_TIME = hIGH_TIME;
    }

    public int getLOW_TIME() {
        return LOW_TIME;
    }

    public void setLOW_TIME(int lOW_TIME) {
        LOW_TIME = lOW_TIME;
    }

    // *********************************************************** //
    // get the state and time of data.
    // *********************************************************** //

    /**
     * Convert state and time for per bit in data.
     * 
     * if ok, return true, otherwise false;
     * 
     * @param state
     * @return
     */
    public boolean setStateAndTime(boolean[] state) {
        if (D)
            Log.d(TAG, "down time: " + getSTART_DOWN_TIME() + "\nup time: "
                    + getSTART_UP_TIME() + "\nhigh time: " + getHIGH_TIME()
                    + "\nlow time: " + getLOW_TIME());

        // temp boolean array.
        boolean[] temp;
        temp = new boolean[state.length];

        if (state == null || state.length < 8) {
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

        stateList.add(START_DOWN_BIT);
        stateList.add(START_UP_BIT);

        for (int i = 0; i < temp.length; i++) {
            if (temp[i]) {
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + "state add true");
                stateList.add(HIGH_DOWN_BIT);
                timeList.add(HIGH_TIME);
                stateList.add(HIGH_UP_BIT);
                timeList.add(LOW_TIME);
            } else {
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + "state add false");
                stateList.add(LOW_DOWN_BIT);
                timeList.add(LOW_TIME);
                stateList.add(LOW_UP_BIT);
                timeList.add(LOW_TIME);
            }
        }

        return true;
    }

    /**
     * All is reading. starting communicate here.
     * 
     * you must get state and time list from setStateAndTime() method. It will
     * be save as static data in State class. so just call start() like this
     * start(state.stateList, state.timeList).
     * 
     * @param state
     * @param time
     */
    public void start(ArrayList<Integer> state, ArrayList<Integer> time) {
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

    /**
     * Timer Task for turn on the "Lighting block". Start communicating.
     * 
     * @author xiaobo
     * 
     */
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

    // *********************************************************** //
    // get data and convert to
    // *********************************************************** //

    /**
     * convert integer to boolean, true is HIGH_BIT, false is LOW_BIT.
     * 
     * @param source
     *            integer to convert
     * @return
     */
    public boolean[] convertToByte(int source) {
        if (source > 255 && source < 0) {
            Log.d(TAG, "number erro");
            source = number;
            return null;
        }

        boolean[] bitState = new boolean[8];
        Arrays.fill(bitState, true);

        bitState[0] = ((source & 1) == 1); // when sendme[i]= xxxx xxx1; xxxx
                                           // xxx1 & 0000 0001
        bitState[1] = ((source & 2) == 2); // meanings bit is TRUE;
        bitState[2] = ((source & 4) == 4);
        bitState[3] = ((source & 8) == 8);
        bitState[4] = ((source & 16) == 16);
        bitState[5] = ((source & 32) == 32);
        bitState[6] = ((source & 64) == 64);
        bitState[7] = ((source & 128) == 128);

        for (int i = 0; i < bitState.length; i++) {
            if (D) {
                Log.d(TAG, "bitState[" + i + "]" + bitState[i]);
            }

        }

        return bitState;
    }

    /**
     * convert integer to boolean, true is HIGH_BIT, false is LOW_BIT.
     * 
     * @param data
     *            String to convert
     * @return
     */
    public boolean[] convertToByte(String data) {
        int source = Integer.parseInt(data);

        if (source > 255 && source < 0) {
            Log.d(TAG, "number erro");
            source = number;
            return null;
        }

        boolean[] bitState = new boolean[8];
        Arrays.fill(bitState, true);

        bitState[0] = ((source & 1) == 1); // when sendme[i]= xxxx xxx1; xxxx
                                           // xxx1 & 0000 0001
        bitState[1] = ((source & 2) == 2); // meanings bit is TRUE;
        bitState[2] = ((source & 4) == 4);
        bitState[3] = ((source & 8) == 8);
        bitState[4] = ((source & 16) == 16);
        bitState[5] = ((source & 32) == 32);
        bitState[6] = ((source & 64) == 64);
        bitState[7] = ((source & 128) == 128);

        for (int i = 0; i < bitState.length; i++) {
            if (D) {
                Log.d(TAG, "bitState[" + i + "]" + bitState[i]);
            }

        }

        return bitState;
    }

}
