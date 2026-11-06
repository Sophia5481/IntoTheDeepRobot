//package org.firstinspires.ftc.teamcode.opmode;
//
//import com.acmerobotics.roadrunner.Pose2d;
//import com.arcrobotics.ftclib.command.Command;
//import com.arcrobotics.ftclib.command.CommandOpMode;
//import com.arcrobotics.ftclib.command.InstantCommand;
//import com.arcrobotics.ftclib.command.ParallelCommandGroup;
//import com.arcrobotics.ftclib.command.ParallelRaceGroup;
//import com.arcrobotics.ftclib.command.WaitCommand;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//
//import org.firstinspires.ftc.teamcode.Robot;
//import org.firstinspires.ftc.teamcode.commands.FollowPath;
//import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
//import org.firstinspires.ftc.teamcode.commands.intake.IntakeCommand;
//import org.firstinspires.ftc.teamcode.commands.intake.IntakePrepCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.L1HangCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
//import org.firstinspires.ftc.teamcode.pedroPathing.follower.Follower;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierCurve;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierLine;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Path;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathCallback;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathChain;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;
//
//@Autonomous(name = "Left Auto 0+5", group = "Auto")
//public class LeftAuto4Cycle extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//
//    Pose2d start = new Pose2d(33.5, 65.0, Math.toRadians(-90));
//    public static double FULLSPEED = 0.7;
//    public static double PUSH = 1.0;
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
//                scorePreload(),
//                intakeAndScore(firstIntake, false),
//                intakeAndScore(secondIntake, false),
//                intakeAndScore(thirdIntake, false),
//                intakeAndScore(fourthIntake, true),
//                new ScoreBucketCommand(robot.outtake, robot.intake, true)
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
//    private Command scorePreload() {
//        Path toScoring = new Path(new BezierLine(
//                new Point(start),
//                new Point(extendScoring)
//        ));
//        toScoring.setLinearHeadingInterpolation(start.heading.toDouble(), extendScoring.heading.toDouble(), 0.8);
//
//        Path score = new Path(new BezierLine(
//                new Point(extendScoring),
//                new Point(scoring)
//        ));
//        score.setConstantHeadingInterpolation(scoring.heading.toDouble());
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                        new FollowPath(follower, toScoring, true),
//                        new ExtendBucketCommand(robot.outtake)
//                ),
//                new FollowPath(follower, score, true),
//                new WaitCommand(200)
//        );
//    }
//
//    Pose2d extendScoring = new Pose2d(53, 53, Math.toRadians(-135));
//    Pose2d scoring = new Pose2d(60, 60, Math.toRadians(-135));
//
//    Pose2d firstIntake = new Pose2d(51, 47, Math.toRadians(-90));
//    Pose2d secondIntake = new Pose2d(60.5, 46, Math.toRadians(-90));
//    Pose2d thirdIntake = new Pose2d(62.5, 45, Math.toRadians(-64));
//    Pose2d fourthIntake = new Pose2d(17, 61, Math.toRadians(-180));
//
//    public Command intakeAndScore(Pose2d intakePos, boolean fast) {
//        Path intake = new Path(new BezierLine(
//                new Point(scoring),
//                new Point(intakePos)
//        ));
//        intake.setLinearHeadingInterpolation(scoring.heading.toDouble(), intakePos.heading.toDouble(), 0.8);
//
//        Path toScoring = new Path(new BezierLine(
//                new Point(intakePos),
//                new Point(extendScoring)
//        ));
//        toScoring.setLinearHeadingInterpolation(intakePos.heading.toDouble(), extendScoring.heading.toDouble(), 0.8);
//
//        Path score = new Path(new BezierLine(
//                new Point(extendScoring),
//                new Point(scoring)
//        ));
//        score.setConstantHeadingInterpolation(scoring.heading.toDouble());
//
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                    new SequentialCommandGroup(
//                            new ScoreBucketCommand(robot.outtake, robot.intake, true),
//                            fast ? new InstantCommand(() -> {
//                                robot.intake.intakeOn();
//                                robot.intake.slideOut();
//                                robot.intake.wristIntake();
//                            }) : new InstantCommand()
//                    ),
//                    new SequentialCommandGroup(
//                        new WaitCommand(200),
//                        new FollowPath(follower, intake, true)
//                    ),
//                    new InstantCommand(robot.intake::intakeOn)
//                ),
//                fast ? new InstantCommand() : new IntakePrepCommand(robot.intake),
//                new ParallelRaceGroup(
//                        new WaitCommand(1600),
//                        new IntakeCommand(robot.intake, robot.outtake, true, true)
//                ),
//                new WaitCommand(400), // Let the sample transfer
//
//                // Scoring
//                new ParallelCommandGroup(
//                        new SequentialCommandGroup(
//                                new FollowPath(follower, toScoring, true),
//                                new WaitCommand(fast ? 300 : 700),
//                                new FollowPath(follower, score, true)
//                        ),
//                        new SequentialCommandGroup(
//                                new WaitCommand(600),
//                                new ExtendBucketCommand(robot.outtake)
//                        )
//                ),
//                new WaitCommand(200)
//        );
//    }
//}
