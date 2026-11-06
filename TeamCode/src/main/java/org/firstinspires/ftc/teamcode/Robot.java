package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.commands.Memory;
import org.firstinspires.ftc.teamcode.commands.VoltageSensing;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.LED;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;
//import org.firstinspires.ftc.teamcode.subsystems.BlobVision;
import org.firstinspires.ftc.vision.opencv.ColorRange;

import java.util.List;

public class Robot extends com.arcrobotics.ftclib.command.Robot {
    // Not sure if this is intended to be private.
    // I made this public so I can access it with "robot.intake"
    public Intake intake;
    public Outtake outtake;
//    public BlobVision vision;
//    public ManualInput manualInput;
    public Telemetry telem;
    public org.firstinspires.ftc.teamcode.subsystems.LED led;

    public static boolean RED_ALLIANCE = true;

    List<LynxModule> hubs;

    public Robot(boolean auto, final HardwareMap hMap) {
        intake = new Intake(hMap);
        outtake = new Outtake(hMap);
        led = new LED(hMap);
//        vision = new BlobVision(hMap, ColorRange.YELLOW);
        register(intake, outtake);
        VoltageSensing.initialize(hMap);

        hubs = hMap.getAll(LynxModule.class);
        for (LynxModule hub : hubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        if (auto) {
            initAuto(hMap);
        } else {
            initTele(hMap);
        }
    }

    public void initTele(final HardwareMap hMap) {
        if (!Memory.ENDED_AUTO)  {
            outtake.init(false);
            intake.init(false);
            home();
        }
    }

    public void startTele() {
        if (Memory.ENDED_AUTO) {
            outtake.init(false);
            intake.init(false);
        }
        Memory.ENDED_AUTO = false;
    }

    public void initAuto(final HardwareMap hMap) {
        outtake.init(true);
        intake.init(true);

        home();
        Memory.ENDED_AUTO = true;
    }

    public void resetCache() {
        for (LynxModule hub : hubs) {
            hub.clearBulkCache();
        }
    }

    public void home() {
        try {
            intake.home();
            outtake.home();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void allianceConfig(Telemetry telemetry, Gamepad gamepad1) {
        telemetry.addData("Alliance", RED_ALLIANCE ? "Red" : "Blue");
        telemetry.addLine("Use driver 1's " + (RED_ALLIANCE ? "Right" : "Left") + " bumper to switch alliances");
        telemetry.update();
        if (gamepad1.left_bumper) {
            RED_ALLIANCE = true;
        } else if (gamepad1.right_bumper) {
            RED_ALLIANCE = false;
        }

        if (RED_ALLIANCE) {
            led.setPattern(RevBlinkinLedDriver.BlinkinPattern.RED);
        } else {
            led.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE);
        }
    }
}
