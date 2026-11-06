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
//import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
//import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
//import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
//import org.firstinspires.ftc.teamcode.pedroPathing.follower.Follower;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierCurve;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierLine;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Path;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathCallback;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.PathChain;
//import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;
//import org.firstinspires.ftc.teamcode.pedroPathing.tuning.FollowerConstants;
//import org.firstinspires.ftc.teamcode.pedroPathing.util.CustomPIDFCoefficients;
//
//import java.util.concurrent.TimeUnit;
//
//@Autonomous(name = "Right Auto 6+0 Fast Intake", group = "Auto")
//public class RightAuto6_0_Fast_Intake extends CommandOpMode {
//    private Follower follower;
//    private Robot robot;
//
//    Pose2d start = new Pose2d(-8.5, 65.0, Math.toRadians(-90));
//    Pose2d subIntake = new Pose2d(2, 40, Math.toRadians(-90));
//    public static double FULLSPEED = 0.85;
//    public static double PUSH = 1.0;
//    public static double SLOW = 0.6;
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
//                scorePreloadAndFirstPush(),
//
//                new InstantCommand(() -> {
//                    FollowerConstants.headingPIDFCoefficients = new CustomPIDFCoefficients(
//                            5,
//                            0,
//                            0.2,
//                            0
//                    );
//                }),
//
//                giveToHP(push1, subGive, postPush1, false),
//                giveToHP(push2, postPush1, postPush2, false),
//                giveToHP(push3, postPush2, postPush3, true),
//
//                new InstantCommand(() -> {
//                    FollowerConstants.headingPIDFCoefficients = new CustomPIDFCoefficients(
//                            1,
//                            0,
//                            0.2,
//                            0
//                    );
//                }),
//
//                intakeAndScore(0, -1, true, true),
//                intakeAndScore(-1, -3, false, false),
//                intakeAndScore(-3, -5, false, false),
//                intakeAndScore(-5, -6, false, false),
//                intakeAndScore(-6, 1.5, false, false),
////                new ConditionalCommand(intakeAndScore(1, 3, false, true), new InstantCommand(), () -> timer.remainingTime() > 4000),
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
//    private Pose2d getChangedY(Pose2d pose, double y) {
//        return new Pose2d(new Vector2d(pose.position.x, y), pose.heading);
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
//        score.getPath(1).setZeroPowerAccelerationMultiplier(8);
//        score.setCallbacks(new PathCallback(0, () -> {
//            follower.setMaxPower(PUSH);
//        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
//            follower.setMaxPower(PUSH);
//        }, PathCallback.PARAMETRIC, 1));
//        return score;
//    }
//
//    private PathChain fastSpecimenScoring(Pose2d initial, double xPos, boolean start) {
//        PathChain score;
//        if (start) {
//            score = new PathChain(
//                    new Path(new BezierCurve(
//                        new Point(initial),
//                        new Point(getChangedY(getChangedX(specimenFirstScoring, xPos),
//                                specimenFirstScoring.position.y + 20)),
//                        new Point(getChangedX(specimenFirstScoring, xPos))
//                    ))
//            );
//        } else {
//            score = new PathChain(
//                    new Path(new BezierCurve(
//                            new Point(initial),
//                            new Point(getChangedY(getChangedX(specimenScoring, xPos),
//                                    specimenScoring.position.y + 20)),
//                            new Point(getChangedX(specimenScoring, xPos))
//                    ))
//            );
//        }
//
//        score.getPath(0).setConstantHeadingInterpolation(specimenScoring.heading.toDouble());
//        // Reduce the time waiting for the follower to complete its path
//        score.getPath(0).setPathEndTValueConstraint(0.95);
//        score.getPath(0).setPathEndTimeoutConstraint(20);
//        score.getPath(0).setZeroPowerAccelerationMultiplier(5);
//
//        return score;
//    }
//
//    Pose2d specimenLinedUp = new Pose2d(3, 40.0, Math.toRadians(-87));
//    // changed x position from 3 to 2
//    Pose2d specimenScoring = new Pose2d(2, 34.75, Math.toRadians(-85));
//    Pose2d specimenFirstScoring = new Pose2d(-6, 31.5, Math.toRadians(-87));
//
//    Pose2d driveControlPoint = new Pose2d(0, 48, Math.toRadians(-87));
//    Pose2d driveControlPoint2 = new Pose2d(-24, 48, Math.toRadians(-87));
//
//    Pose2d subGive = new Pose2d(-30.5, 47, Math.toRadians(-235));
//
//    Pose2d push1 = new Pose2d(-24.5, 42.5, Math.toRadians(-135));
//    Pose2d postPush1 = new Pose2d(-25.5, 50, Math.toRadians(-235));
//
//    Pose2d push2 = new Pose2d(-31, 46, Math.toRadians(-135));
//    Pose2d postPush2 = new Pose2d(-35, 52, Math.toRadians(-235));
//
//    Pose2d push3 = new Pose2d(-44.5, 45.5, Math.toRadians(-135));
//    Pose2d postPush3 = new Pose2d(-39.5, 52, Math.toRadians(-235));
//    private Command scorePreloadAndFirstPush() {
//        double slideOffset = 0.8;
//        Path intakeSub = new Path(new BezierCurve(
//            new Point(specimenFirstScoring),
//            new Point(subIntake)
//        ));
//        intakeSub.setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), subIntake.heading.toDouble(), 0.8);
//
//        // Creates the path for the robot to push the samples
//        PathChain goPrePush = new PathChain(new Path(new BezierCurve(
//                new Point(subIntake),
//                new Point(driveControlPoint),
//                new Point(driveControlPoint2),
//                new Point(subGive)
//        )));
//        goPrePush.getPath(0).setLinearHeadingInterpolation(specimenScoring.heading.toDouble(), subGive.heading.toDouble(), 0.8);
//        goPrePush.getPath(0).setZeroPowerAccelerationMultiplier(8);
//        goPrePush.getPath(0).setPathEndTValueConstraint(0.8);
//        goPrePush.setCallbacks(
//                new PathCallback(0.5, () -> {
//                    robot.intake.slideOut();
//                    robot.intake.wristIntake();
//                }, PathCallback.PARAMETRIC, 1)
//        );
//        Path firstPush = new Path(new BezierLine(
//                new Point(push1),
//                new Point(postPush1)
//        ));
//        firstPush.setLinearHeadingInterpolation(push1.heading.toDouble(), postPush1.heading.toDouble());
//        firstPush.setZeroPowerAccelerationMultiplier(8);
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
//
//        // Returns the path to score the specimen and push samples
//        return new SequentialCommandGroup(
//                // scoring the specimen
//            new ParallelCommandGroup(
//                new ParallelRaceGroup(
//                        new WaitCommand(1200),
//                        new SequentialCommandGroup(
////                                new WaitCommand(400), // Give the slides some time to extend
//                                // Drives to score the specimen with x Position 2
//                                new FollowPath(follower, specimenScoringChain(start, 0, true))
//                        )
//                ),
//                // Raises the slides to prepare for scoring
//                new ExtendSpecimenCommand(robot.outtake, true)
//            ),
//            new InstantCommand(() -> follower.setMaxPower(PUSH)),
//            new ParallelCommandGroup(
//                    new ScoreSpecimenCommand(robot.outtake),
//                    // Follow the path to push the first sample
////                    new FollowPath(follower, goPrePush, true)
//                    new FollowPath(follower, intakeSub, true),
//                    new SequentialCommandGroup(
//                // Give time for robot to move away before extending hslides
//                            new WaitCommand(100),
//                            new IntakeAuto.IntakeAutoPrep(robot.intake, true, slideOffset)
////                            new IntakeAuto(robot.intake, 1000, slideOffset)
//                    )
//            ),
//                // get the red sample in the sub
//                new IntakeAuto(robot.intake, 500, slideOffset),
//                // check if the sample is correct, otherwise spit it out
//                new ConditionalCommand(
//                        new com.arcrobotics.ftclib.command.SequentialCommandGroup(
//                                new IntakeReverseCommand(robot.intake),
//                                new WaitCommand(500)
//                        ),
//                        new InstantCommand(),
//                        robot.intake::isWrongColor
//                ),
//                new ParallelCommandGroup(
//                        new SequentialCommandGroup(
//                                new WaitCommand(100),
//                                new FollowPath(follower, goPrePush, true)
//                        ),
//                        retractIntake(),
//                        new SequentialCommandGroup(
//                                new WaitCommand(1300),
//                                new InstantCommand(() -> {
//                                    robot.intake.slidePos(0.3);
//                                    robot.intake.wristReverse();
//                                    robot.intake.wristIntake();
//                                    robot.intake.intakeReverse();
//                                })
//                        )
//                ),
//
//                new WaitCommand(200)
////                new IntakeAuto(robot.intake, robot.outtake, 1000),
////                new ParallelCommandGroup(
////                        new FollowPath(follower, firstPush, true),
////                        new InstantCommand(() -> {
////                            robot.intake.slidePos(0.5);
////                        })
////                ),
////
////                new InstantCommand(robot.intake::intakeReverse),
////                new WaitCommand(300)
//        );
//    }
//
//    double giveAngle = Math.toRadians(-235);
//    double grabAngle = Math.toRadians(-135);
//    private Command giveToHP(Pose2d grab, Pose2d lastGive, Pose2d give, boolean last) {
////        PathChain toIntake = new PathChain(new Path(new BezierLine(
////                new Point(lastGive),
////                new Point(grab)
////        )));
//    	Path toIntake = new Path(new BezierLine(
//                new Point(lastGive),
//                new Point(grab)
//        ));
//        toIntake.setPathEndTimeoutConstraint(50);
//        toIntake.setLinearHeadingInterpolation(giveAngle, grabAngle, 0.8);
//        toIntake.setZeroPowerAccelerationMultiplier(8);
//        toIntake.setPathEndTValueConstraint(0.9);
////		toIntake.getPath(0).setLinearHeadingInterpolation(giveAngle, grabAngle);
////        toIntake.getPath(0).setZeroPowerAccelerationMultiplier(8);
////        toIntake.setCallbacks(
////                new PathCallback(
////
////                )
////        );
//
//        Path givePath = new Path(new BezierLine(
//                new Point(grab),
//                new Point(give)
//        ));
//        givePath.setPathEndTimeoutConstraint(50);
//		givePath.setLinearHeadingInterpolation(grabAngle, giveAngle, 0.8);
//        givePath.setPathEndTValueConstraint(0.9);
//        givePath.setZeroPowerAccelerationMultiplier(8);
//
//        if (last) {
//            return new SequentialCommandGroup(
////                    new ParallelCommandGroup(
//                            new FollowPath(follower, toIntake, true),
//                            new IntakeAuto(robot.intake, robot.outtake, 1500),
////                    ),
//                    new ParallelCommandGroup(
//                            new FollowPath(follower, givePath, true),
//                            new InstantCommand(() -> {
//                                robot.intake.slidePos(0.3);
//                            })
//                    ),
//                    new InstantCommand(robot.intake::wristReverse),
//                    new InstantCommand(robot.intake::intakeReverse),
//                    new WaitCommand(200),
//                    new InstantCommand(robot.intake::intakeOn)
//            );
//        } else {
//            return new SequentialCommandGroup(
//                    new ParallelCommandGroup(
//                            new FollowPath(follower, toIntake, true),
////                    new InstantCommand(() -> {
////                        Log.d("Finished Turning! Heading: ", "" + follower.getPose().heading.toDouble());
////                    }),
//                            new SequentialCommandGroup(
//                                    new WaitCommand(200),
//                                    new IntakeAuto(robot.intake, robot.outtake, 1800)
//                            )
//                    ),
//                    new InstantCommand(robot.intake::intakeOn),
//                    new ParallelCommandGroup(
//                            new FollowPath(follower, givePath, true),
//                            new InstantCommand(() -> {
//                                robot.intake.slidePos(0.3);
//                            })
//                    ),
//                    new InstantCommand(robot.intake::wristReverse),
//                    new InstantCommand(robot.intake::intakeReverse),
//                    new WaitCommand(200),
//                    new InstantCommand(robot.intake::intakeOff)
//            );
//        }
//    }
//
//    private Command retractIntake() {
//        return new ParallelCommandGroup(
//                new InstantCommand(robot.intake::intakeOff),
//                new InstantCommand(robot.intake::wristPreIntake),
//                new InstantCommand(robot.intake::slideIn)
//        );
//    }
//
//    // pre-intake position after bot just finished pushing
//    Pose2d preIntake = new Pose2d(-36, 54, Math.toRadians(-90));
//    // pre-intkae position for almost all cycles
//    Pose2d smoothPreIntake = new Pose2d(-36, 51, Math.toRadians(-90));
//    Pose2d intakeControlPoint = new Pose2d(-12, 48, Math.toRadians(-90));
//    // intake position for almost all cycles
//    Pose2d intakePosition = new Pose2d(-36, 54, Math.toRadians(-90));
//    // intake position for the second cycle, after bot just finished pushing
//    Pose2d firstIntakePosition = new Pose2d(-36, 60, Math.toRadians(-90));
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
//        preIntakePath.setZeroPowerAccelerationMultiplier(5.0);
//        PathChain intakePath = new PathChain(new Path(new BezierLine(
//                new Point(start ? preIntake : smoothPreIntake),
//                startIntake ? new Point(firstIntakePosition) : new Point(intakePosition)
//        )));
//        intakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.heading.toDouble());
//        intakePath.getPath(0).setZeroPowerAccelerationMultiplier(1.5);
//        intakePath.setCallbacks(
//                new PathCallback(0.7, () -> {
//                    robot.outtake.specimenClawClose();
//                }, PathCallback.PARAMETRIC, 0)
//        );
//
//        PathChain fastIntakePath = new PathChain(
//                new Path(new BezierCurve(
//                new Point(getChangedX(specimenScoring, prevXPos)),
//                new Point(getChangedY(intakePosition, intakePosition.position.y - 3)),
//                new Point(intakePosition)
//        )));
//        fastIntakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.heading.toDouble());
//        fastIntakePath.getPath(0).setZeroPowerAccelerationMultiplier(2.0);
//        fastIntakePath.setCallbacks(
//                new PathCallback(0.87, () -> {
//                    robot.outtake.specimenClawClose();
//                }, PathCallback.PARAMETRIC, 0)
//        );
//
////        PathChain smoothIntake = new PathChain();
//        if (!start) {
////            smoothIntake = new PathChain(preIntakePath, intakePath);
////            smoothIntake.getPath(0).setConstantHeadingInterpolation(intakePosition.heading.toDouble());
////            smoothIntake.setCallbacks(
////                    new PathCallback(0.0, () -> {
////                follower.setMaxPower(PUSH); // slow
////                }, PathCallback.PARAMETRIC, 1),
////                new PathCallback(0.8, () -> {
////                    robot.outtake.specimenClawClose();
////                }, PathCallback.PARAMETRIC, 1));
//        }
//        // path for the first cycle after pushing
//        if (start == true) {
//            return new SequentialCommandGroup(
//                    new InstantCommand(() -> {
//                        FollowerConstants.headingPIDFCoefficients = new CustomPIDFCoefficients(
//                                5,
//                                0,
//                                0.2,
//                                0
//                        );
//                    }),
//                    new ParallelCommandGroup(
//                            retractIntake(),
//                            start ? new WaitCommand(1000) : new ScoreSpecimenCommand(robot.outtake),
//                            new FollowPath(follower, preIntakePath, true)
//                    ),
//                    new InstantCommand(() -> {
//                        FollowerConstants.headingPIDFCoefficients = new CustomPIDFCoefficients(
//                                1,
//                                0,
//                                0.2,
//                                0
//                        );
//                    }),
//                    new InstantCommand(() -> follower.setMaxPower(SLOW)), // SLOW
//                    new FollowPath(follower, intakePath, true),
////                    new WaitCommand(100),
////                    new InstantCommand(() -> {
////                        robot.outtake.specimenClawClose();
////                    }),
////                    new WaitCommand(300),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH)),
//
//                    new ParallelCommandGroup(
//                            new ParallelRaceGroup(
//                                    new WaitCommand(2500),
////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
////                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
//                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
//                            ),
//                            new ExtendSpecimenCommand(robot.outtake, true)
//                    ),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH))
//            );
//        } else {
//            // path on all other cycles
//            return new SequentialCommandGroup(
//                    new ParallelCommandGroup(
//                        new ScoreSpecimenCommand(robot.outtake),
//                        new FollowPath(follower, fastIntakePath)
//                    ),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH)),
//
//                    new ParallelCommandGroup(
//                            new ParallelRaceGroup(
//                                    new WaitCommand(2500),
////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
////                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
//                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
//                            ),
//                            new ExtendSpecimenCommand(robot.outtake, true)
//                    ),
//                    new InstantCommand(() -> follower.setMaxPower(PUSH))
//            );
//        }
//
//    }
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
//                new InstantCommand(() -> follower.setMaxPower(PUSH)),
//                new ScoreSpecimenCommand(robot.outtake),
//                new FollowPath(follower, park, true)
//        );
//    }
//}
