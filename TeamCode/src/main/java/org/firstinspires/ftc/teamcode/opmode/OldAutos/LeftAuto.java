//package org.firstinspires.ftc.teamcode.opmode;
//
//import com.acmerobotics.roadrunner.Pose2d;
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
//@Autonomous(name = "Left Auto 1+3", group = "Auto")
//public class LeftAuto extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//
//    Pose2d start = new Pose2d(9.5, 65.0, Math.toRadians(-90));
//    public static double FULLSPEED = 0.7;
//    public static double PUSH = 0.85;
//
//    @Override
//    public void initialize() {
//        telemetry.addLine("Initializing...");
//        telemetry.update();
//
//        follower = new Follower(hardwareMap);
//        follower.setStartingPose(start);
//        follower.setMaxPower(PUSH);
//        robot = new Robot(true, hardwareMap);
//
//        telemetry.addLine("Ready!");
//        telemetry.update();
//
//        waitForStart();
//
//        schedule(new SequentialCommandGroup(
//                scorePreload(),
//                scoreFirst(),
//                intakeAndScore(secondIntake),
//                intakeAndScore(thirdIntake),
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
//    Pose2d specimenLinedUp = new Pose2d(4.0, 40.0, Math.toRadians(-87));
//    Pose2d specimenScoring = new Pose2d(4.0, 32.5, Math.toRadians(-87));
//    Pose2d driveIntakeIntermediate = new Pose2d(10, 50.0, Math.toRadians(-90));
//    private Command scorePreload() {
//        PathChain score = new PathChain(
//                new Path(new BezierLine(new Point(start), new Point(specimenLinedUp))),
//                new Path(new BezierLine(new Point(specimenLinedUp), new Point(specimenScoring)))
//        );
//        score.getPath(0).setLinearHeadingInterpolation(start.heading.toDouble(), specimenScoring.heading.toDouble(), 0.5);
//        score.getPath(1).setConstantHeadingInterpolation(specimenScoring.heading.toDouble());
//        score.setCallbacks(new PathCallback(0, () -> {
//            follower.setMaxPower(PUSH);
//        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
//            follower.setMaxPower(FULLSPEED);
//        }, PathCallback.PARAMETRIC, 1));
//
//        // Intaking
//        Path intake = new Path(new BezierCurve(
//                new Point(specimenScoring),
//                new Point(driveIntakeIntermediate),
//                new Point(firstIntake)
//        ));
//        intake.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), firstIntake.heading.toDouble(), 0.8);;
//
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                    new ParallelRaceGroup(
//                        new WaitCommand(3000),
//                        new SequentialCommandGroup(
//                            new WaitCommand(300), // Give the slides some time to extend
//                            new FollowPath(follower, score)
//                        )
//                    ),
//                    new ExtendSpecimenCommand(robot.outtake, true)
//                ),
//                new ParallelCommandGroup(
//                        new FollowPath(follower, intake, true),
//                        new ScoreSpecimenCommand(robot.outtake),
//                        new InstantCommand(robot.intake::intakeOn)
//                ),
//                new IntakePrepCommand(robot.intake),
//                new ParallelRaceGroup(
//                        new WaitCommand(1300),
//                        new IntakeCommand(robot.intake, robot.outtake, true, true)
//                ),
//                new WaitCommand(600) // Let the sample transfer
//        );
//    }
//
//    Pose2d extendScoring = new Pose2d(53, 53, Math.toRadians(-135));
//    Pose2d scoring = new Pose2d(60, 60, Math.toRadians(-135));
//    Pose2d firstIntake = new Pose2d(49.5, 47, Math.toRadians(-90));
//
//    public Command scoreFirst() {
//        Path toScoring = new Path(new BezierLine(
//                new Point(firstIntake),
//                new Point(extendScoring)
//        ));
//        toScoring.setLinearHeadingInterpolation(firstIntake.heading.toDouble(), extendScoring.heading.toDouble(), 0.8);
//
//        Path score = new Path(new BezierLine(
//                new Point(extendScoring),
//                new Point(scoring)
//        ));
//        score.setConstantHeadingInterpolation(scoring.heading.toDouble());
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                        new FollowPath(follower, toScoring, true),
//                        new SequentialCommandGroup(
//                                new WaitCommand(750),
//                                new ExtendBucketCommand(robot.outtake)
//                        )
//                ),
//                new FollowPath(follower, score, true),
//                new WaitCommand(200)
//        );
//    }
//
//    Pose2d secondIntake = new Pose2d(60.5, 46, Math.toRadians(-90));
//    Pose2d thirdIntake = new Pose2d(62.5, 45, Math.toRadians(-64));
//
//    public Command intakeAndScore(Pose2d intakePos) {
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
//                    new ScoreBucketCommand(robot.outtake, robot.intake, true),
//                    new SequentialCommandGroup(
//                        new WaitCommand(200),
//                        new FollowPath(follower, intake, true)
//                    ),
//                    new InstantCommand(robot.intake::intakeOn)
//                ),
//                new IntakePrepCommand(robot.intake),
//                new ParallelRaceGroup(
//                        new WaitCommand(1200),
//                        new IntakeCommand(robot.intake, robot.outtake, true, true)
//                ),
//                new WaitCommand(700), // Let the sample transfer
//
//                // Scoring
//                new ParallelCommandGroup(
//                        new FollowPath(follower, toScoring, true),
//                        new SequentialCommandGroup(
//                                new WaitCommand(600),
//                                new ExtendBucketCommand(robot.outtake)
//                        )
//                ),
//                new FollowPath(follower, score, true),
//                new WaitCommand(200)
//        );
//    }
//
//    Pose2d parking = new Pose2d(24.5, 10, Math.toRadians(-180));
//    Pose2d parkingControl = new Pose2d(58, 0, Math.toRadians(-180));
//    public Command park() {
//        Path park = new Path(new BezierCurve(
//                new Point(scoring),
//                new Point(parkingControl),
//                new Point(parking)
//        ));
//        park.setLinearHeadingInterpolation(scoring.heading.toDouble(), parking.heading.toDouble(), 0.8);
//
//        return new ParallelCommandGroup(
//                new SequentialCommandGroup(
//                    new ScoreBucketCommand(robot.outtake, robot.intake, false),
//                    new L1HangCommand(robot.outtake)
//                ),
//                new SequentialCommandGroup(
//                    new WaitCommand(200),
//                    new FollowPath(follower, park, true)
//                )
//        );
//    }
//}
