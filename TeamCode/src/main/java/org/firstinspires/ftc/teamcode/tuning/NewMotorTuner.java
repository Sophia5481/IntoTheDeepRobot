package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.commands.MotorController;
import org.firstinspires.ftc.teamcode.commands.MotorMoveCommandNew;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.VoltageSensing;

// DOES NOT WORK
@Config
@TeleOp(name = "Motor Tuning Test", group = "Test")
public class NewMotorTuner extends OpMode {
    private MotorEx motor;

    public static String NAME = "bucket";
    public static boolean INVERTED = true;
    public static double POWER = 0.0;
    public static Motor.ZeroPowerBehavior zeroPowerBehavior = Motor.ZeroPowerBehavior.FLOAT;

    public static double SAMPLE_KP = 0.01;
    public static double SAMPLE_KD = 0.001;
    public static double SAMPLE_KF = 0.1;
    public static double SAMPLE_TOLERANCE = 50;
    public MotorController sampleController;

    public static double target = 600;

    @Override
    public void init() {
        motor = new MotorEx(hardwareMap, NAME);
        motor.setPositionCoefficient(0);
        motor.setInverted(true);
//        sampleController = new MotorPeriodicController(sampleCoeffs, sampleMotor);
        sampleController = new MotorController(motor, SAMPLE_KP, SAMPLE_KD, SAMPLE_KF, SAMPLE_TOLERANCE);
        VoltageSensing.initialize(hardwareMap);
    }

    @Override
    public void loop() {
        new SequentialCommandGroup(
                new MotorMoveCommandNew(sampleController, target),
                new WaitCommand(2000),
                new MotorMoveCommandNew(sampleController, 0),
                new WaitCommand(2000)
        );
    }
}
