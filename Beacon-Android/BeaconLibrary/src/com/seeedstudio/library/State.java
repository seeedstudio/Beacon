package com.seeedstudio.library;

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
    private static final boolean D = false;

    // *********************************************************** //
    // static data
    // *********************************************************** //
    // the light state, high_bit is light, low_bit is black.
    public static final int STATE_DONE = 8;
    public static final int STATE_NONE = 7;
    public static final int START_BIT = 2;
    public static final int HIGH_BIT = 3;
    public static final int LOW_BIT = 4;

    // there are two state per bit, low and high, with different time
    public final static int START_DOWN_BIT = 1;
    public final static int START_UP_BIT = 2;
    public final static int HIGH_DOWN_BIT = 3;
    public final static int HIGH_UP_BIT = 4;
    public final static int LOW_DOWN_BIT = 5;
    public final static int LOW_UP_BIT = 6;

    // flash time for data high_bit time, and low_bit time.
    public static int START_DOWN_TIME = 100;
    public static int START_UP_TIME = 45;
    // high bit
    public static int HIGH_DOWN_TIME = 70;
    public static int HIGH_UP_TIME = 20;
    // low bit
    public static int LOW_DOWN_TIME = 20;
    public static int LOW_UP_TIME = 20;

    // state none time
    public static int STATE_NONE_TIME = 1000;
    // state done time
    public static int STATE_DONE_TIME = 0;

    // even and odd check mode, EVEN_MODE means have even "1" bits.
    public static int EVEN_MODE = 1;
    public static int ODD_MODE = 2;

    // default data to send.
    private int number = 0;
    // state and time list for timer
    private ArrayList<Integer> stateList = new ArrayList<Integer>();
    private ArrayList<Integer> timeList = new ArrayList<Integer>();
    // data to be convert list
    private ArrayList<Integer> dataList = new ArrayList<Integer>();
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
        this.mState = STATE_NONE;
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

    public static int getEVEN_MODE() {
        return EVEN_MODE;
    }

    public static void setEVEN_MODE(int eVEN_MODE) {
        EVEN_MODE = eVEN_MODE;
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

    public int getHIGH_DOWN_TIME() {
        return HIGH_DOWN_TIME;
    }

    public void setHIGH_DOWN_TIME(int hIGH_TIME) {
        HIGH_DOWN_TIME = hIGH_TIME;
    }

    public int getLOW_DOWN_TIME() {
        return LOW_DOWN_TIME;
    }

    public void setLOW_DOWN_TIME(int lOW_TIME) {
        LOW_DOWN_TIME = lOW_TIME;
    }

    public int getHIGH_UP_TIME() {
        return HIGH_UP_TIME;
    }

    public void setHIGH_UP_TIME(int hIGH_UP_TIME) {
        HIGH_UP_TIME = hIGH_UP_TIME;
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
    private boolean setStateAndTime(boolean[] state) {
        if (D)
            Log.d(TAG, "down time: " + getSTART_DOWN_TIME() + "\nup time: "
                    + getSTART_UP_TIME() + "\nhigh up time: "
                    + getHIGH_UP_TIME() + "\nhigh down time: "
                    + getHIGH_DOWN_TIME() + "\nlow time: " + getLOW_DOWN_TIME());

        // checkBit bit
        int checkBit = 0;
        // temp boolean array.
        boolean[] temp;
        temp = new boolean[state.length];

        if (state == null || state.length < 8) {
            Log.d(TAG, "Data error");
            return false;
        }

        // 0001 0101 --> 1010 1000
        for (int i = 0; i < state.length; i++) {
            temp[i] = state[state.length - i - 1];
            // if (D)
            // Log.d(TAG, "flip the boolean array temp[" + i + "] = "
            // + temp[i]);
        }

        for (int i = 0; i < temp.length; i++) {
            // "1"
            if (temp[i]) {
                checkBit++; // checkBit bit plus
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + " state add true");
                addHigh();
                // stateList.add(HIGH_DOWN_BIT);
                // timeList.add(HIGH_DOWN_TIME);
                // stateList.add(HIGH_UP_BIT);
                // timeList.add(HIGH_UP_TIME);

                // "0"
            } else {
                if (D)
                    Log.d(TAG, "temp[" + i + "] is: " + temp[i]
                            + " state add false");
                addLow();
                // addStateAndTime(LOW_DOWN_BIT, LOW_DOWN_TIME);
                // addStateAndTime(LOW_UP_BIT, LOW_UP_TIME);
                // stateList.add(LOW_DOWN_BIT);
                // timeList.add(LOW_DOWN_TIME);
                // stateList.add(LOW_UP_BIT);
                // timeList.add(LOW_UP_TIME);
            }
        }

        // the data's "1" bit is EVEN that mean checkBit bit is "1"
        // otherwise, checkBit bit is "0"
        if (checkBit == 0 || checkBit % 2 == 0) {
            if (D)
                Log.d(TAG, "the checkBit is \"1\"");
            addHigh();
        } else if (checkBit % 2 != 0) {
            if (D)
                Log.d(TAG, "the checkBit is \"0\"");
            addLow();
        }

        return true;
    }

    private void addStateAndTime(int state, int time) {
        if (stateList == null || timeList == null)
            return;

        stateList.add(state);
        timeList.add(time);
    }

    private void addHigh() {
        addStateAndTime(HIGH_DOWN_BIT, HIGH_DOWN_TIME);
        addStateAndTime(HIGH_UP_BIT, HIGH_UP_TIME);
    }

    private void addLow() {
        addStateAndTime(LOW_DOWN_BIT, LOW_DOWN_TIME);
        addStateAndTime(LOW_UP_BIT, LOW_UP_TIME);
    }

    /**
     * prepare all thing before start.
     * 
     * @return
     */
    public boolean prepare() {
        if (dataList == null) {
            return false;
        }

        if (D)
            Log.d(TAG, "dataList size: " + dataList.size());

        prepareStartBit();

        // query dataList and then convert it, set up state and time.
        for (int i = 0; i < dataList.size(); i++) {
            setStateAndTime(convertToByte(dataList.get(i)));
        }

        // dataList.clear();
        prepareEndStateNoneBit();

        return true;
    }

    private void prepareEndStateNoneBit() {

        stateList.add(STATE_NONE);
        timeList.add(STATE_NONE_TIME);
        stateList.add(STATE_DONE);
        timeList.add(STATE_DONE_TIME);

    }

    /**
     * prepare the start bit for state and time list.
     */
    private void prepareStartBit() {
        // add the start bit time.
        timeList.add(1500);
        timeList.add(START_DOWN_TIME);
        timeList.add(START_UP_TIME);

        stateList.add(START_DOWN_BIT);
        stateList.add(START_UP_BIT);
    }

    /**
     * release state and time list.
     */
    private void releaseAllResource() {
        // clear timeList and stateList per time.
        if (!timeList.isEmpty()) {
            timeList.clear();
        }
        if (!stateList.isEmpty()) {
            stateList.clear();
        }

        if (!dataList.isEmpty()) {
            dataList.clear();
        }
    }

    /**
     * release all the resource
     */
    public void release() {
        this.releaseAllResource();
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
    private void start(ArrayList<Integer> state, ArrayList<Integer> time) {
        if (D)
            Log.d(TAG, "state size is: " + state.size() + ", time size is: "
                    + time.size());

        int tempTime = 0;
        for (int j = 0; j < state.size(); j++) {

            // Timer for flip the light block.
            tempTime = tempTime + time.get(j).intValue();
            timer.schedule(new FlipTask(mHandler, state.get(j)), tempTime);

            if (D)
                Log.d(TAG, "time [" + j + "] is: " + tempTime + "\n"
                        + "state [" + j + "] is: " + state.get(j));
        }

        // releaseStateAndTime();
    }

    /**
     * All is reading. starting communicate here.
     */
    public void start() {
        this.start(stateList, timeList);
    }

    /**
     * Timer Task for turn on the "Light block". Start communicating.
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
     * add String data to convert list.
     * 
     * @param str
     */
    public void add(String str) {

        if (str == null)
            return;
        add(Integer.parseInt(str));
    }

    /**
     * add integer data to convert list.
     * 
     * @param integer
     */
    public void add(int integer) {
        int i = 0;

        if (integer > 255) {
            i = (integer >> 8) & 0xff; // high 8 bit
            dataList.add(i);
            integer = integer & 0xff; // low 8 bit
            dataList.add(integer);

            Log.d(TAG, "high 8 bit: " + i);
            Log.d(TAG, "low 8 bit: " + integer);
        } else if (integer < 0) {
            Log.d(TAG, "number erro");
            return;
        } else {
            // dataList.add(0); // high 8 bits
            dataList.add(integer); // low 8 bits
            Log.d(TAG, "0 < integer <= 255: " + integer);
        }

    }

    public void addAtom(Atom atom) {
        add(atom.getDeviceStartInt());
        add(atom.getId()); // device id

        // sensor
        if (atom.getSensorId() != 0) {
            add(atom.getSensorStartInt());
            add(atom.getSensorId());
            if (atom.getSensorFrequency() != Atom.FRQ_NULL) {
                add(atom.getSensorFrequency());
            }

        }
        // if (atom.getSensorFrequency() <= 255) {
        // add(0);
        // add(atom.getSensorFrequency());
        // } else {
        // add(atom.getSensorFrequency());
        // }
        // add(atom.getSensorUnit());

        // actuator
        if (atom.getActuatorId() != 0) {
            add(atom.getActuatorStartInt());
            add(atom.getActuatorId());
            add(atom.getActuatorTrigger());
            add(atom.getActuatorAction());
            add(atom.getActuatorCompare());
            if (atom.getActuatorCompareValue() <= 255) {
                add(0);
                add(atom.getActuatorCompareValue());
            } else {
                add(atom.getActuatorCompareValue());
            }
        }

    }

    /**
     * convert integer to boolean, true is HIGH_BIT, false is LOW_BIT.
     * 
     * @param source
     *            integer to convert
     * @return
     */
    private boolean[] convertToByte(int source) {
        if (source > 255 && source < 0) {
            Log.d(TAG, "number erro");
            source = number;
            return null;
        }

        if (D)
            Log.d(TAG, String.valueOf(source));

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

        // for (int i = 0; i < bitState.length; i++) {
        // if (D) {
        // Log.d(TAG, "convert to boolean array bitState[" + i + "]"
        // + bitState[i]);
        // }
        //
        // }

        return bitState;
    }

    /**
     * convert String to boolean, true is HIGH_BIT, false is LOW_BIT.
     * 
     * @param data
     *            String to convert
     * @return
     */
    private boolean[] convertToByte(String data) {

        int source = Integer.parseInt(data);

        return convertToByte(source);
    }

    /**
     * Combine first and second boolean array.
     * 
     * @param first
     *            stay into at the first block on new boolean array
     * @param second
     * @return
     */
    private boolean[] arrayCombine(boolean[] first, boolean[] second) {

        if (first == null || second == null) {
            return null;
        }

        int firstLen = first.length;
        int secondLen = second.length;

        int len = firstLen + secondLen;

        boolean[] combine = new boolean[len];

        System.arraycopy(first, 0, combine, 0, firstLen);
        System.arraycopy(second, 0, combine, firstLen, secondLen);

        return combine;
    }
}
