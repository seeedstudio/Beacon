Beacon-Lighting-Communication
=============================

Beacon communicate library with light dependent resistor (i.e photoresistance) in Android.

## Usage

This library just have one class for now.

Normally, you have to instantiate it at first. and then, you can use it like below step:

 - get data, converting it to boolean arrary.
 - set up state and time, and then store them to ArraryList<Integer>.
 - start it.

Also, you don't to know all the detail. There are code sample.
    
    // assume the data is String data type
    String data = "1";
    // instantiate State with a Handler.
    State state = new State(getApplicationContext(), mHandler);
    // createa boolean arrary and convert the data to boolean arrary.
    boolean[] bits = new boolean[8];
    bits = state.convertToByte(data);
    // start it with state and time ArraryList data.
    state.start(state.stateList, state.timeList);