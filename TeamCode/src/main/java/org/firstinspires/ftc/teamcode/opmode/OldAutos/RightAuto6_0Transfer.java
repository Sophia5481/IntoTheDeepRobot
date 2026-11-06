package org.firstinspires.ftc.teamcode.opmode.OldAutos;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
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
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Gamepad;

import java.util.concurrent.TimeUnit;

@Disabled
@Autonomous(name = "Right Auto 6+0 Transfer Version", group = "Auto")
public class RightAuto6_0Transfer extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private ManualInput manualInput;
    private Gamepad g1;

    Pose start = new Pose(-8.5, 65.0, Math.toRadians(-90));
    Pose subIntake = new Pose(2, 40, Math.toRadians(-90));

    Pose intake1 = new Pose(-46.0, 51.0, Math.toRadians(-90));
    Pose intake2 = new Pose(-57.0, 51.0, Math.toRadians(-90));
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
        follower.setMaxPower(PUSH);
        robot = new Robot(true, hardwareMap);

        telemetry.addLine("Ready!");
        telemetry.update();

        while (opModeInInit()) {
            // first do alliance selection
            // once start is pressed, the gamepad inputs will be read for sub intaking
            if (!readGamepad) {
                robot.allianceConfig(telemetry, gamepad1);
                if (gamepad1.start) {
                    readGamepad = true;
                }
            } else {
                manualInput.update(g1);
                subIntake = manualInput.getManualPoses().get(0).getPose().copy();
                telemetry.addLine("sub Intake x: " + subIntake.getX());
                telemetry.addLine("sub Intake y: " + subIntake.getY());
                telemetry.update();
            }
        }

        waitForStart();
        timer.start();

        schedule(new SequentialCommandGroup(
                scorePreloadAndFirstPush(),

                giveToHP(start, intake2, false, true),
                giveToHP(intake2, intake2, true, true),
                giveToHP(intake2, intake2, false, false),

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
                    new Path(new BezierLine(new Point(getChangedX(specimenLinedUp, xPos)), new Point(getChangedX(specimenFirstScoring, xPos))))
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
        score.getPath(1).setZeroPowerAccelerationMultiplier(8);
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
    Pose specimenFirstScoring = new Pose(-6, 31.0, Math.toRadians(-90));

    Pose driveControlPoint = new Pose(0, 48, Math.toRadians(-87));
    Pose driveControlPoint2 = new Pose(-24, 48, Math.toRadians(-87));

    Pose subGive = new Pose(-31.5, 48, Math.toRadians(-235));

    Pose push1 = new Pose(-25.0, 49, Math.toRadians(-135));
    Pose postPush1 = new Pose(-25.5, 51, Math.toRadians(-235));

    Pose push2 = new Pose(-35, 49, Math.toRadians(-135));
    Pose postPush2 = new Pose(-35, 52, Math.toRadians(-235));

    Pose push3 = new Pose(-44.5, 49, Math.toRadians(-135));
    Pose postPush3 = new Pose(-39.5, 52, Math.toRadians(-235));
    private Command scorePreloadAndFirstPush() {
        double slideOffset = 0.8;
        Path intakeSub = new Path(new BezierCurve(
                new Point(specimenFirstScoring),
                new Point(subIntake)
        ));
        intakeSub.setLinearHeadingInterpolation(specimenScoring.getHeading(), subIntake.getHeading(), 0.8);

        // Creates the path for the robot to push the samples
        PathChain goTransfer = new PathChain(new Path(new BezierCurve(
                new Point(subIntake),
                new Point(intake1)
        )));
        goTransfer.getPath(0).setLinearHeadingInterpolation(subIntake.getHeading(), intake1.getHeading(), 0.8);
        goTransfer.getPath(0).setZeroPowerAccelerationMultiplier(4);
        goTransfer.getPath(0).setPathEndTimeoutConstraint(50);

        // Returns the path to score the specimen and push samples
        return new SequentialCommandGroup(
                // scoring the specimen
                new ParallelCommandGroup(
                        new ParallelRaceGroup(
                                new WaitCommand(1500),
                                new SequentialCommandGroup(
                                        new WaitCommand(300), // Give the slides some time to extend
                                        // Drives to score the specimen with x Position 2
                                        new FollowPath(follower, specimenScoringChain(start, -5, true), true)
                                )
                        ),
                        // Raises the slides to prepare for scoring
                        new ExtendSpecimenCommand(robot.outtake, true)
                ),
//                new WaitCommand(200),
                new InstantCommand(() -> follower.setMaxPower(PUSH)),
                new ParallelCommandGroup(
                        new ScoreSpecimenCommand(robot.outtake),
                        // Follow the path to push the first sample
                        new LimelightCommand(robot.intake, follower, false, true)
                ),

                // TODO: Get rid of the wait time
                new WaitCommand(500),
                // get the red sample in the sub
                new IntakeAuto(robot.intake, 1250, 0.0),
                // check if the sample is correct, otherwise spit it out
                new ConditionalCommand(
                        new IntakeReverseCommand(robot.intake, false),
                        new InstantCommand(),
                        robot.intake::needReverse
                ),
                new ParallelCommandGroup(
                        new SequentialCommandGroup(
                                new WaitCommand(100),
                                new FollowPath(follower, goTransfer, true)
                        ),
                        new SequentialCommandGroup(
                                new IntakeRetractCommand(robot.intake, robot.outtake, false),
                                new GiveToHP(robot.outtake, true),
                                new WaitCommand(200),
                                new IntakeAuto.IntakeAutoPrep(robot.intake, false, 0.2)
                        )
                )
        );
    }

    double giveAngle = Math.toRadians(-235);
    double grabAngle = Math.toRadians(-135);
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
                        turn ? new InstantCommand(() -> {follower.turn(Math.toRadians(25.0), false); }) : new FollowPath(follower, intakePath, true),
                        new IntakeRetractCommand(robot.intake, robot.outtake, true)
                ) : new IntakeRetractCommand(robot.intake, robot.outtake, true),

                new ParallelCommandGroup(
                        new GiveToHP(robot.outtake, true),
                        new SequentialCommandGroup(
                                new WaitCommand(200),
                                move ? new IntakeAuto.IntakeAutoPrep(robot.intake, false, 0.2) : new InstantCommand()
                        )
                ),

                new WaitCommand(200)
        );
    }

    // tries to intake sample again if it didn't get one 😔
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
                new InstantCommand(robot.intake::intakeOn),
                new InstantCommand(robot.intake::wristPreIntake),
                new InstantCommand(robot.intake::slideIn),
                new SequentialCommandGroup(
                        new WaitCommand(500),
                        new InstantCommand(robot.intake::intakeOff)
                )
        );
    }

    // pre-intake position after bot just finished pushing
    Pose preIntake = new Pose(-36, 54, Math.toRadians(-90));
    // pre-intkae position for almost all cycles
    Pose smoothPreIntake = new Pose(-36, 51, Math.toRadians(-90));
    Pose intakeControlPoint = new Pose(-12, 48, Math.toRadians(-90));
    // intake position for almost all cycles
    Pose intakePosition = new Pose(-36, 58, Math.toRadians(-90));
    // intake position for the second cycle, after bot just finished pushing
    Pose firstIntakePosition = new Pose(-38, 58, Math.toRadians(-90));
    public Command intakeAndScore(double prevXPos, double xPos, boolean start, boolean startIntake) {
        Path preIntakePath;
        if (start) {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(start ? intake2 : specimenScoring, prevXPos)),
                    new Point(start ? intake2 : smoothPreIntake)
            ));
        } else {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(start ? intake2 : specimenScoring, prevXPos)),
                    new Point(intakeControlPoint),
                    new Point(start ? preIntake : smoothPreIntake)
            ));
            preIntakePath.setPathEndTValueConstraint(0.8);
        }

        preIntakePath.setLinearHeadingInterpolation(start ? intake2.getHeading() : specimenScoring.getHeading(), intakePosition.getHeading(), 0.8);
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
        fastIntakePath.getPath(0).setPathEndTimeoutConstraint(150);

        // path for the first cycle after pushing
        if (start == true) {
            return new SequentialCommandGroup(
                    new ParallelCommandGroup(
                            retractIntake(),
                            start ? new WaitCommand(1000) : new ScoreSpecimenCommand(robot.outtake),
                            start ? new ScoreBucketCommand(robot.outtake) : new InstantCommand(),
                            new InstantCommand(() -> {
                                follower.setHeadingPIDF(new CustomPIDFCoefficients(1.5, 0, 0.2, 0));
                            }),
                            new FollowPath(follower, preIntakePath, true)
                    ),
                    new InstantCommand(() -> follower.setMaxPower(FULLSPEED)), // SLOW
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