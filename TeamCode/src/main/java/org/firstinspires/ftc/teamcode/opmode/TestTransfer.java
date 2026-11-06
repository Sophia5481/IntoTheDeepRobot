package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.acmerobotics.roadrunner.Vector2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.CustomPIDFCoefficients;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.ManualInput;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.GiveToHP;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;
import org.firstinspires.ftc.teamcode.utils.Logger;

import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathCallback;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.follower.FollowerConstants;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

@Disabled
@Autonomous(name = "Test HP Speed", group = "Auto")
public class TestTransfer extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private ManualInput manualInput;
    private Gamepad g1;

    Pose start = new Pose(-48.0, 48.0, Math.toRadians(-90));
    Pose intake2 = new Pose(-57.0, 48.0, Math.toRadians(-90));
    public static double FULLSPEED = 0.85;
    public static double PUSH = 1.0;
    public static double SLOW = 0.6;
    Timing.Timer timer = new Timing.Timer(30000, TimeUnit.MILLISECONDS);

    boolean readGamepad = false;

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();
        g1 = new Gamepad();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(start);
        follower.updatePose();
        //follower.setMaxPower(FULLSPEED);
        follower.setMaxPower(1.0);
        follower.updatePose();
        robot = new Robot(true, hardwareMap);

        telemetry.addLine("Ready!");
        telemetry.update();

        waitForStart();
        timer.start();

        schedule(
                new SequentialCommandGroup(
                        // preparing
                    new ScoreSpecimenCommand(robot.outtake),
                    new GiveToHP(robot.outtake, true),
                    new WaitCommand(200),
                    new IntakeAuto.IntakeAutoPrep(robot.intake, false, 0.2),
                    new WaitCommand(200),
                    // giving samples to HP
                    giveToHP(start, intake2, false, true),
                    giveToHP(intake2, start, true, true),
                    giveToHP(intake2, start, false, false)
//                    new GiveToHP(robot.outtake, true),
//                    new WaitCommand(400),
//                    new ScoreBucketCommand(robot.outtake),
//                    new WaitCommand(400),
//                    new IntakeAuto(robot.intake, 500, 0.7),
//                    new WaitCommand(500),
//                    new IntakeRetractCommand(robot.intake, robot.outtake, true),
//                    new WaitCommand(500),
//                    new GiveToHP(robot.outtake, true),
//                    new WaitCommand(400),
//                    new ScoreBucketCommand(robot.outtake)
                )
        );
    }

    @Override
    public void run() {
        super.run();
        follower.drawOnDashBoard();
        follower.update();
        robot.resetCache();
        telemetry.addData("Time Left", timer.remainingTime());
        telemetry.update();
    }

    private Command giveToHP(Pose beginning, Pose next, boolean turn, boolean move) {
        Path intakePath = new Path(new BezierLine(
            new Point(beginning),
            new Point(next)
        ));
        intakePath.setConstantHeadingInterpolation(Math.toRadians(-90));
        intakePath.setPathEndTimeoutConstraint(50);
        return new SequentialCommandGroup(
                new ParallelCommandGroup(
                        new IntakeAuto(robot.intake, 500, 0.7),
                        new ScoreBucketCommand(robot.outtake)
                ),

                move ? new ParallelCommandGroup(
                        new InstantCommand(() -> {follower.setHeadingPIDF(new CustomPIDFCoefficients(2, 0, 0.2, 0)); }),
                        turn ? new InstantCommand(() -> {follower.turn(Math.toRadians(30.0), false); }) : new FollowPath(follower, intakePath, true),
                        new IntakeRetractCommand(robot.intake, robot.outtake, true)
                ) : new IntakeRetractCommand(robot.intake, robot.outtake, true),

                new ParallelCommandGroup(
                        new GiveToHP(robot.outtake, true),
                        new SequentialCommandGroup(
                                new WaitCommand(200),
                                move ? new IntakeAuto.IntakeAutoPrep(robot.intake, false, 0.2) : new InstantCommand()
                        )
                ),

                new WaitCommand(400)
        );
    }
}