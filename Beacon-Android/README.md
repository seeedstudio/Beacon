## Beacon-Android-App
**WARNING** **I delete the Beacon-Lighting-Communication**, It be instead of Android Beacon setup App.
The setup App relay by BeaconLibrary.

And ... of course. It still WIP.

## Beacon-Lighting-Communication

Beacon communicate library with light dependent resistor (i.e photoresistance)
in Android.

How android system communicate with electronic system? Via WiFi? GPRS? Or
Bluetooth? Related module would be needed in the electronic system, for
example, a Wifi module, but usually, this kind of modules is often too
expensive especially when the communication is simple.

With the visible light communication, a so expensive module is not needed
anymore. A $0.1 phototransistor is enough. The android system firstly encode
the message, and then sent the message via blink of screen. The electronic
system receive the message via phototransistor and then decode the message.
Suitable for Beacon system.Library and a simple Demo are included.

## Below Usage manual is `Deprecated`
The Library refactor is WIP. Comming back soon ....

## Usage

This repo include [Android Library](https://github.com/seeedstudio/Beacon/tree/master/Beacon-Lighting-Communication/library)
and [Demo](https://github.com/seeedstudio/Beacon/tree/master/Beacon-Lighting-Communication/Demo)

This library just have one class for now.

Normally, you have to instantiate it at first. and then, you can use it like
below step:

 - get data, converting it to boolean arrary.
 - set up state and time, and then store them to ArraryList<Integer>.
 - start it.

Also, you don't to know all the detail. There are code sample.

changelog 1/10:

    // assume the data is String data type
    String data = "1";

    // instantiate State with a Handler.
    State state = new State(getApplicationContext(), mHandler);

    // add data to state
    state.add(data);

    // prepare send
    state.prepare();

    // start send.
    state.start();


Below code snippet is outdate.

    // assume the data is String data type
    String data = "1";

    // instantiate State with a Handler.
    State state = new State(getApplicationContext(), mHandler);

    // createa boolean arrary and convert the data to boolean arrary.
    boolean[] bits = new boolean[8];
    bits = state.convertToByte(data);

    // start it with state and time ArraryList data.
    state.start(state.stateList, state.timeList);
