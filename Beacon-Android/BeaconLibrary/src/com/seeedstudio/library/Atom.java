package com.seeedstudio.library;

import java.io.Serializable;

/**
 * Preference of Beacon Atom, include sensor and actuator.
 * 
 * @author xiaobo
 * 
 */
public class Atom implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -9042424498470988075L;
    // debugging
    private static final boolean D = Utility.DEBUG;
    private static final String TAG = "Beacon";

    // TODO:
    // 作为一个 adapter 的 item。
    // sensor, actuator item, Done

    // ////////////////////////////////////////////////////
    // field nubmer
    // ////////////////////////////////////////////////////

    // frequency
    public static final int FRQ_FAST = 0x80;
    public static final int FRQ_STD = 0x81;
    public static final int FRQ_LOW = 0x82;
    public static final int FRQ_MINI = 0x83;
    public static final int FRQ_NULL = 0x84;
    
    public static final int UNIT_MS = 0;
    public static final int UNIT_S = 1;
    public static final int UNIT_MIN = 2;
    public static final int UNIT_HOUR = 3;

    public static final int ACTIN_TYPE_ON = 1;
    public static final int ACTIN_TYPE_OFF = 2;

    public static final int COMPARE_TYEP_GREATER = 1;
    public static final int COMPARE_TYEP_LESS = 2;
    public static final int COMPARE_TYEP_EQUAL = 3;
    public static final int COMPARE_TYEP_NULL = 4;

    // sensor and actuator
    private Sensor sensor;
    private Actuator actuator;

    // device name
    private String name = "Beacon";
    private String description = "description";
    private int id = 0;

    // private Bitmap bitmap = Bitmap.createBitmap("");

    // emum for unit, action and compare
    public enum Unit {
        UNIT_MS, UNIT_S, UNIT_MIN, UNIT_HOUR
    }

    public enum Action {
        ACTIN_TYPE_ON, ACTIN_TYPE_OFF
    }

    public enum Compare {
        COMPARE_TYEP_GREATER, COMPARE_TYEP_LESS, COMPARE_TYEP_EQUAL, COMPARE_TYEP_NULL
    }

    // 0x51, 0x52, 0x53 start bit, integer version
    private int deviceStartInt = 81;
    private int sensorStartInt = 82;
    private int actuatorStartInt = 84;

    public Atom() {

    }

    public Atom(String name, String description, int id) {
        this.name = name;
        this.description = description;
        this.id = id;
        sensor = new Sensor();
        actuator = new Actuator();
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDeviceStartInt() {
        return deviceStartInt;
    }

    public void setDeviceStartInt(int deviceStartInt) {
        this.deviceStartInt = deviceStartInt;
    }

    public int getSensorStartInt() {
        return sensorStartInt;
    }

    public void setSensorStartInt(int sensorStartInt) {
        this.sensorStartInt = sensorStartInt;
    }

    public int getActuatorStartInt() {
        return actuatorStartInt;
    }

    public void setActuatorStartInt(int actuatorStartInt) {
        this.actuatorStartInt = actuatorStartInt;
    }

    // sensor getter and setter
    public int getSensorId() {
        return this.sensor.getId();
    }

    public void setSensorId(int id) {
        this.sensor.setId(id);
    }

    public int getSensorFrequency() {
        return this.sensor.getFrequency();
    }

    public void setSensorFrequency(int frequency) {
        this.sensor.setFrequency(frequency);
    }

    public int getSensorUnit() {
        return this.sensor.getUnit();
    }

    public void setSensorUnit(int unit) {
        this.sensor.setUnit(unit);
    }

    // actuator setter and getter
    public int getActuatorId() {
        return this.actuator.getId();
    }

    public void setActuatorId(int id) {
        this.actuator.setId(id);
    }

    public int getActuatorTrigger() {
        return this.actuator.getTrigger();
    }

    public void setActuatorTrigger(int trigger) {
        this.actuator.setTrigger(trigger);
    }
    
    public int getActuatorAction() {
        return this.actuator.getAction();
    }

    public void setActuatorAction(int action) {
        this.actuator.setAction(action);
    }

    public int getActuatorCompare() {
        return this.actuator.getCompare();
    }

    public void setActuatorCompare(int compare) {
        this.actuator.setCompare(compare);
    }

    public int getActuatorCompareValue() {
        return this.actuator.getCompareValue();
    }

    public void setActuatorCompareValue(int compareValue) {
        this.actuator.setCompareValue(compareValue);

    }

    /**
     * Sensor item
     * 
     * @author xiaobo
     * 
     */

    private class Sensor implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 5264170777994406528L;
        // TODO
        // id, broadcast frequency, unit
        // sensor id
        int id = 1;
        int frequency = 1;
        int unit = UNIT_MS;

        public Sensor() {
            super();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getFrequency() {
            return frequency;
        }

        public void setFrequency(int frequency) {
            this.frequency = frequency;
        }

        public int getUnit() {
            return unit;
        }

        public void setUnit(int unit) {
            this.unit = unit;
        }

    }

    /**
     * Actuator item
     * 
     * @author xiaobo
     * 
     */
    private class Actuator implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = -4510278294072150461L;
        // actuator id
        int id = 2;
        int trigger = 1;
        int action = ACTIN_TYPE_ON;
        int compare = COMPARE_TYEP_GREATER;
        int compareValue = 20;

        public Actuator() {
            super();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getTrigger() {
            return trigger;
        }

        public void setTrigger(int trigger) {
            this.trigger = trigger;
        }

        public int getAction() {
            return action;
        }

        public void setAction(int action) {
            this.action = action;
        }

        public int getCompare() {
            return compare;
        }

        public void setCompare(int compare) {
            this.compare = compare;
        }

        public int getCompareValue() {
            return compareValue;
        }

        public void setCompareValue(int compareValue) {
            this.compareValue = compareValue;
        }

    }

    /**
     * Beacon send action
     * 
     * @author xiaobo
     * 
     */
    public interface OnLighting {

        public void onSend();
    }

}
