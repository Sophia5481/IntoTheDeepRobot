package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.pedropathing.localization.Pose;
import com.acmerobotics.roadrunner.Vector2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.CustomPIDFCoefficients;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;

import com.pedropathing.follower.Follower;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathCallback;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;

import java.util.concurrent.TimeUnit;

@Autonomous(name = "Right Auto PUSH 5+0", group = "Auto")
public class RightAutoPush5_0 extends CommandOpMode {
    private Follower follower;
    private Robot robot;

    Pose start = new Pose(-8.5, 65.0, Math.toRadians(-90));
    public static double FULLSPEED = 0.8;
    public static double PUSH = 1.0;
    public static double SLOW = 0.6;
    Timing.Timer timer = new Timing.Timer(30000, TimeUnit.MILLISECONDS);

    public static Pose goPushControlPoint = new Pose(-32, 56, Math.toRadians(0));
    public static Pose goPushControlPoint2 = new Pose(-34.5, 12, Math.toRadians(0));
    // the control point when preparing for the second push
    public static Pose pushControlPoint = new Pose(-38, 30, Math.toRadians(0));
    // the control point when preparing for the third push
    public static Pose pushControlPoint2 = new Pose(-48, 30, Math.toRadians(0));
    public static Pose push1 = new Pose(-42.0, 12, Math.toRadians(90));
    public static Pose postPush1 = new Pose(-42.0, 46, Math.toRadians(90));

    public static Pose push2 = new Pose(-52, 18, Math.toRadians(90));
    public static Pose postPush2 = new Pose(-52, 46, Math.toRadians(90));

    public static Pose push3 = new Pose(-59.5, 18, Math.toRadians(90));
    public static Pose postPush3 = new Pose(-60, 55, Math.toRadians(90));

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(start);
        follower.updatePose();
        follower.setMaxPower(FULLSPEED);
//        follower.setMaxPower(PUSH);
        robot = new Robot(true, hardwareMap);


        telemetry.addLine("Ready!");
        telemetry.update();

        while (opModeInInit()) {
            // first do alliance selection
            // once start is pressed, the gamepad inputs will be read for sub intaking
            robot.allianceConfig(telemetry, gamepad1);
        }

        waitForStart();
        timer.start();

        schedule(new SequentialCommandGroup(
                scorePreloadAndFirstPush(),
//                giveToHP(push2, postPush1, postPush2),
//                giveToHP(push3, postPush2, postPush3),
                intakeAndScore(-5, -2.8, true, true),
                intakeAndScore(-2.8, -0.6, false, false),
                intakeAndScore(-0.6, 1.6, false, false),
                new ConditionalCommand(intakeAndScore(1.6, 3.8, false, true), new InstantCommand(), () -> timer.remainingTime() > 4000),
                park()
        ));
    }

    @Override
    public void run() {
        super.run();
        follower.drawOnDashBoard();
        follower.update();
        Log.d("Follower ", "Heading error: " + Math.toDegrees(follower.getHeadingError()));
        Log.d("Follower ", "Heading value: " + Math.toDegrees(follower.getTotalHeading()));
        Log.d("Follower ", "Pose: " + follower.getPose().toString());
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
                    new Path(new BezierLine(new Point(specimenLinedUp), new Point(getChangedX(specimenFirstScoring, xPos))))
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
        score.getPath(0).setLinearHeadingInterpolation(initial.getHeading(), specimenScoring.getHeading(), 0.8);
        score.getPath(1).setConstantHeadingInterpolation(specimenScoring.getHeading());
        // Reduce the time waiting for the follower to complete its path
        score.getPath(1).setPathEndTValueConstraint(0.95);
        score.getPath(1).setPathEndTimeoutConstraint(20);
        score.getPath(1).setZeroPowerAccelerationMultiplier(6);
        score.setCallbacks(new PathCallback(0, () -> {
            follower.setMaxPower(FULLSPEED);
        }, PathCallback.PARAMETRIC, 1), new PathCallback(0.9, () -> {
            follower.setMaxPower(FULLSPEED);
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
    Pose specimenScoring = new Pose(2, 35.25, Math.toRadians(-87));
    Pose specimenFirstScoring = new Pose(-6, 33, Math.toRadians(-87));

    Pose driveControlPoint = new Pose(0, 48, Math.toRadians(-87));
    Pose driveControlPoint2 = new Pose(-24, 48, Math.toRadians(-87));

    private Command scorePreloadAndFirstPush() {
        // Creates the path for the robot to push the samples
        Path goPrePush = new Path(new BezierCurve(
                new Point(specimenFirstScoring),
                new Point(goPushControlPoint),
                new Point(goPushControlPoint2),
                new Point(push1)
        ));
        goPrePush.setLinearHeadingInterpolation(specimenScoring.getHeading(), push1.getHeading(), 0.5);
//        goPrePush.setZeroPowerAccelerationMultiplier(0.8);
        goPrePush.setZeroPowerAccelerationMultiplier(8);
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
        thirdPush.setZeroPowerAccelerationMultiplier(6);
        PathChain pushPath = new PathChain(goPrePush, firstPush, prePush2, secondPush, prePush3, thirdPush);

        // Returns the path to score the specimen and push samples
        return new SequentialCommandGroup(
                // scoring the specimen
                new ParallelCommandGroup(
                        new ParallelRaceGroup(
                                new WaitCommand(1500),
                                new SequentialCommandGroup(
                                        new WaitCommand(300), // Give the slides some time to extend
                                        // Drives to score the specimen with x Position 2
                                        new FollowPath(follower, specimenScoringChain(start, -5, true))
                                )
                        ),
                        // Raises the slides to prepare for scoring
                        new ExtendSpecimenCommand(robot.outtake, true)
                ),
                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
                new ParallelCommandGroup(
                        new ScoreSpecimenCommand(robot.outtake),
                        // Follow the path to push the first sample
                        new FollowPath(follower, pushPath, false)
                )
        );
    }

    // pre-intake position after bot just finished pushing
    Pose preIntake = new Pose(-36, 50, Math.toRadians(-90));
    // pre-intkae position for almost all cycles
    Pose smoothPreIntake = new Pose(-36, 45, Math.toRadians(-90));
    Pose intakeControlPoint = new Pose(-12, 48, Math.toRadians(-90));
    // intake position for almost all cycles
    Pose intakePosition = new Pose(-36, 58, Math.toRadians(-90));
    // intake position for the second cycle, after bot just finished pushing
    Pose firstIntakePosition = new Pose(-38, 61, Math.toRadians(-90));
    public Command intakeAndScore(double prevXPos, double xPos, boolean start, boolean startIntake) {
        Path preIntakePath;
        if (start) {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(start ? postPush3 : specimenScoring, prevXPos)),
                    new Point(start ? preIntake : smoothPreIntake)
            ));
        } else {
            preIntakePath = new Path(new BezierCurve(
                    new Point(getChangedX(start ? postPush3 : specimenScoring, prevXPos)),
                    new Point(intakeControlPoint),
                    new Point(start ? preIntake : smoothPreIntake)
            ));
            preIntakePath.setPathEndTValueConstraint(0.99);
        }

        preIntakePath.setLinearHeadingInterpolation(start ? postPush3.getHeading() : specimenScoring.getHeading(), intakePosition.getHeading(), 0.6);
        preIntakePath.setZeroPowerAccelerationMultiplier(3.0);
        PathChain intakePath = new PathChain(new Path(new BezierLine(
                new Point(start ? preIntake : smoothPreIntake),
                startIntake ? new Point(firstIntakePosition) : new Point(intakePosition)
        )));
        intakePath.getPath(0).setConstantHeadingInterpolation(intakePosition.getHeading());
        intakePath.getPath(0).setZeroPowerAccelerationMultiplier(2.0);
        intakePath.getPath(0).setPathEndTimeoutConstraint(500);
        intakePath.setCallbacks(
                new PathCallback(0.99, () -> {
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

        // path for the first cycle after pushing
        if (start == true) {
            return new SequentialCommandGroup(
                    new InstantCommand(() -> follower.setMaxPower(SLOW)),
                    new ParallelCommandGroup(
//                            retractIntake(),
                            start ? new WaitCommand(1000) : new ScoreSpecimenCommand(robot.outtake),
                            new InstantCommand(() -> {
                                follower.setHeadingPIDF(new CustomPIDFCoefficients(2, 0, 0.2, 0));
                            }),
                            new FollowPath(follower, preIntakePath, true)
                    ),
                    new InstantCommand(() -> follower.setMaxPower(SLOW)), // SLOW
                    new FollowPath(follower, intakePath, true),
                    new WaitCommand(100),
//                    new InstantCommand(() -> {
//                        robot.outtake.specimenClawClose();
//                    }),
//                    new WaitCommand(300),
                    new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),

                    new ParallelCommandGroup(
                            new ParallelRaceGroup(
                                    new WaitCommand(2500),
//                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
                            ).withTimeout(2500),
                            new SequentialCommandGroup(
                                new WaitCommand(200),
                                new ExtendSpecimenCommand(robot.outtake, true)
                            )
                    )
            );
        } else {
            // path on all other cycles
            return new SequentialCommandGroup(
                    new ParallelCommandGroup(
                            new ScoreSpecimenCommand(robot.outtake),
                            new FollowPath(follower, fastIntakePath)
                    ),
                    new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),

                    new ParallelCommandGroup(
                            new ParallelRaceGroup(
                                    new WaitCommand(2500),
//                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                    new FollowPath(follower, specimenScoringChain(intakePosition, xPos, start))
                                    new FollowPath(follower, fastSpecimenScoring(intakePosition, xPos, start))
                            ),
                            new SequentialCommandGroup(
                                    new WaitCommand(200),
                                    new ExtendSpecimenCommand(robot.outtake, true)
                            )
                    ),
                    new InstantCommand(() -> {
                        Log.d("Finished Specimen Scoring! : ", "" + xPos);
                    })
//                    new InstantCommand(() -> follower.setMaxPower(PUSH))
            );
        }

    }

    Pose fastIntakePosition = new Pose(-36, 60.5, Math.toRadians(0));
//    Pose fastIntakeControl = new Pose(0, 60.5, Math.toRadians(0));
//    public Command fastIntakeAndScore(double prevXPos, double xPos) {
//        PathChain intakePath = new PathChain(new Path(new BezierLine(
//                new Point(getChangedX(specimenScoring, prevXPos)),
//                new Point(fastIntakePosition)
//        )));
//        intakePath.getPath(0).setLinearHeadingInterpolation(specimenScoring.getHeading(),
//                intakePosition.getHeading());
//        intakePath.setCallbacks(new PathCallback(0.9, () -> {
//            robot.outtake.specimenClawClose();
//        }, PathCallback.PARAMETRIC, 0));
//
//        return new SequentialCommandGroup(
//                new ParallelCommandGroup(
//                        new ScoreSpecimenCommand(robot.outtake),
//                        new FollowPath(follower, intakePath, true)
//                ),
//                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//
//                new ParallelCommandGroup(
//                        new ParallelRaceGroup(
//                                new WaitCommand(2500),
////                                new InstantCommand(() -> follower.setMaxPower(FULLSPEED)),
//                                new FollowPath(follower, specimenScoringChain(fastIntakePosition, xPos))
//                        ),
//                        new ExtendSpecimenCommand(robot.outtake, true)
//                )
//        );
//    }

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