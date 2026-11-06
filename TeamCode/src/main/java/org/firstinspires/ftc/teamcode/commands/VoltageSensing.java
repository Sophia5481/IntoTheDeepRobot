package org.firstinspires.ftc.teamcode.commands;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.Range;

public class VoltageSensing {
    static VoltageSensor sensor;
    static long lastRead = 0;
    static double lastValue = 0;

    public static long UPDATE_TIME = 100;
    public static double TARG_VOLTAGE = 12.0;

    public static void initialize(HardwareMap hardwareMap) {
        sensor = hardwareMap.voltageSensor.iterator().next();
    }

    public static double readVoltage() {
        if (System.currentTimeMillis() - lastRead > UPDATE_TIME) {
            lastValue = sensor.getVoltage();
            lastRead = System.currentTimeMillis();
        }
        return lastValue;
    }

    // Maps a regular motor power to a motor power assuming a constant voltage of targVoltage
    public static double mapMotorPower(double power) {
        power *= TARG_VOLTAGE/readVoltage();
        return Range.clip(power, -1, 1);
    }
}
