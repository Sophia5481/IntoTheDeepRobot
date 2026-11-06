//package org.firstinspires.ftc.teamcode.opmode;
//
//import com.acmerobotics.roadrunner.Pose2d;
//import com.acmerobotics.roadrunner.Vector2d;
//import com.arcrobotics.ftclib.command.Command;
//import com.arcrobotics.ftclib.command.CommandOpMode;
//import com.arcrobotics.ftclib.command.CommandScheduler;
//import com.arcrobotics.ftclib.command.InstantCommand;
//import com.arcrobotics.ftclib.command.ParallelCommandGroup;
//import com.arcrobotics.ftclib.command.ParallelRaceGroup;
//import com.arcrobotics.ftclib.command.WaitCommand;
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
//@Autonomous(name = "Right Auto 3+0", group = "Auto")
//public class RightAuto extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//
//    Pose2d start = new Pose2d(-8.5, 65.0, Math.toRadians(-90));
//    public static double FULLSPEED = 0.7;
//    public static double PUSH = 1.0;
//    public static double SLOW = 0.2;
//
//    @Override
//    public void initialize() {
//        telemetry.addLine("Initializing...");
//        telemetry.update();
//
//        follower = new Follower(hardwareMap);
//        follower.setStartingPose(start);
//        follower.setMaxPower(FULLSPEED);
//        robot = new Robot(true, hardwareMap);
//
//        telemetry.addLine("Ready!");
//        telemetry.update();
//
//        waitForStart();
//
//        schedule(new SequentialCommandGroup(
//                scorePreloadAndPush(),
//                intakeAndScore(3, 0, true),
//                intakeAndScore(0, -3, false),
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
//    }
//
//    private Pose2d getChangedX(Pose2d pose, double x) {
//        return new Pose2d(new Vector2d(x, pose.position.y), pose.heading);
//    }
//
//    private PathChain specimenScoringChain(Pose2d initial, double xPos) {
//        PathChain score = new PathChain(
//                new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
//                new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenScoring, xPos))))
//        );
//        score.getPath(0).setLinearHeadingInterpolation(initial.heading.toDouble(), specimenScoring.heading.toDouble(), 0.8);
//        score.getPath(1).setConstantHeadingInterpolation(specimenScoring.heading.toDouble());
//        score.setCallbacks(new PathCallback(0, () -> {
//            follower.setMaxPower(PUSH);
//        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
//            follower.setMaxPower(FULLSPEED);
//        }, PathCallback.PARAMETRIC, 1));
//        return score;
//    }
//
//    Pose2d specimenLinedUp = new Pose2d(3, 40.0, Math.toRadians(-87));
//    Pose2d specimenScoring = new Pose2d(3, 32.5, Math.toRadians(-87));
//
//    Pose2d pushControlPoint = new Pose2d(-36, 36, Math.toRadians(0));
//    Pose2d goPushControlPoint = new Pose2d(-30, 52, Math.toRadians(0));
//    Pose2d goPushControlPoint2 = new Pose2d(-35, 12, Math.toRadians(0));
//    Pose2d prePush = new Pose2d(-52, 12, Math.toRadians(0)); // Before the first push
//    Pose2d postPush = new Pose2d(-52, 56, Math.toRadians(0)); // The x for this is the final x, goes 46, 52, 58
//    private Command scorePreloadAndPush() {
//        Path goPrePush = new Path(new BezierCurve(
//                new Point(specimenScoring),
//                new Point(goPushControlPoint),
//                new Point(goPushControlPoint2),
//                new Point(getChangedX(prePush, -46))
//        ));
//        goPrePush.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), prePush.heading.toDouble(), 0.8);
//        Path push1 = new Path(new BezierLine(
//                new Point(getChangedX(prePush, -46)),
//                new Point(getChangedX(postPush, -46))
//        ));
//        push1.setConstantHeadingInterpolation(postPush.heading.toDouble());
//        Path prePush2 = new Path(new BezierCurve(
//                new Point(getChangedX(postPush, -46)),
//                new Point(pushControlPoint),
//                new Point(prePush)
//        ));
//        prePush2.setConstantHeadingInterpolation(postPush.heading.toDouble());
//        Path push2 = new Path(new BezierLine(
//                new Point(prePush),
//                new Point(postPush)
//        ));
//        push2.setConstantHeadingInterpolation(postPush.heading.toDouble());
//        return new SequentialCommandGroup(
//            new ParallelCommandGroup(
//                new ParallelRaceGroup(
//                        new WaitCommand(3000),
//                        new SequentialCommandGroup(
//                                new WaitCommand(300), // Give the slides some time to extend
//                                new FollowPath(follower, specimenScoringChain(start, 3))
//                        )
//                ),
//                new ExtendSpecimenCommand(robot.outtake, true)
//            ),
//            new ParallelCommandGroup(
//                    new ScoreSpecimenCommand(robot.outtake),
//                    new FollowPath(follower, goPrePush, true)
//            ),
//            new FollowPath(follower, push1),
//            new FollowPath(follower, prePush2),
//            new FollowPath(follower, push2)
//        );
//    }
//
//    Pose2d preIntake = new Pose2d(-36, 49, Math.toRadians(-45));
//    Pose2d intakePosition = new Pose2d(-43, 56, Math.toRadians(-45));
//    public Command intakeAndScore(double prevXPos, double xPos, boolean start) {
//        Path preIntakePath = new Path(new BezierLine(
//                new Point(getChangedX(start ? postPush : specimenScoring, prevXPos)),
//                new Point(preIntake)
//        ));
//        preIntakePath.setLinearHeadingInterpolation(start ? postPush.heading.toDouble() : specimenScoring.heading.toDouble(), intakePosition.heading.toDouble(), 0.8);
//        preIntakePath.setZeroPowerAccelerationMultiplier(10);
//        PathChain intakePath = new PathChain(new Path(new BezierLine(
//                new Point(preIntake),
//                new Point(intakePosition)
//        )));
//        intakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.heading.toDouble());
//        intakePath.setCallbacks(new PathCallback(0.8, () -> {
//            robot.outtake.specimenClawClose();
//        }, PathCallback.PARAMETRIC, 0));
//
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                    start ? new InstantCommand() : new ScoreSpecimenCommand(robot.outtake),
//                    new FollowPath(follower, preIntakePath, true)
//                ),
//                new WaitCommand(2000),
//
//                new InstantCommand(() -> follower.setMaxPower(SLOW)),
//                new FollowPath(follower, intakePath),
//                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//
//                new ParallelCommandGroup(
//                        new ParallelRaceGroup(
//                                new WaitCommand(3000),
//                                new FollowPath(follower, specimenScoringChain(intakePosition, xPos))
//                        ),
//                        new ExtendSpecimenCommand(robot.outtake, true)
//                )
//        );
//    }
//
//    Pose2d parking = new Pose2d(-58, 58, Math.toRadians(0));
//    public Command park() {
//        Path park = new Path(new BezierLine(
//                new Point(specimenScoring),
//                new Point(parking)
//        ));
//        park.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), parking.heading.toDouble(), 0.8);
//
//        return new ParallelCommandGroup(
//                new ScoreSpecimenCommand(robot.outtake),
//                new FollowPath(follower, park, true)
//        );
//    }
//}
