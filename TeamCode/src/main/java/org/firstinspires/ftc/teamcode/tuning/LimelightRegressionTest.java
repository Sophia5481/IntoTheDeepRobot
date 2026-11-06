package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.ConvertToRobotMovement;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

@Config
@TeleOp(name = "LimelightRegressionTest", group = "Test")
public class LimelightRegressionTest extends OpMode {
    private ServoEx servo;

    public static String NAME = "intakeSlide";
    public Intake intake;
    public static boolean INVERTED = false;
    public static double ANGLE = 40.0;
    public static double MINANGLE = 0.0;
    public static double MAXANGLE = 90.0;

    public static double y_dist = 8.0;

    public static double POSITION = 0.52;
    public static boolean USEPOSITION = true;

    @Override
    public void init() {
        intake = new Intake(hardwareMap);
    }

    @Override
    public void loop() {
        intake.slidePos(ConvertToRobotMovement.convertToSlideOffset(y_dist, true));
    }
}
