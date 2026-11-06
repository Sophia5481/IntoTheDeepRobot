package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.arcrobotics.ftclib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeBeginCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.GiveToHP;
import org.firstinspires.ftc.teamcode.commands.outtake.ResetOuttake;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

import com.pedropathing.follower.Follower;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.follower.FollowerConstants;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import java.util.concurrent.TimeUnit;

@TeleOp(name = "Intake Detection Test", group = "Test")
public class IntakeDetectionTest extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private Intake intake;
    private GamepadEx g1, g2;
    private double intakeExtension;
    public static double isPushingSamples;
    private Timing.Timer timer;

    @Override
    public void initialize() {
        telemetry.addLine("Ready!");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        intake = new Intake(hardwareMap);
        FollowerConstants.useBrakeModeInTeleOp = true;
        follower.startTeleopDrive();

        timer = new Timing.Timer(120, TimeUnit.SECONDS);

        g1 = new GamepadEx(gamepad1);
        g2 = new GamepadEx(gamepad2);

        intakeExtension = 0;

        telemetry.addLine("Ready!");
        telemetry.update();

        waitForStart();
        timer.start();
    }

    long lastUpdate = System.currentTimeMillis();
    @Override
    public void run() {
        super.run();
        telemetry.addData("Loop Rate (Hz)", 1000.0/(double)(System.currentTimeMillis() - lastUpdate));
        telemetry.addData("Time Left (s)", timer.remainingTime());
        telemetry.addData("Intake Need Reverse? ", intake.needReverse());
        telemetry.addData("Force sample intaked? ", intake.forceSampleIntaked());
        telemetry.addData("Sample Intaked: ", intake.sampleIntaked());
        telemetry.addData("Intake jammed: ", intake.intakeJammed());
        intake.updateSampleColor();
        lastUpdate = System.currentTimeMillis();
        telemetry.update();
    }
}
