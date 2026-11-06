package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.commands.VoltageSensing;

@Config
@TeleOp(name = "MotorExTest", group = "Test")
public class MotorExTest extends OpMode {
    private MotorEx motor;

    public static String NAME = "bucket";
    public static boolean INVERTED = true;
    public static double POWER = 0.0;
    public static Motor.ZeroPowerBehavior zeroPowerBehavior = Motor.ZeroPowerBehavior.FLOAT;

    @Override
    public void init() {
        motor = new MotorEx(hardwareMap, NAME);
        VoltageSensing.initialize(hardwareMap);
    }

    @Override
    public void loop() {
        motor.setInverted(INVERTED);
        motor.setZeroPowerBehavior(zeroPowerBehavior);
        motor.set(VoltageSensing.mapMotorPower(POWER));
        telemetry.addData("POWER", VoltageSensing.mapMotorPower(POWER));
    }
}
