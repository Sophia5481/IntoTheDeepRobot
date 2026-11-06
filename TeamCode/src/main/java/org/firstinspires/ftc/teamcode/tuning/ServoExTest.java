package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Config
@TeleOp(name = "ServoExTest", group = "Test")
public class ServoExTest extends OpMode {
    private ServoEx servo;

    public static String NAME = "intakeSlide";
    public static boolean INVERTED = false;
    public static double ANGLE = 40.0;
    public static double MINANGLE = 0.0;
    public static double MAXANGLE = 90.0;

    public static double POSITION = 0.52;
    public static boolean USEPOSITION = false;

    @Override
    public void init() {
        servo = new SimpleServo(hardwareMap, NAME, MINANGLE, MAXANGLE);
    }

    @Override
    public void loop() {
        if (USEPOSITION) {
            servo.setPosition(POSITION);
        } else {
            servo.setRange(MINANGLE, MAXANGLE);
            servo.setInverted(INVERTED);
            servo.turnToAngle(ANGLE);
        }
    }
}
