package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Config
@TeleOp(name = "DualServoExTest", group = "Test")
public class DualServoExTest extends OpMode {
    private ServoEx servo;
    private ServoEx servo2;

    public static String NAME = "intakeSlide";
    public static boolean INVERTED = false;
    public static double ANGLE = 40.0;
    public static double MINANGLE = 0.0;
    public static double MAXANGLE = 90.0;

    public static double POSITION = 0.52;
    public static boolean USEPOSITION = false;

    public static String NAME2 = "intakeSlide";
    public static boolean INVERTED2 = false;
    public static double ANGLE2 = 40.0;
    public static double MINANGLE2 = 0.0;
    public static double MAXANGLE2 = 90.0;

    public static double POSITION2 = 0.52;

    @Override
    public void init() {
        servo = new SimpleServo(hardwareMap, NAME, MINANGLE, MAXANGLE);
        servo2 = new SimpleServo(hardwareMap, NAME2, MINANGLE2, MAXANGLE2);
    }

    @Override
    public void loop() {
        if (USEPOSITION) {
            servo.setPosition(POSITION);
            servo2.setPosition(POSITION2);
        } else {
            servo.setRange(MINANGLE, MAXANGLE);
            servo.setInverted(INVERTED);
            servo.turnToAngle(ANGLE);
            servo2.setRange(MINANGLE2, MAXANGLE2);
            servo2.setInverted(INVERTED2);
            servo2.turnToAngle(ANGLE2);
        }
    }
}
