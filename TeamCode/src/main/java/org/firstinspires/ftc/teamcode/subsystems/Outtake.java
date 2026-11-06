package org.firstinspires.ftc.teamcode.subsystems;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

//import org.firstinspires.ftc.teamcode.commands.MotorController;
import org.firstinspires.ftc.teamcode.commands.MotorCoefficients;
import org.firstinspires.ftc.teamcode.commands.MotorController;
//import org.firstinspires.ftc.teamcode.commands.MotorMoveCommandNew;
import org.firstinspires.ftc.teamcode.commands.MotorMoveCommandNew;

@Config
public class Outtake extends SubsystemBase {
    private final MotorEx sampleMotor;
//    public static MotorCoefficients sampleCoeffs = new MotorCoefficients(0.02, 0.05, 0.0001,
//            0.0005, 0.00002, 0.01, 0.2,
//            6000, 2000,
//            0.00002, 0.0003, 0.11);

//    public static MotorCoefficients sampleCoeffs = new MotorCoefficients(0.015, 0.05, 0.0001,
//            0.0005, 0.00002, 0.01, 0.2,
//            6000, 2000,
//            0.00002, 0.0003, 0.11);

//    private MotorPeriodicController sampleController;
    public static int BUCKET_LOW = 260; // 420 * 1150 / 1620; // used to be 298
    public static int BUCKET_HIGH = 670; // 925 * 1150 / 1620; // used to be 657
    public static int BUCKET_HIGH_TELE = 615;
    public static int SAMPLE_HP = 250;

    public static int samplePreOuttake = 58;

    public static double SAMPLE_KP = 0.01;
    public static double SAMPLE_KD = 0.001;
    public static double SAMPLE_KP_DOWN = 0.003;
    public static double SAMPLE_KF = 0.1;
    public static double SAMPLE_KF_DOWN = 0.05;
    public static double SAMPLE_TOLERANCE = 50;

    public MotorController sampleController;

    private final MotorEx specimenMotor;
//    public static MotorCoefficients specimenCoeffs = new MotorCoefficients(0.015, 0.05, 0.0001,
//            0.0005, 0.00002, 0.01, 0.2,
//            6000, 2000,
//            0.00002, 0.0003, 0.11);

//    private MotorPeriodicController specimenController;
    public MotorController specimenController;

    public static double SPECIMEN_KP = 0.004;
    public static double SPECIMEN_KP_DOWN = 0.0015;
    public static double SPECIMEN_KF = 0.05;
    public static double SPECIMEN_KF_DOWN = 0.1;
    public static double SPECIMEN_TOLERANCE = 50;

    public static int SPECIMEN_LOW = 200;
    public static int SPECIMEN_HIGH = 650;
    public static int SPECIMEN_L1 = 400;

    private ServoEx specimenClaw;
    public static double SPECIMEN_CLAW_MINANGLE = -210;
    public static double SPECIMEN_CLAW_MAXANGLE = 70.0;

    private ServoEx specimenArm;
    public static double SPECIMEN_ARM_MINANGLE = 0.0;
    public static double SPECIMEN_ARM_MAXANGLE = 180.0;

    private ServoEx guide;
    public static double GUIDE_MINANGLE = -100;
    public static double GUIDE_MAXANGLE = 170.0;

    private ServoEx sampleArm;
    public static double SAMPLE_ARM_MINANGLE = 0.0;
    public static double SAMPLE_ARM_MAXANGLE = 180.0;

    private ServoEx sampleClaw;
    public static double SAMPLE_CLAW_MINANGLE = 0.0;
    public static double SAMPLE_CLAW_MAXANGLE = 180.0;

    public Outtake(final HardwareMap hMap) {
        sampleMotor = new MotorEx(hMap, "bucket");
        sampleMotor.setPositionCoefficient(0);
        sampleMotor.setInverted(true);
//        sampleController = new MotorPeriodicController(sampleCoeffs, sampleMotor);
        sampleController = new MotorController(sampleMotor, SAMPLE_KP, SAMPLE_KD, SAMPLE_KF, SAMPLE_TOLERANCE);

        specimenMotor = new MotorEx(hMap, "specimen");
        specimenMotor.setPositionCoefficient(0);
//        specimenController = new MotorPeriodicController(specimenCoeffs, specimenMotor);
        specimenController = new MotorController(specimenMotor, SPECIMEN_KP, SPECIMEN_KF, SPECIMEN_TOLERANCE);

        specimenClaw = new SimpleServo(hMap, "specimenClaw", SPECIMEN_CLAW_MINANGLE, SPECIMEN_CLAW_MAXANGLE);
        specimenClaw.setInverted(true);

        sampleClaw = new SimpleServo(hMap, "sampleClaw", SAMPLE_CLAW_MINANGLE, SAMPLE_CLAW_MAXANGLE);
        sampleClaw.setInverted(true);

        specimenArm = new SimpleServo(hMap, "specimenArm", SPECIMEN_ARM_MINANGLE, SPECIMEN_ARM_MAXANGLE);

        guide = new SimpleServo(hMap, "guide", GUIDE_MINANGLE, GUIDE_MAXANGLE);

        sampleArm = new SimpleServo(hMap, "bucketArm", SAMPLE_ARM_MINANGLE, SAMPLE_ARM_MAXANGLE);
    }

    public void init(boolean auto) {
        if (auto) {
            specimenArmVertical();
            specimenClawClose();
            sampleArmGrab();
        } else {
            new InstantCommand(this::specimenLow);
            specimenClawOpen();
            specimenArmIntake();
            sampleArmTransfer();
        }

        guideStored();
        sampleClawOpen();
    }

    // only use in teleop
    public SequentialCommandGroup safeInit() {
        return new SequentialCommandGroup(
                new InstantCommand(() -> {
                    new InstantCommand(this::specimenLow);
                    specimenClawOpen();
                    specimenArmIntake();
                    guideStored();
                    sampleClawOpen();
                    sampleArmTransfer();
                }),
                new WaitCommand(500),
                new InstantCommand(this::specimenDown)
        );
    }

    @Override
    public void periodic() {
        sampleController.periodic();
        specimenController.periodic();
    }

    public void home() throws InterruptedException {
        sampleController.home();
        specimenController.home();
    }

    public Command bucketDown() {
//        return new MotorMoveCommand(this, sampleMotor, 0, sampleCoeffs);
        return new MotorMoveCommandNew(sampleController, 0);
    }

    public Command bucketLow() {
//        return new MotorMoveCommand(this, sampleMotor, BUCKET_LOW, sampleCoeffs);
        return new MotorMoveCommandNew(sampleController, BUCKET_LOW);
    }

    public Command bucketHigh() {
//        return new MotorMoveCommand(this, sampleMotor, BUCKET_HIGH, sampleCoeffs);
        return new MotorMoveCommandNew(sampleController, BUCKET_HIGH);
    }
    public Command bucketHighTele() {
//        return new MotorMoveCommand(this, sampleMotor, BUCKET_HIGH, sampleCoeffs);
        return new MotorMoveCommandNew(sampleController, BUCKET_HIGH_TELE);
    }

    // sample tolerance is 50 so need to add 70 to increase slide height by 20
    public Command increaseSlideLayer() {
        BUCKET_HIGH += 70;
        return new MotorMoveCommandNew(sampleController, BUCKET_HIGH);
    }

    public Command decreaseSlideLayer() {
        BUCKET_HIGH -= 70;
        return new MotorMoveCommandNew(sampleController, BUCKET_HIGH);
    }

    public Command specimenDown() {
//        return new MotorMoveCommand(this, specimenMotor, 0, specimenCoeffs);
        return new MotorMoveCommandNew(specimenController, 0);
    }

    public Command specimenLow() {
//        return new MotorMoveCommand(this, specimenMotor, SPECIMEN_LOW, specimenCoeffs);
        return new MotorMoveCommandNew(specimenController, SPECIMEN_LOW);
    }

    public Command sampleHP() {
//        return new MotorMoveCommand(this, sampleMotor, SAMPLE_HP, sampleCoeffs);
        return new MotorMoveCommandNew(sampleController, SAMPLE_HP);
    }

    public Command specimenHigh() {
//        return new MotorMoveCommand(this, specimenMotor, SPECIMEN_HIGH, specimenCoeffs);
        return new MotorMoveCommandNew(specimenController, SPECIMEN_HIGH);
    }

    public Command specimenL1() {
//        return new MotorMoveCommand(this, specimenMotor, SPECIMEN_L1, specimenCoeffs);
        return new MotorMoveCommandNew(specimenController, SPECIMEN_L1);
    }

    public void specimenClawOpen() {
        specimenClaw.turnToAngle(-55.0);
    }

    public void specimenHalfOpen() {
        specimenClaw.turnToAngle(-80.0);
    }

    public void specimenClawClose() {
        specimenClaw.turnToAngle(-95.0);
    }

    public void specimenArmIntake() {
        specimenArm.turnToAngle(33.0);
    } //33.0

    public void specimenArmOuttake() {
        specimenArm.turnToAngle(150.0);
    }

    public void specimenArmVertical() {
        specimenArm.turnToAngle(80.0);
    }

    public void guideStored() {guide.turnToAngle(90.0); }

    public void guideOut() {guide.turnToAngle(-4.0); }

    public void sampleClawClose() {
        sampleClaw.turnToAngle(57.5);
    }

    public void sampleClawWide() {
        sampleClaw.turnToAngle(90);
    }

    public void sampleClawOpen() {
        sampleClaw.turnToAngle(80);
    }

    // arm position while waiting for intake to come back
    public void sampleArmTransfer() {
        sampleArm.turnToAngle(124);
    }

    // arm needs to rotate slightly down to grab the sample more reliably.
    // however it has to wait until the intake comes back before turning down
    // because otherwise it would hit the intake
    public void sampleArmGrab() {
        sampleArm.turnToAngle(130);
    }

    public void sampleArmOuttake() {
        sampleArm.turnToAngle(37);
    }

    public void sampleArmOuttakeTele() {
        sampleArm.turnToAngle(42);
    }

    public void sampleArmPreOuttake() {
        sampleArm.turnToAngle(samplePreOuttake);
    }

    public void sampleArmHP() {
        sampleArm.turnToAngle(180); }

    public boolean isClawOpen() {
        return Math.abs(specimenClaw.getAngle() - (-55)) < 0.1;
    }

}
