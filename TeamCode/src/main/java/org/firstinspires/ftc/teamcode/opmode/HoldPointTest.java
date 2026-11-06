//package org.firstinspires.ftc.teamcode.opmode;
//
//import android.util.Log;
//
//import com.acmerobotics.roadrunner.Pose2d;
//import com.arcrobotics.ftclib.command.Command;
//import com.arcrobotics.ftclib.command.CommandOpMode;
//import com.arcrobotics.ftclib.command.InstantCommand;
//import com.arcrobotics.ftclib.command.ParallelCommandGroup;
//import com.arcrobotics.ftclib.command.SequentialCommandGroup;
//import com.arcrobotics.ftclib.command.WaitCommand;
//import com.arcrobotics.ftclib.gamepad.GamepadEx;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//
//import org.firstinspires.ftc.teamcode.Robot;
//import org.firstinspires.ftc.teamcode.commands.FollowPath;
//import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
//import org.firstinspires.ftc.teamcode.commands.auto.IntakeSubAuto;
//import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
//import org.firstinspires.ftc.teamcode.pedroPathing.follower.Follower;
//import org.firstinspires.ftc.teamcode.pedroPathing.localization.Pose;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierCurve;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierLine;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierPoint;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Path;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;
//
//@Autonomous(name = "Hold Point Test", group = "Auto")
//public class HoldPointTest extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//    private GamepadEx g1;
//
//    Point scoring = new Point(61.0 + 72.0, 56.0 + 72.0, Point.CARTESIAN);
//    double scoringHeading = Math.toRadians(-120.0);
//
//    @Override
//    public void initialize() {
//        telemetry.addLine("Initializing...");
//        telemetry.update();
//
//        follower = new Follower(hardwareMap);
//        follower.setStartingPose(new Pose2d(scoring.getX(), scoring.getY(), scoringHeading));
//        follower.setMaxPower(1.0);
//        robot = new Robot(true, hardwareMap);
//
//        waitForStart();
//
//        schedule(new SequentialCommandGroup(
//            intakeAndScore(intake1, true)
//        ));
//    }
//
//    @Override
//    public void run() {
//        super.run();
//        follower.drawOnDashBoard();
//        follower.update();
//        robot.resetCache();
//    }
//
//    Pose intake1 = new Pose(61.0 + 72.0, 56.0 + 72.0, Math.toRadians(-150));
//
//    private Command intakeAndScore(Pose position, boolean fast) {
//        return new SequentialCommandGroup(
//                // Score the sample & go to intake at once
//                        new InstantCommand(() -> {
//                            follower.holdPoint(new BezierPoint(new Point(scoring.getX(), scoring.getY(), Point.CARTESIAN)),
//                                    position.getHeading());
//                        }),
//                        new InstantCommand(() -> {
//                            follower.holdPoint(new BezierPoint(new Point(scoring.getX(), scoring.getY(), Point.CARTESIAN)),
//                                    Math.toRadians(-90));
//                        }),
//                        new InstantCommand(() -> {
//                            follower.holdPoint(new BezierPoint(new Point(scoring.getX(), scoring.getY(), Point.CARTESIAN)),
//                                    Math.toRadians(-75));
//                        })
//        );
//    }
//}
