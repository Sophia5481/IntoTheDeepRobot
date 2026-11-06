package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.DualServoMoveCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.GiveToHP;

@Config
public class Intake extends SubsystemBase {
    private final ServoEx slideServo;
    private final ServoEx slideServo2;
    public static double INTAKE_IN = 0.85; // 0.83 0.35
    public static double INTAKE_BARELY_OUT = 0.7; // 0.68 0.45
    public static double INTAKE_HALFWAY = 0.7; //0.62 0.55
    // not used
    public static double INTAKE_PUSHING = 0.6; //0.43 0.7
    public static double INTAKE_TRANSFER = 0.85; //0.83 0.37
    public static double INTAKE_OUT = 0.32; //0.25 0.95

    private final RevColorSensorV3 colorSensor;

    private final MotorEx intakeMotor;
    public static double MOTOR_INTAKE = -0.9; //-1.0
    public static double MOTOR_INTAKE_TRANSFER = -0.5;
    public static double MOTOR_REVERSE = 0.6;
    public static double MOTOR_SLOW_REVERSE = 0.30;

    private final ServoEx sweeper;
    public static double SWEEPER_MINANGLE = 0.0;
    public static double SWEEPER_MAXANGLE = 90.0;

    private final ServoEx wrist;
    public static double WRIST_MINANGLE = -20;
    public static double WRIST_MAXANGLE = 110.0;
    public static double WRIST_INTAKE = 47.0;
    public static double WRIST_INTAKE_SLIDE_OUT = 54.0;

    private final MotorEx hangMotor;
    public static double HANG_P = 0.015;
    public double ratio = 223.0 / 117.0;
    public static int HANG_EXTENDED = 3800 * 223 / 117;
    public static int HANG_HANGING = 2550 * 223 / 117;

    private final Servo led;

    private final Limelight3A limelight;

    public boolean INTAKING = false;
    public boolean AUTO = false;

    public Intake(final HardwareMap hMap) {
        slideServo = new SimpleServo(hMap, "intakeSlide", 0, 180);
        slideServo2 = new SimpleServo(hMap, "intakeSlide2", 0, 180);

        colorSensor = hMap.get(RevColorSensorV3.class, "colorSensor");

        intakeMotor = new MotorEx(hMap, "intakeMotor");
        intakeMotor.setRunMode(Motor.RunMode.RawPower); // TODO: Make this velocity control

        wrist = new SimpleServo(hMap, "wrist", WRIST_MINANGLE, WRIST_MAXANGLE);
        wrist.setInverted(false);

        sweeper = new SimpleServo(hMap, "sweeper", SWEEPER_MINANGLE, SWEEPER_MAXANGLE);

        hangMotor = new MotorEx(hMap, "hangMotor");
        hangMotor.setInverted(true);
        hangMotor.setRunMode(Motor.RunMode.PositionControl);
        hangMotor.setPositionCoefficient(HANG_P);
        hangMotor.setTargetPosition(0);
        hangMotor.setPositionTolerance(3);

        led = hMap.get(Servo.class, "led");

        limelight = hMap.get(Limelight3A.class, "limelight");
    }

    public void init(boolean auto) {
        AUTO = auto;
        slideServo.setPosition(INTAKE_IN);
        intakeMotor.set(0.0);
        sweeper.turnToAngle(35.0);
        if (auto) {
            wristTransfer();
        } else {
            wristStored();
        }
        // THERE'S NO WAY THE LIMELIGHT WAS RUNNING THE WHOLE AUTO AND I DIDN'T NOTICE 😭😭😭😭😭😭
        // 😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭😭
//        limelight.pipelineSwitch(0);
         // wristTransfer()
    }

    // checks if either there is no sample or if the sample is the wrong color
    // checks if the robot needs to take action to get the right color / get a sample
    public boolean isWrongColor() {
        updateSampleColor();

        // prevent null pointer exception
        if (intakedColor == null) {
            return true;
        }
        if (intakedColor == SampleColor.BLUE && Robot.RED_ALLIANCE || intakedColor == SampleColor.RED && !Robot.RED_ALLIANCE || !sampleIntaked()) {
            Log.d("Wrong color?", "Oh yeah.");
            Log.d("Sample color: ", "" + intakedColor);
            Log.d("Alliance Color: ", "" + Robot.RED_ALLIANCE);
        } else {
            Log.d("Wrong color?", "No way!");
        }
        return intakedColor == SampleColor.BLUE && Robot.RED_ALLIANCE || intakedColor == SampleColor.RED && !Robot.RED_ALLIANCE || !sampleIntaked();
    }

    // checks if the sample specifically is the wrong color and needs reversing
    // otherwise, if there isn't a sample, then it will return false
    public boolean needReverse() {
        // make sure it's getting the most recent color data
        updateSampleColor();
        // prevent null pointer exception
        if (intakedColor == null) {
            return false;
        }
        if (intakedColor == SampleColor.BLUE && Robot.RED_ALLIANCE || intakedColor == SampleColor.RED && !Robot.RED_ALLIANCE) {
            Log.d("Wrong color? ", "unfortunately yes");
        } else {
            Log.d("Wrong color? ", "Nah we good");
        }
        return (intakedColor == SampleColor.BLUE && Robot.RED_ALLIANCE || intakedColor == SampleColor.RED && !Robot.RED_ALLIANCE);
    }

    // checks specifically if the color is specifically the correct alliance color for specimen autotransfer
    public boolean isCorrectColor() {
        // prevent null pointer exception
        if (intakedColor == null) {
            return false;
        }
        if (intakedColor == SampleColor.RED && Robot.RED_ALLIANCE || intakedColor == SampleColor.BLUE && !Robot.RED_ALLIANCE) {
            Log.d("Correct color?", "Oh yeah.");
            Log.d("Sample color: ", "" + intakedColor);
            Log.d("Alliance Color: ", "" + Robot.RED_ALLIANCE);
        } else {
            Log.d("Correct color?", "No way!");
        }
        return intakedColor == SampleColor.RED && Robot.RED_ALLIANCE || intakedColor == SampleColor.BLUE && !Robot.RED_ALLIANCE;
    }

    public void home() throws InterruptedException {
        hangMotor.setRunMode(Motor.RunMode.RawPower);
        hangMotor.setPositionCoefficient(HANG_P - 1.0);
        while (hangMotor.motorEx.getCurrent(CurrentUnit.AMPS) < 4.0) {
            hangMotor.set(-0.5);
        }
        Thread.sleep(100); // Let it stop spinning
        hangMotor.set(0.0);
        Thread.sleep(100); // Let it come out of the pushed position
        hangMotor.stopAndResetEncoder();
        hangMotor.setRunMode(Motor.RunMode.PositionControl);
        hangMotor.setTargetPosition(0);
        hangMotor.setPositionCoefficient(HANG_P);
    }

    boolean hanging = false;

    public SampleColor intakedColor = null;
    // auto transfer for sample
    public Outtake AUTOTRANSFER = null;
    // auto transfer to give to HP in specimen
    public Outtake AUTOHP = null;
    @Override
    public void periodic() {
        if ((!hangMotor.atTargetPosition() || hanging) && hangMotor.getPositionCoefficient() == HANG_P) {
            hangMotor.set(1.0);
        }

        Log.d("Intaking: ", "" + this.needReverse());
        // Teleop intaking; repeatedly checks the state of the intake to automatically reverse/retract
        if (INTAKING && !AUTO && sampleIntaked()) {
            if (isWrongColor()) {
                // if wrong color, reverse
                CommandScheduler.getInstance().schedule(new IntakeReverseCommand(this, /*(AUTOTRANSFER == null) ? false : */ true));
            } else {
                Log.d("SAD", "SAD");
                if (System.currentTimeMillis() - sampleIntakeTime > 300) {
                    intakeOff();
                    if (AUTOTRANSFER != null) {
                        CommandScheduler.getInstance().schedule(new SequentialCommandGroup(
                                new InstantCommand(() -> INTAKING = false),
//                                new WaitCommand(150),
                                new IntakeRetractCommand(this, AUTOTRANSFER),
                                new ExtendBucketCommand(AUTOTRANSFER, true, true)
                        ));
                    }
                    if (AUTOHP != null && isCorrectColor()) {
                        CommandScheduler.getInstance().schedule(new SequentialCommandGroup(
                                new InstantCommand(() -> INTAKING = false),
//                                new WaitCommand(150),
                                new IntakeRetractCommand(this, AUTOHP),
                                new GiveToHP(AUTOHP, false)
                        ));
                    }
                }
                wristTransfer();
            }
            // Un-jamming
            // TODO: Test the unjamming code
//            if (intakeJammed()) {
//                CommandScheduler.getInstance().schedule(new SequentialCommandGroup(
//                        new InstantCommand(this::wristReverse),
//                        new InstantCommand(this::intakeReverse),
//                        new WaitUntilCommand(() -> intakeMotor.motorEx.getCurrent(CurrentUnit.AMPS) < 5.0),
//                        new InstantCommand(this::wristIntake),
//                        new InstantCommand(this::intakeOn)
//                ));
//            }
        }


    }

    public boolean intakeJammed() {
        Log.d("Submersible intaking: ", "motor current: " + intakeMotor.motorEx.getCurrent(CurrentUnit.AMPS));
        Log.d("Submersible intaking: ", "motor velocity: " + Math.abs(intakeMotor.getVelocity()));
        Log.d("Submersible intaking: ", "motor power diff: " + Math.abs(intakeMotor.get() - MOTOR_INTAKE));
        if (INTAKING && intakeMotor.motorEx.getCurrent(CurrentUnit.AMPS) > 5.0 && Math.abs(intakeMotor.getVelocity()) < 20 && Math.abs(intakeMotor.get() - MOTOR_INTAKE) < 0.05) {
            Log.d("Submersible intaking: ", "intake jammed");
            return true;
        } else {
            Log.d("Submersible intaking: ", "intake not jammed");
            return false;
        }
    }

    public void slideOut() {
        slideServo.setPosition(INTAKE_OUT);
        slideServo2.setPosition(INTAKE_OUT);
    }

    public DualServoMoveCommand slideOutTime(long time_ms) {
        return new DualServoMoveCommand(this, slideServo, slideServo2, INTAKE_OUT, time_ms, true);
    }

    public void slideIn() {
        slideServo.setPosition(INTAKE_IN);
        slideServo2.setPosition(INTAKE_IN);
    }

    public boolean isSlideIn() {
        Log.d("Slide in: ", "" + slideServo.getPosition());
        return (Math.abs(slideServo.getPosition() - INTAKE_IN) < 0.01);
    }

    public void slideBarelyOut() {
        slideServo.setPosition(INTAKE_BARELY_OUT);
        slideServo2.setPosition(INTAKE_BARELY_OUT);
    }

    public void slideHalfway() {
        slideServo.setPosition(INTAKE_HALFWAY);
        slideServo2.setPosition(INTAKE_HALFWAY);
    }

    public void slidePushing() {
        slideServo.setPosition(INTAKE_PUSHING);
        slideServo2.setPosition(INTAKE_PUSHING);
    }

    public void slideTransfer() {
        slideServo.setPosition(INTAKE_TRANSFER);
        slideServo2.setPosition(INTAKE_TRANSFER);
    }

    public boolean slideIsOut() {
        return Math.abs(slideServo.getPosition() - INTAKE_OUT) < 0.01 || slideServo.getPosition() > INTAKE_HALFWAY - 0.01;
    }

    public void slidePos(double offset) {
        slideServo.setPosition(INTAKE_HALFWAY + (INTAKE_OUT - INTAKE_HALFWAY)*offset);
        slideServo2.setPosition(INTAKE_HALFWAY + (INTAKE_OUT - INTAKE_HALFWAY)*offset);
        Log.d("Slide servo pos:", Double.toString(INTAKE_HALFWAY + (INTAKE_OUT - INTAKE_HALFWAY)*offset));
//        wrist.setPosition(WRIST_INTAKE + (WRIST_INTAKE_SLIDE_OUT - WRIST_INTAKE)*offset);
    }

    public double getSlidePos() {
        // x is pos
        // 0.7 + (0.32 - 0.7) * x = value
        // x = value - 0.7 / (0.32 - 0.7)
        return (slideServo.getPosition() - 0.7) / (0.32 - 0.7);
    }

    public void intakeOn() {
        intakeMotor.set(MOTOR_INTAKE);
    }

    public void intakeTransferOn() {
        intakeMotor.set(MOTOR_INTAKE_TRANSFER);
    }

    public void intakeOff() {
        intakeMotor.set(0.0);
    }

    public void intakeReverseSlow() {
        intakeMotor.set(MOTOR_SLOW_REVERSE);
    }

    public void intakeReverse() {
        intakeMotor.set(MOTOR_REVERSE);
    }

    public boolean isIntakeReversing() {
        return intakeMotor.getVelocity() > 0.1;
    }

    public boolean isIntakeOn() {
        return (intakeMotor.getVelocity() < -0.1);
    }

    public void hangExtended() {
        hangMotor.setTargetPosition(HANG_EXTENDED);
        hanging = true;
    }
    public void hangHanging() {
        hangMotor.setTargetPosition(HANG_HANGING);
    }


    public void wristIntake() {
        wrist.turnToAngle(WRIST_INTAKE);
        Log.d("Wrist intake:", "I TRIED MY BEST THO");
    }

    public void wristIntake(double pos) {
        wrist.turnToAngle(WRIST_INTAKE + (WRIST_INTAKE_SLIDE_OUT - WRIST_INTAKE) * pos);
    }

    public double getWristAngle() {
        return wrist.getAngle();
    }

    public boolean isWristIntake() {
        return Math.abs(wrist.getAngle() - (WRIST_INTAKE)) < 0.01;
    }

    public void wristTransfer() {
        wrist.turnToAngle(95);
    }

    public void wristPreIntake() {
        wrist.turnToAngle(95);
    }

    public void wristHalfway() {
        wrist.turnToAngle(55);
    }

    public void wristPushing() {wrist.turnToAngle(55); }

    public void wristStored() {wrist.turnToAngle(60); }

    public void wristReverse() {
        wrist.turnToAngle(70);
    }

    // make wrist horizontal while claw is grabbing sample out of the intake
    // prevents the flexible roof from smacking the sample out of the outtake claw
    public void wristHorizontal() {
        wrist.turnToAngle(80);
    }

    boolean sampleIntakeLastVal = false;
    long sampleIntakeLastUpdate = 0;
    long sampleIntakeTime = 0;
    public boolean displayColor = false;
    public boolean noDisplayColor = false; // Need to set this to true to show colors during manual drive
    public boolean forceSampleIntaked() {
        return colorSensor.getDistance(DistanceUnit.INCH) < 1.4;
    }
    public boolean sampleIntaked() {
        if (System.currentTimeMillis() - sampleIntakeLastUpdate > 25) {
            updateSampleColor();
            double distance = colorSensor.getDistance(DistanceUnit.INCH);
            boolean newVal = distance < 1.4;
            Log.d("Color Sensor Distance", "Distance is " + distance + " inches");
            if (newVal) {
                Log.d("Color Sensor Distance", "TRUE");
            } else {
                Log.d("Color Sensor Distance", "FALSE");
            }

            if (!sampleIntakeLastVal && newVal) {
                sampleIntakeTime = System.currentTimeMillis();
            }

            sampleIntakeLastVal = newVal;
            sampleIntakeLastUpdate = System.currentTimeMillis();
        }
        Log.d("Sample Intaked:", sampleIntakeLastVal ? "true" : "false");
        return sampleIntakeLastVal;
    }

    public void displayColor(double color) {
        if (Math.abs(led.getPosition() - color) > 0.01) {
            led.setPosition(color);
        }
    }

    public enum SampleColor {
        RED, YELLOW, BLUE
    }
    public void updateSampleColor() {
        // classify color based on which RBG value is the greatest
        // in the edge case that all are really small because there's no sample,
        // the intakedColor is then changed to null.
        NormalizedRGBA color = colorSensor.getNormalizedColors();
        Log.d("Sample Color: ", "Red Colors: " + color.red);
        Log.d("Sample Color: ", "Blue Colors: " + color.blue);
        Log.d("Sample Color: ", "Green Colors: " + color.green);
        if (color.green > color.blue && color.green > color.red) {
            intakedColor = SampleColor.YELLOW;
            return;
        }
        if (color.red > color.blue) {
            intakedColor = SampleColor.RED;
            return;
        }
        intakedColor = SampleColor.BLUE;

        // if the color sensor doesn't see anything, it will incorrectly default to yellow
        // so if the color doesn't see a sample, the color will be null
        if (!forceSampleIntaked()) {
            intakedColor = null;
        }

        if (intakedColor == SampleColor.BLUE) {
            Log.d("Sample Color: ", "blue");
        } else if (intakedColor == SampleColor.RED) {
            Log.d("Sample Color: ", "red");
        } else if (intakedColor == SampleColor.YELLOW) {
            Log.d("Sample Color: ", "yellow");
        } else {
            Log.d("Sample Color: ", "none!");
        }
    }

    public double getRed() {
        NormalizedRGBA color = colorSensor.getNormalizedColors();
        return color.red;
    }

    public double getBlue() {
        NormalizedRGBA color = colorSensor.getNormalizedColors();
        return color.blue;
    }

    public double getGreen() {
        NormalizedRGBA color = colorSensor.getNormalizedColors();
        return color.green;
    }

    public void sweeperStored() {
        sweeper.turnToAngle(35.0);
    }

    public void sweeperOut() {
        sweeper.turnToAngle(72.0);
    }

    public Limelight3A getLimelight() {
        return limelight;
    }

    // Robot issues:
    // Can't detect sample -- possibly the color sensor was installed off
    // Auto-transfer doesn't work -- maybe the color sensor issue
    // Sample comes out and gets stuck -- need to code removal
    // Sample bumps out of intake during transfer -- increase intake time, but may need to tune to optimize speed
    // OUTTAKE CLAW CAN'T GRAB SAMPLE -- need to investigate this
    // Outtake claw scoring is wobbly -- lowered outtake slide height: check if that impacts auto
    // Outtake claw can drop sample while scoring -- increased wait time after claw opens
    // Need to find way to speed up all transfer
    // Outtake claw can hit sample after scoring if it fails to grab sample -- make intake stored after scoring
    // Need to be able to re-transfer in case transfer fails
    //
    // *crying noises*
}
