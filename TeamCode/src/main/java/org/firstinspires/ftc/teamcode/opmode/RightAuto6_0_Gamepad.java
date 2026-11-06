package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathCallback;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.CustomPIDFCoefficients;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.ManualInput;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;
import org.firstinspires.ftc.teamcode.utils.Logger;

import java.util.concurrent.TimeUnit;

@Disabled
@Autonomous(name = "Right Auto 6+0 Gamepad", group = "Auto")
public class RightAuto6_0_Gamepad extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private ManualInput manualInput;
    private Gamepad g1;
    private Telemetry telem;

    Pose start = new Pose(-8.5, 65.0, Math.toRadians(-90));
    Pose subIntake = new Pose(2, 38, Math.toRadians(-90));
    public static double FULLSPEED = 0.8;
    public static double PUSH = 1.0;
    public static double SLOW = 0.6;
    Timing.Timer timer = new Timing.Timer(30000, TimeUnit.MILLISECONDS);

    boolean readGamepad = false;

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();
        g1 = new Gamepad();
        manualInput = new ManualInput(telem, 0, true);

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(start);
        follower.updatePose();
        //follower.setMaxPower(FULLSPEED);
        follower.setMaxPower(PUSH);
        robot = new Robot(true, hardwareMap);

        telemetry.addLine("Ready!");
        telemetry.update();

        while (opModeInInit()) {
            // first do alliance selection
            // once start is pressed, the gamepad inputs will be read for sub intaking
            robot.allianceConfig(telemetry, gamepad1);
            manualInput.update(g1);
            subIntake = manualInput.getManualPoses().get(0).getPose().copy();
        }

        waitForStart();
        timer.start();

        schedule(new SequentialCommandGroup(
                scorePreloadAndFirstPush(),

                //giveToHP(push1, subGive, postPush1, false),
                //giveToHP(push2, postPush1, postPush2, false),
                //giveToHP(push3, postPush2, postPush3, true),

                intakeAndScore(-4, -5, true, true),
                // the initial scoring position and scoring position when driving over to score have diff
                // amounts of drift
                intakeAndScore(-5, -3, false, false),
                intakeAndScore(-3, -1, false, false),
                intakeAndScore(-1, 1, false, false),
                intakeAndScore(1, 3, false, false),
//                new ConditionalCommand(intakeAndScore(1, 3, false, true), new InstantCommand(), () -> timer.remainingTime() > 4000),
                park()
        ));
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

    private Pose getChangedX(Pose pose, double x) {
        return new Pose(x, pose.getY(), pose.getHeading());
    }

    private Pose getChangedY(Pose pose, double y) {
        return new Pose(pose.getX(), y, pose.getHeading());
    }

    private PathChain specimenScoringChain(Pose initial, double xPos, boolean start) {
        // Creates the path for the robot to score the specimen
        PathChain score;
        if (start) {
            score = new PathChain(
                    new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
                    new Path(new BezierLine(new Point(getChangedX(specimenLinedUp, xPos)), new Point(getChangedX(specimenPathFirstScoring, xPos))))
            );
        } else {
            score = new PathChain(
                    new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
                    new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenScoring, xPos))))
            );
        }
//        PathChain score = new PathChain(
//                new Path(new BezierLine(new Point(initial), new Point(getChangedX(specimenLinedUp, xPos)))),
//                new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenScoring, xPos))))
//        );
        score.getPath(0).setLinearHeadingInterpolation(initial.getHeading(),
                start ? specimenFirstScoring.getHeading() : specimenScoring.getHeading(), 0.8);
        score.getPath(1).setConstantHeadingInterpolation(specimenScoring.getHeading());
        // Reduce the time waiting for the follower to complete its path
        score.getPath(1).setPathEndTValueConstraint(0.95);
        score.getPath(1).setPathEndTimeoutConstraint(20);
        if (start) {
            follower.setMaxPower(FULLSPEED);
        }
        score.getPath(1).setZeroPowerAccelerationMultiplier(start ? 3.0 : 8);
        score.setCallbacks(new PathCallback(0, () -> {
            follower.setMaxPower(PUSH);
        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
            follower.setMaxPower(PUSH);
        }, PathCallback.PARAMETRIC, 1));
        return score;
    }

    private PathChain fastSpecimenScoring(Pose initial, double xPos, boolean start) {
        PathChain score;
        if (start) {
            score = new PathChain(
                    new Path(new BezierCurve(
                            new Point(initial),
                            new Point(getChangedY(getChangedX(specimenFirstScoring, xPos),
                                    specimenFirstScoring.getY() + 20)),
                            new Point(getChangedX(specimenFirstScoring, xPos))
                    ))
            );
            score.getPath(0).setZeroPowerAccelerationMultiplier(3.0);
        } else {
            score = new PathChain(
                    new Path(new BezierCurve(
                            new Point(initial),
                            new Point(getChangedY(getChangedX(specimenScoring, xPos),
                                    specimenScoring.getY() + 20)),
                            new Point(getChangedX(specimenScoring, xPos))
                    ))
            );
        }

        score.getPath(0).setConstantHeadingInterpolation(specimenScoring.getHeading());
        // Reduce the time waiting for the follower to complete its path
        score.getPath(0).setPathEndTValueConstraint(0.95);
        score.getPath(0).setPathEndTimeoutConstraint(20);
        score.getPath(0).setZeroPowerAccelerationMultiplier(5);

        return score;
    }

    Pose specimenLinedUp = new Pose(3, 40.0, Math.toRadians(-87));
    // changed x position from 3 to 2
    Pose specimenScoring = new Pose(2, 34.75, Math.toRadians(-85));
    Pose specimenPathFirstScoring = new Pose(-6, 34.0, Math.toRadians(-90)); // Further away so that path ends faster
    Pose specimenFirstScoring = new Pose(-6, 31.0, Math.toRadians(-90));

    Pose driveControlPoint = new Pose(0, 48, Math.toRadians(-87));
    Pose driveControlPoint2 = new Pose(-24, 48, Math.toRadians(-87));
    Pose subGive = new Pose(-33.5, 48, Math.toRadians(125));
    public static Pose goPushControlPoint = new Pose(-32, 56, Math.toRadians(0));
    public static Pose goPushControlPoint2 = new Pose(-32, 12, Math.toRadians(0));
    // the control point when preparing for the second push
    public static Pose pushControlPoint = new Pose(-40, 30, Math.toRadians(0));
    // the control point when preparing for the third push
    public static Pose pushControlPoint2 = new Pose(-50, 30, Math.toRadians(0));
    public static Pose push1 = new Pose(-42.0, 15, Math.toRadians(90));
    public static Pose postPush1 = new Pose(-42.0, 46, Math.toRadians(90));

    public static Pose push2 = new Pose(-52, 21, Math.toRadians(90));
    public static Pose postPush2 = new Pose(-52, 46, Math.toRadians(90));

    public static Pose push3 = new Pose(-58.5, 21, Math.toRadians(90));
    public static Pose postPush3 = new Pose(-59.5, 55, Math.toRadians(90));
    private Command scorePreloadAndFirstPush() {
        // Creates the path for the robot to push the samples
        PathChain goPrePush = new PathChain(new Path(new BezierCurve(
                new Point(specimenFirstScoring),
                new Point(driveControlPoint),
                new Point(driveControlPoint2),
                new Point(subGive)
        )));
        goPrePush.getPath(0).setLinearHeadingInterpolation(specimenFirstScoring.getHeading(), subGive.getHeading(), 0.8);
        goPrePush.getPath(0).setZeroPowerAccelerationMultiplier(8);
        goPrePush.getPath(0).setPathEndTValueConstraint(0.8);
        goPrePush.getPath(0).setPathEndTimeoutConstraint(50);

        Path goPrePrePush = new Path(new BezierCurve(
                new Point(subGive),
                new Point(goPushControlPoint),
                new Point(goPushControlPoint2),
                new Point(push1)
        ));
        goPrePrePush.setLinearHeadingInterpolation(subGive.getHeading(), push1.getHeading(), 0.5);
        goPrePrePush.setZeroPowerAccelerationMultiplier(0.8);
        goPrePrePush.setZeroPowerAccelerationMultiplier(8);

        Path firstPush = new Path(new BezierLine(
                new Point(push1),
                new Point(postPush1)
        ));
        firstPush.setLinearHeadingInterpolation(push1.getHeading(), postPush1.getHeading());
        firstPush.setZeroPowerAccelerationMultiplier(3.0);

        Path prePush2 = new Path(new BezierCurve(
                new Point(postPush1),
                new Point(pushControlPoint),
                new Point(push2)
        ));
        prePush2.setLinearHeadingInterpolation(postPush1.getHeading(), push2.getHeading());
        prePush2.setZeroPowerAccelerationMultiplier(3.0);
        Path secondPush = new Path(new BezierLine(
                new Point(push2),
                new Point(postPush2)
        ));
        secondPush.setLinearHeadingInterpolation(push2.getHeading(), postPush2.getHeading());
        secondPush.setZeroPowerAccelerationMultiplier(3.0);

        Path prePush3 = new Path(new BezierCurve(
                new Point(postPush2),
                new Point(pushControlPoint2),
                new Point(push3)
        ));
        prePush3.setLinearHeadingInterpolation(postPush2.getHeading(), push3.getHeading());
        prePush3.setZeroPowerAccelerationMultiplier(2.0);
        Path thirdPush = new Path(new BezierLine(
                new Point(push3),
                new Point(postPush3)
        ));
        thirdPush.setLinearHeadingInterpolation(push3.getHeading(), postPush3.getHeading());
        thirdPush.setZeroPowerAccelerationMultiplier(8);
        thirdPush.setPathEndTimeoutConstraint(50);
        PathChain pushPath = new PathChain(goPrePrePush, firstPush, prePush2, secondPush, prePush3, thirdPush);

        // Returns the path to score the specimen and push samples
        return new SequentialCommandGroup(
                // scoring the specimen
                new ParallelCommandGroup(
                        new SequentialCommandGroup(
                                new WaitCommand(300), // Give the slides some time to extend
                                // Drives to score the specimen with x Position 2
                                new FollowPath(follower, specimenScoringChain(start, -5, true)),
                                new InstantCommand(() -> follower.holdPoint(specimenFirstScoring))
//                                new WaitCommand(200)
                        ),
                        // Raises the slides to prepare for scoring
                        new ExtendSpecimenCommand(robot.outtake, true)
                ),
                //new WaitCommand(200),
                new InstantCommand(() -> follower.setMaxPower(PUSH)),
                new ParallelCommandGroup(
                        new ScoreSpecimenCommand(robot.outtake),
                        // Follow the path to push the first sample
                        new LimelightCommand(robot.intake, follower, false, true)
                ),

                new WaitCommand(100),
                // get the red sample in the sub
                new IntakeAuto(robot.intake, 750, 0.0),
                // check if the sample is correct, otherwise spit it out
                new InstantCommand(() -> follower.setMaxPower(1.0)),
                new ConditionalCommand(
                        new SequentialCommandGroup(
                                new IntakeReverseCommand(robot.intake, false),
                                retractIntake(),
                                new FollowPath(follower, goPrePush, true)
                        ),
                        new ParallelCommandGroup( // If it is correct, give to HP
                                new SequentialCommandGroup(
                                        new InstantCommand(() -> {
                                            goPrePush.setCallbacks(
                                                    new PathCallback(0.5, () -> {
                                                        robot.intake.slideOut();
                                                        robot.intake.wristIntake();
                                                    }, PathCallback.PARAMETRIC, 1)
                                            );
                                        }),
                                        new WaitCommand(100),
                                        new FollowPath(follower, goPrePush, true)
                                ),
                                retractIntake(),
                                new SequentialCommandGroup(
                                        new WaitCommand(1300),
                                        new InstantCommand(() -> {
                                            robot.intake.slidePos(0.3);
                                            robot.intake.wristIntake();
                                            robot.intake.intakeReverse();
                                        })
                                ),
                                new SequentialCommandGroup(
                                        new WaitUntilCommand(() -> !robot.intake.sampleIntaked()),
                                        new WaitCommand(50)
                                )
                        ),
                        () -> robot.intake.needReverse()
                ),
                new ParallelCommandGroup(
                        new InstantCommand(() -> {
                            robot.intake.slideIn();
                            robot.intake.wristTransfer();
                            robot.intake.intakeOff();
                        }),
                        new FollowPath(follower, pushPath, false)
                )
        );
    }

    double giveAngle = Math.toRadians(-235);
    double grabAngle = Math.toRadians(-135);
    private Command giveToHP(Pose grab, Pose lastGive, Pose give, boolean last) {
//        PathChain toIntake = new PathChain(new Path(new BezierLine(
//                new Point(lastGive),
//                new Point(grab)
//        )));
        Path toIntake = new Path(new BezierLine(
                new Point(lastGive),
                new Point(grab)
        ));
        toIntake.setPathEndTimeoutConstraint(300);
        toIntake.setLinearHeadingInterpolation(giveAngle, grabAngle, 0.8);
        toIntake.setZeroPowerAccelerationMultiplier(4);
        toIntake.setPathEndTValueConstraint(0.99);
//		toIntake.getPath(0).setLinearHeadingInterpolation(giveAngle, grabAngle);
//        toIntake.getPath(0).setZeroPowerAccelerationMultiplier(8);
//        toIntake.setCallbacks(
//                new PathCallback(
//
//                )
//        );

        Path givePath = new Path(new BezierLine(
                new Point(grab),
                new Point(give)
        ));
        givePath.setPathEndTimeoutConstraint(100);
        givePath.setLinearHeadingInterpolation(grabAngle, giveAngle, 0.8);
        givePath.setPathEndTValueConstraint(0.99);
        givePath.setZeroPowerAccelerationMultiplier(8);

        if (last) {
            return new SequentialCommandGroup(
//                    new ParallelCommandGroup(
                    new FollowPath(follower, toIntake, true),
                    new IntakeAuto(robot.intake, robot.outtake, 1000),
//                    ),
                    new ParallelCommandGroup(
                            new FollowPath(follower, givePath, true),
                            new InstantCommand(() -> {
                                robot.intake.slidePos(0.5);
                                robot.intake.wristReverse();
                            })
                    ),
                    new InstantCommand(robot.intake::intakeReverse),
                    new WaitCommand(200),
                    new InstantCommand(robot.intake::intakeOff)
            );
        } else {
            return new SequentialCommandGroup(
                    new FollowPath(follower, toIntake, true),
                    new InstantCommand(() -> {
                        Log.d("Finished Turning! Heading: ", "" + follower.getPose().getHeading());
                    }),
                    new IntakeAuto(robot.intake, robot.outtake, 1000),
                    new ConditionalCommand(
                            tryAgain(),
                            new InstantCommand(),
                            () -> robot.intake.sampleIntaked()
                    ),
                    new ParallelCommandGroup(
                            new FollowPath(follower, givePath, true),
                            new InstantCommand(() -> {
                                robot.intake.slidePos(0.5);
                            })
                    ),
                    new InstantCommand(robot.intake::wristReverse),
                    new InstantCommand(robot.intake::intakeReverse),
                    new WaitCommand(50),
                    new InstantCommand(robot.intake::intakeOff)
            );
        }
    }

    // tries to intake sample again if it didn't get one 😔
    // it doesn't work yet 😔
    private Command tryAgain() {
        // TODO: Work on last sample logic
        Pose failedPose = follower.getPose();
        Path retry;
        double retryRadius = failedPose.getHeading() + Math.toRadians(5);
        retry = new Path(new BezierLine(
                new Point(new Pose(failedPose.getX() - 72.0, failedPose.getY() - 72.0, failedPose.getHeading())),
                new Point(new Pose(failedPose.getX() - 72.0, failedPose.getY() - 72.0 + 1.5, retryRadius))
        ));
        retry.setLinearHeadingInterpolation(failedPose.getHeading(), retryRadius);

        return new SequentialCommandGroup(
                new InstantCommand(() -> {
                    Logger.d("Failed heading: ", "Pose: " + failedPose.toString());
                    Logger.d("Failed heading: ", "normal:" + failedPose.getHeading());
                    Logger.d("Failed heading: ", "degrees:" + Math.toDegrees(failedPose.getHeading()));
                    Logger.d("Failed heading: ", "radians:" + Math.toRadians(failedPose.getHeading()));
                    Logger.d("Failed heading: ", "Follower's heading: " + Math.toDegrees(follower.getTotalHeading()));
                }),
                new InstantCommand(() -> {
                    follower.turn(Math.toRadians(5), false);
                }),
                new IntakeAuto(robot.intake, 500, 1.0),
                new InstantCommand(() -> {
                    follower.turn(Math.toRadians(10), true);
                }),
                new IntakeAuto(robot.intake, 500, 1.0)
        );

    }

    private Command retractIntake() {
        return new ParallelCommandGroup(
                new SequentialCommandGroup(
                        new InstantCommand(robot.intake::wristTransfer),
                        new WaitCommand(100),
                        new InstantCommand(robot.intake::intakeTransferOn),
                        new WaitCommand(100),
                        new InstantCommand(robot.intake::intakeOff),
                        new WaitCommand(300)

                ),
                new InstantCommand(robot.intake::slideIn)
        );
    }

    // pre-intake position after bot just finished pushing
    Pose preIntake = new Pose(-36, 54, Math.toRadians(-90));
    // pre-intkae position for almost all cycles
    Pose smoothPreIntake = new Pose(-36, 51, Math.toRadians(-90));
    Pose intakeControlPoint = new Pose(-12, 48, Math.toRadians(-90));
    // intake position for almost all cycles
    Pose intakePosition = new Pose(-36, 57, Math.toRadians(-90));
    // intake position for the second cycle, after bot just finished pushing
    Pose firstIntakePosition = new Pose(-38, 59, Math.toRadians(-90));
    public Command intakeAndScore(double prevXPos, double xPos, boolean start, boolean startIntake) {
        Path preIntakePath;
        if (start) {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(postPush3, prevXPos)),
                    new Point(preIntake)
            ));
        } else {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(specimenScoring, prevXPos)),
                    new Point(intakeControlPoint),
                    new Point(smoothPreIntake)
            ));
            preIntakePath.setPathEndTValueConstraint(0.8);
        }

        preIntakePath.setLinearHeadingInterpolation(start ? postPush3.getHeading() : specimenScoring.getHeading(), intakePosition.getHeading(), 0.4);
        preIntakePath.setZeroPowerAccelerationMultiplier(8.0);

        PathChain intakePath = new PathChain((new Path(new BezierLine(
                new Point(start ? preIntake : smoothPreIntake),
                startIntake ? new Point(firstIntakePosition) : new Point(intakePosition)
        ))));
        intakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.getHeading());
        intakePath.getPath(0).setZeroPowerAccelerationMultiplier(2.5);
        intakePath.getPath(0).setPathEndTimeoutConstraint(150);
        intakePath.setCallbacks(
                new PathCallback(0.7, () -> {
                    robot.outtake.specimenClawClose();
                }, PathCallback.PARAMETRIC, 0)
        );

        PathChain fastIntakePath = new PathChain(
                new Path(new BezierCurve(
                        new Point(getChangedX(specimenScoring, prevXPos)),
                        new Point(getChangedY(intakePosition, intakePosition.getY() - 6)),
                        new Point(intakePosition)
                )));
        fastIntakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.getHeading());
        fastIntakePath.getPath(0).setZeroPowerAccelerationMultiplier(2.5);
        fastIntakePath.setCallbacks(
                new PathCallback(0.87, () -> {
                    robot.outtake.specimenClawClose();
                }, PathCallback.PARAMETRIC, 0)
        );
        fastIntakePath.getPath(0).setPathEndTimeoutConstraint(50);

//        PathChain smoothIntake = new PathChain();
//        if (start) {
//            smoothIntake = new PathChain(preIntakePath, intakePath);
//            smoothIntake.setCallbacks(
//                    new PathCallback(0.0, () -> {
//                follower.setMaxPower(PUSH); // slow
//                }, PathCallback.PARAMETRIC, 1),
//                new PathCallback(0.87, () -> {
//                    robot.outtake.specimenClawClose();
//                }, PathCallback.PARAMETRIC, 1));
//        }
        // path for the first cycle after pushing
        if (start) {
            return new SequentialCommandGroup(
                    new ParallelCommandGroup(
                            new InstantCommand(() -> {
                                follower.setHeadingPIDF(new CustomPIDFCoefficients(2, 0, 0.2, 0));
                            }),
                            start ? new WaitCommand(1000) : new ScoreSpecimenCommand(robot.outtake),
                            new FollowPath(follower, preIntakePath, true)
                    ),
                    new InstantCommand(() -> follower.setMaxPower(PUSH)), // SLOW
                    new FollowPath(follower, intakePath, true),
//                    new WaitCommand(100),
//                    new InstantCommand(() -> {
//                        robot.outtake.specimenClawClose();
//                    }),
//                    new WaitCommand(300),
                    new InstantCommand(() -> follower.setMaxPower(PUSH)),

                    new ParallelCommandGroup(
                            new ParallelRaceGroup(
                                    new WaitCommand(2500),
//                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
                            ),
                            new SequentialCommandGroup(
                                    new WaitCommand(100),
                                    new ExtendSpecimenCommand(robot.outtake, true)
                            )
                    ),
                    new InstantCommand(() -> follower.setMaxPower(PUSH))
            );
        } else {
            // path on all other cycles
            return new SequentialCommandGroup(
                    new ParallelCommandGroup(
                            new ScoreSpecimenCommand(robot.outtake),
                            new FollowPath(follower, fastIntakePath)
                    ),
                    new InstantCommand(() -> follower.setMaxPower(PUSH)),

                    new ParallelCommandGroup(
                            new ParallelRaceGroup(
                                    new WaitCommand(2500),
//                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
                            ),
                            new SequentialCommandGroup(
                                    new WaitCommand(100),
                                    new ExtendSpecimenCommand(robot.outtake, true)
                            )
                    ),
                    new InstantCommand(() -> follower.setMaxPower(PUSH))
            );
        }

    }

    Pose parking = new Pose(-48, 56, Math.toRadians(0));
    public Command park() {
        Path park = new Path(new BezierLine(
                new Point(specimenScoring),
                new Point(parking)
        ));
        park.setLinearHeadingInterpolation(specimenScoring.getHeading(), parking.getHeading(), 0.8);

        return new ParallelCommandGroup(
                new InstantCommand(() -> follower.setMaxPower(PUSH)),
                new ScoreSpecimenCommand(robot.outtake),
                new FollowPath(follower, park, true)
        );
    }
}