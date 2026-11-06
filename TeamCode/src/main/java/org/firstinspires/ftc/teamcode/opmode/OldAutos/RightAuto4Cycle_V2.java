//package org.firstinspires.ftc.teamcode.opmode;
//
//import com.acmerobotics.roadrunner.Pose2d;
//import com.acmerobotics.roadrunner.Vector2d;
//import com.arcrobotics.ftclib.command.Command;
//import com.arcrobotics.ftclib.command.CommandOpMode;
//import com.arcrobotics.ftclib.command.ConditionalCommand;
//import com.arcrobotics.ftclib.command.InstantCommand;
//import com.arcrobotics.ftclib.command.ParallelCommandGroup;
//import com.arcrobotics.ftclib.command.ParallelRaceGroup;
//import com.arcrobotics.ftclib.command.WaitCommand;
//import com.arcrobotics.ftclib.util.Timing;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//
//import org.firstinspires.ftc.teamcode.Robot;
//import org.firstinspires.ftc.teamcode.commands.FollowPath;
//import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
//import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
//import org.firstinspires.ftc.teamcode.pedroPathing.follower.Follower;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierCurve;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierLine;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Path;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathCallback;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathChain;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;
//
//import java.util.concurrent.TimeUnit;
//
//@Autonomous(name = "Right Auto 3 Cycles 5+0", group = "Auto")
//public class RightAuto4Cycle_V2 extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//
//    Pose2d start = new Pose2d(-8.5, 65.0, Math.toRadians(-90));
//    public static double FULLSPEED = 0.7;
//    public static double PUSH = 1.0;
//    public static double SLOW = 0.4;
//    Timing.Timer timer = new Timing.Timer(30000, TimeUnit.MILLISECONDS);
//
//    @Override
//    public void initialize() {
//        telemetry.addLine("Initializing...");
//        telemetry.update();
//
//        follower = new Follower(hardwareMap);
//        follower.setStartingPose(start);
//        //follower.setMaxPower(FULLSPEED);
//        follower.setMaxPower(PUSH);
//        robot = new Robot(true, hardwareMap);
//
//
//        telemetry.addLine("Ready!");
//        telemetry.update();
//
//        waitForStart();
//        timer.start();
//
//        schedule(new SequentialCommandGroup(
//                scorePreloadAndPush(),
//                intakeAndScore(2, 0, true, true),
//                intakeAndScore(0, -3, false, false),
//                intakeAndScore(-3, 0, false, false),
//                new ConditionalCommand(intakeAndScore(0, 1, false, true), new InstantCommand(), () -> timer.remainingTime() > 4000),
//                park()
//        ));
//    }
//
//    @Override
//    public void run() {
//        super.run();
//        follower.drawOnDashBoard();
//        follower.update();
//        robot.resetCache();
//        telemetry.addData("Time Left", timer.remainingTime());
//        telemetry.update();
//    }
//
//    private Pose2d getChangedX(Pose2d pose, double x) {
//        return new Pose2d(new Vector2d(x, pose.position.y), pose.heading);
//    }
//
//    private PathChain specimenScoringChain(Pose2d initial, double xPos, boolean start) {
//        // Creates the path for the robot to score the specimen
//        PathChain score;
//        if (start) {
//            score = new PathChain(
//                    new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
//                    new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenFirstScoring, xPos))))
//            );
//        } else {
//            score = new PathChain(
//                    new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
//                    new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenScoring, xPos))))
//            );
//        }
////        PathChain score = new PathChain(
////                new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
////                new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenScoring, xPos))))
////        );
//        score.getPath(0).setLinearHeadingInterpolation(initial.heading.toDouble(), specimenScoring.heading.toDouble(), 0.8);
//        score.getPath(1).setConstantHeadingInterpolation(specimenScoring.heading.toDouble());
//        // Reduce the time waiting for the follower to complete its path
//        score.getPath(1).setPathEndTValueConstraint(0.95);
//        score.getPath(1).setPathEndTimeoutConstraint(20);
//        score.setCallbacks(new PathCallback(0, () -> {
//            follower.setMaxPower(PUSH);
//        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
//            follower.setMaxPower(FULLSPEED);
//        }, PathCallback.PARAMETRIC, 1));
//        return score;
//    }
//
//    Pose2d specimenLinedUp = new Pose2d(3, 40.0, Math.toRadians(-87));
//    // changed x position from 3 to 2
//    Pose2d specimenScoring = new Pose2d(2, 33.35, Math.toRadians(-87));
//    Pose2d specimenFirstScoring = new Pose2d(-6, 31.5, Math.toRadians(-87));
//
//    Pose2d driveControlPoint = new Pose2d(0, 48, Math.toRadians(-87));
//    Pose2d driveControlPoint2 = new Pose2d(-24, 48, Math.toRadians(-87));
//    Pose2d push1 = new Pose2d(-27.5, 39, Math.toRadians(-135));
//    Pose2d postPush1 = new Pose2d(-29.5, 53, Math.toRadians(-190));
//
//    Pose2d push2 = new Pose2d(-34, 36, Math.toRadians(-150));
//    Pose2d postPush2 = new Pose2d(-38, 57, Math.toRadians(-190));
//
//    Pose2d push3 = new Pose2d(-43.5, 36, Math.toRadians(-150));
//    Pose2d postPush3 = new Pose2d(-44.5, 57, Math.toRadians(-190));
//    private Command scorePreloadAndPush() {
//        // Creates the path for the robot to push the samples
//        Path goPrePush = new Path(new BezierCurve(
//                new Point(specimenFirstScoring),
//                new Point(driveControlPoint),
//                new Point(driveControlPoint2),
//                new Point(push1)
//        ));
//        goPrePush.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), push1.heading.toDouble(), 0.8);
//        Path firstPush = new Path(new BezierLine(
//                new Point(push1),
//                new Point(postPush1)
//        ));
//        firstPush.setLinearHeadingInterpolation(push1.heading.toDouble(), postPush1.heading.toDouble());
//
//        Path prePush2 = new Path(new BezierCurve(
//                new Point(postPush1),
//                new Point(push2)
//        ));
//        prePush2.setLinearHeadingInterpolation(postPush1.heading.toDouble(), push2.heading.toDouble());
//        Path secondPush = new Path(new BezierLine(
//                new Point(push2),
//                new Point(postPush2)
//        ));
//        secondPush.setLinearHeadingInterpolation(push2.heading.toDouble(), postPush2.heading.toDouble());
//
//        Path prePush3 = new Path(new BezierCurve(
//                new Point(postPush2),
//                new Point(push3)
//        ));
//        prePush3.setLinearHeadingInterpolation(postPush2.heading.toDouble(), push3.heading.toDouble());
//        Path thirdPush = new Path(new BezierLine(
//                new Point(push3),
//                new Point(postPush3)
//        ));
//        thirdPush.setLinearHeadingInterpolation(push3.heading.toDouble(), postPush3.heading.toDouble());
//        PathChain pushPath = new PathChain(goPrePush, firstPush, prePush2, secondPush, prePush3, thirdPush);
//        pushPath.setCallbacks(new PathCallback(0.7, () -> {
//                    robot.intake.slideOut();
//                    robot.intake.wristIntake();
//                }, PathCallback.PARAMETRIC, 0),
//
//            new PathCallback(0, () -> {
//                robot.intake.wristTransfer();
//            }, PathCallback.PARAMETRIC, 2),
//
//            new PathCallback(0.8, () -> {
//                robot.intake.wristIntake();
//            }, PathCallback.PARAMETRIC, 2),
//
//                new PathCallback(0, () -> {
//                    robot.intake.wristTransfer();
//                }, PathCallback.PARAMETRIC, 4),
//
//                new PathCallback(0.8, () -> {
//                    robot.intake.wristIntake();
//                }, PathCallback.PARAMETRIC, 4));
//
//        // Returns the path to score the specimen and push samples
//        return new SequentialCommandGroup(
//            new ParallelCommandGroup(
//                new ParallelRaceGroup(
//                        new WaitCommand(1500),
//                        new SequentialCommandGroup(
//                                new WaitCommand(300), // Give the slides some time to extend
//                                // Drives to score the specimen with x Position 2
//                                new FollowPath(follower, specimenScoringChain(start, 2, true))
//                        )
//                ),
//                // Raises the slides to prepare for scoring
//                new ExtendSpecimenCommand(robot.outtake, true)
//            ),
//            new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//            new ParallelCommandGroup(
//                    new ScoreSpecimenCommand(robot.outtake),
//                    // Push the samples to the Human Player
//                    new FollowPath(follower, pushPath, true)
//            ),
//                new InstantCommand(() ->
//                    robot.intake.wristTransfer()
//                ),
//                new InstantCommand(() -> robot.intake.slideIn())
//        );
//    }
//
//    // pre-intake position after bot just finished pushing
//    Pose2d preIntake = new Pose2d(-36, 54, Math.toRadians(-90));
//    // pre-intkae position for almost all cycles
//    Pose2d smoothPreIntake = new Pose2d(-36, 51, Math.toRadians(-90));
//    Pose2d intakeControlPoint = new Pose2d(-12, 48, Math.toRadians(-90));
//    // intake position for almost all cycles
//    Pose2d intakePosition = new Pose2d(-36, 60, Math.toRadians(-90));
//    // intake position for the second cycle, after bot just finished pushing
//    Pose2d firstIntakePosition = new Pose2d(-36, 61, Math.toRadians(-90));
//    public Command intakeAndScore(double prevXPos, double xPos, boolean start, boolean startIntake) {
//        Path preIntakePath;
//        if (start) {
//            preIntakePath = new Path(new BezierCurve(
//                    new Point(getChangedX(start ? postPush3 : specimenScoring, prevXPos)),
//                    new Point(start ? preIntake : smoothPreIntake)
//            ));
//        } else {
//            preIntakePath = new Path(new BezierCurve(
//                    new Point(getChangedX(start ? postPush3 : specimenScoring, prevXPos)),
//                    new Point(intakeControlPoint),
//                    new Point(start ? preIntake : smoothPreIntake)
//            ));
//            preIntakePath.setPathEndTValueConstraint(0.8);
//        }
//
//        preIntakePath.setLinearHeadingInterpolation(start ? postPush3.heading.toDouble() : specimenScoring.heading.toDouble(), intakePosition.heading.toDouble(), 0.8);
//        preIntakePath.setZeroPowerAccelerationMultiplier(4);
//        Path intakePath = new Path(new BezierLine(
//                new Point(start ? preIntake : smoothPreIntake),
//                startIntake ? new Point(firstIntakePosition) : new Point(intakePosition)
//        ));
//        intakePath.setConstantHeadingInterpolation(intakePosition.heading.toDouble());
//
//        PathChain smoothIntake = new PathChain();
//        if (!start) {
//            smoothIntake = new PathChain(preIntakePath, intakePath);
//            smoothIntake.getPath(0).setConstantHeadingInterpolation(intakePosition.heading.toDouble());
//            smoothIntake.setCallbacks(
//                    new PathCallback(0.0, () -> {
//                follower.setMaxPower(SLOW);
//                }, PathCallback.PARAMETRIC, 1),
//                new PathCallback(0.8, () -> {
//                    robot.outtake.specimenClawClose();
//                }, PathCallback.PARAMETRIC, 1));
//        }
//        // path for the first cycle after pushing
//        if (start == true) {
//            return new SequentialCommandGroup(
//                    new ParallelCommandGroup(
//                            start ? new WaitCommand(1000) : new ScoreSpecimenCommand(robot.outtake),
//                            new FollowPath(follower, preIntakePath, true)
//                    ),
//                    new InstantCommand(() -> follower.setMaxPower(SLOW - 0.2)),
//                    new FollowPath(follower, intakePath, true),
//                    new WaitCommand(100),
//                    new InstantCommand(() -> {
//                        robot.outtake.specimenClawClose();
//                    }),
//                    new WaitCommand(300),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH)),
//
//                    new ParallelCommandGroup(
//                            new ParallelRaceGroup(
//                                    new WaitCommand(2500),
////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
//                            ),
//                            new ExtendSpecimenCommand(robot.outtake, true)
//                    )
//            );
//        } else {
//            // path on all other cycles
//            return new SequentialCommandGroup(
//                    new ParallelCommandGroup(
//                        new ScoreSpecimenCommand(robot.outtake),
//                        new FollowPath(follower, smoothIntake)
//                    ),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH)),
//
//                    new ParallelCommandGroup(
//                            new ParallelRaceGroup(
//                                    new WaitCommand(2500),
////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
//                            ),
//                            new ExtendSpecimenCommand(robot.outtake, true)
//                    )
//            );
//        }
//
//    }
//
//    Pose2d fastIntakePosition = new Pose2d(-36, 60.5, Math.toRadians(0));
////    Pose2d fastIntakeControl = new Pose2d(0, 60.5, Math.toRadians(0));
////    public Command fastIntakeAndScore(double prevXPos, double xPos) {
////        PathChain intakePath = new PathChain(new Path(new BezierLine(
////                new Point(getChangedX(specimenScoring, prevXPos)),
////                new Point(fastIntakePosition)
////        )));
////        intakePath.getPath(0).setLinearHeadingInterpolation(specimenScoring.heading.toDouble(),
////                intakePosition.heading.toDouble());
////        intakePath.setCallbacks(new PathCallback(0.9, () -> {
////            robot.outtake.specimenClawClose();
////        }, PathCallback.PARAMETRIC, 0));
////
////        return new SequentialCommandGroup(
////                new ParallelCommandGroup(
////                        new ScoreSpecimenCommand(robot.outtake),
////                        new FollowPath(follower, intakePath, true)
////                ),
////                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
////
////                new ParallelCommandGroup(
////                        new ParallelRaceGroup(
////                                new WaitCommand(2500),
//////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
////                                new FollowPath(follower, specimenScoringChain(fastIntakePosition, xPos))
////                        ),
////                        new ExtendSpecimenCommand(robot.outtake, true)
////                )
////        );
////    }
//
//    Pose2d parking = new Pose2d(-48, 56, Math.toRadians(0));
//    public Command park() {
//        Path park = new Path(new BezierLine(
//                new Point(specimenScoring),
//                new Point(parking)
//        ));
//        park.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), parking.heading.toDouble(), 0.8);
//
//        return new ParallelCommandGroup(
//                new InstantCommand(() -> follower.setMaxPower(1.0)),
//                new ScoreSpecimenCommand(robot.outtake),
//                new FollowPath(follower, park, true)
//        );
//    }
//}
