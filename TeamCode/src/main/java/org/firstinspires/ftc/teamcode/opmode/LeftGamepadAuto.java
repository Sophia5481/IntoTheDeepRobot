package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.pedropathing.follower.Follower;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.CustomPIDFCoefficients;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.ManualInput;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.auto.BlindSubIntake;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeSubAuto;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.L1HangCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.BezierPoint;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathCallback;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.follower.FollowerConstants;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Gamepad;
//import org.firstinspires.ftc.teamcode.pedroPathing.util.CustomPIDFCoefficients;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;
import org.firstinspires.ftc.teamcode.utils.Logger;

@Autonomous(name = "Left Auto 0+6 Gamepad", group = "Auto")
public class LeftGamepadAuto extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private Gamepad g1;
    private ManualInput manualInput;
    private Telemetry telem;

    //    Pose2d start = new Pose2d(-8.5 + 72.0, 65.0 + 72.0, Math.toRadians(-90));
    private double startHeading = Math.toRadians(-90.0);
    // After converting to pedro coordinate system, add 72.0 to both x and y
    // The center of the field is now (72, 72), not (0, 0)
    Point start = new Point(40.0 + 72, 65.0 + 72.0, Point.CARTESIAN);

    // The initial scoring position is adjusted for drift
    public static Point scoringInitial = new Point(56.0 + 72.0, 57.0 + 72.0, Point.CARTESIAN);
    public static Point scoring = new Point(57.0 + 72.0, 56.0 + 72.0, Point.CARTESIAN);
    //    public static Point scoring_far = new Point (59.0 + 72.0, 58.0 + 72.0, Point.CARTESIAN);
    public static Point scoringFromSub = new Point(58.0 + 72.0, 57.0 + 72.0, Point.CARTESIAN);
    public static double initialScoringHeading = Math.toRadians(-125.0);
    public static double scoringHeading = Math.toRadians(-125.0);

    private Point subIntake = new Point(24.0 + 72.0, 0.0 + 72.0, Point.CARTESIAN);
    private Point subIntake2 = new Point(24.0 + 72.0, 0.0 + 72.0, Point.CARTESIAN);

    public static Pose intake1 = new Pose(59.0 + 72.0, 57.0 + 72.0, Math.toRadians(-106));
    public static Pose intake2 = new Pose(60.0 + 72.0, 57.5 + 72.0, Math.toRadians(-90));
    public static Pose intake3 = new Pose(60.0 + 72.0, 56.0 + 72.0, Math.toRadians(-67));
    public static Pose envisioning_og = new Pose(36 + 72.0, 12 + 72.0, Math.toRadians(180));

    // used for setting up gamepad inputs
    boolean secondPosition = false;
    boolean readGamepad = false;
    long lastReadTime = 0;

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(new Pose(start.getX(), start.getY(), startHeading));
        manualInput = new ManualInput(telem, 3, false);
        follower.updatePose();
        follower.setMaxPower(1.0);
        robot = new Robot(true, hardwareMap);

        while (opModeInInit()) {
            // first do alliance selection
            // once start is pressed, the gamepad inputs will be read for sub intaking
                robot.allianceConfig(telemetry, gamepad1);
//                manualInput.update(g1);
        }

        waitForStart();

        schedule(
                new SequentialCommandGroup(
                        new InstantCommand(() -> follower.setMaxPower(0.7)),
                        scorePreload(),
                        new InstantCommand(() -> {
                            follower.setHeadingPIDF(new CustomPIDFCoefficients(2, 0, 0.2, 0));
                        }),
                        intakeAndScore(intake1, true, true, false),
                        intakeAndScore(intake2, true, false, false),
                        // the third cycle needs more time for transfer as the robot is turning a lot more
                        intakeAndScore(intake3, false, false, true),

                        new InstantCommand(() -> {
                            follower.setHeadingPIDF(new CustomPIDFCoefficients(1, 0, 0.2, 0));
                        }),
                        // Intake from sub

                        new InstantCommand(() -> follower.setMaxPower(1.0)),
                        // 5th CYCLE -----------------------------------------------------
                        goToSub(false, /* manualInput.getManualPoses().get(0).getPose().copy() */  new Pose(24.0 + 72.0, 9 + 72.0, Math.toRadians(180))),
                        new InstantCommand(() -> {
                            Log.d("Limelight", "position it thinks it's at is " + follower.getPose().toString());
                        }),
                        new InstantCommand(() -> {
                            Log.d("Finished the path to go to sub!!", "TRUE");
                        }),
                        new WaitCommand(200),
                        new BlindSubIntake(robot.intake, follower, 0),
                        scoreFromSub(subIntake),

                        // 6th CYCLE -----------------------------------------------------
                        goToSub(false, /* manualInput.getManualPoses().get(1).getPose().copy() */  new Pose(24.0 + 72.0, 9 + 72.0, Math.toRadians(180))),

                        new InstantCommand(() -> {
                            Log.d("Limelight", "position it thinks it's at is " + follower.getPose().toString());
                        }),
                        new InstantCommand(() -> {
                            Log.d("Finished the path to go to sub!!", "TRUE");
                        }),
                        new WaitCommand(200),
                        new BlindSubIntake(robot.intake, follower, 0),
                        scoreFromSub(subIntake),

//                        // 7th CYCLE -------------------------------------------------------
//                goToSub(false, /*manualInput.getManualPoses().get(2).getPose().copy()*/, ),
//                new InstantCommand(() -> {
//                    Log.d("Limelight", "position it thinks it's at is " + follower.getPose().toString());
//                }),
//                new InstantCommand(() -> {
//                    Log.d("Finished the path to go to sub!!", "TRUE");
//                }),
//                new WaitCommand(200),
//                new BlindSubIntake(robot.intake, follower, 0),
//                scoreFromSub(subIntake),

                        // 8th CYCLE --------------------------------------------------------
                        // ... but it never has time for it 😭😭😭😭
//                lastCycle(),

                        park()
                )
        );
    }

    @Override
    public void run() {
        super.run();
        Log.d("Follower ", "Heading error: " + Math.toDegrees(follower.getHeadingError()));
        Log.d("Follower ", "Heading value: " + Math.toDegrees(follower.getTotalHeading()));
        Log.d("Follower ", "Pose: " + follower.getPose().toString());
        follower.drawOnDashBoard();
        follower.update();
        robot.resetCache();
    }

    // Gamepad input process:
    // Press the left and right bumper to adjust the intaking position by 1 inch
    // When done, press start. This will update the intaking position for the 5th and 6th cycle
    // Then, repeat the process, the only difference is that it'll update the 7th cycle
    private void readGamepadInputs() {
//        telemetry.addLine("In gamepad input loop");
//        telemetry.update();
        if (gamepad1.dpad_left && !secondPosition && System.currentTimeMillis() - lastReadTime > 200) {
//                subIntake = addYToIt(subIntake, -1.0);
            subIntake.setCoordinates(subIntake.getX(), (subIntake.getY() - 1.0), Point.CARTESIAN);
            Log.d("Reached setting coordinates: ", "True");
            Log.d("Sub Y Coord: ", "" + subIntake.getY());
            telemetry.addLine("Sub Y Coord #1:" + subIntake.getY());
            Log.d("Time: ", "" + System.currentTimeMillis() + " Last: " + lastReadTime);
            telemetry.update();
            gamepad1.dpad_left = false;
            lastReadTime = System.currentTimeMillis();
        }
        if (gamepad1.dpad_right && !secondPosition && System.currentTimeMillis() - lastReadTime > 200) {
//                subIntake = addYToIt(subIntake, 1.0);
            subIntake.setCoordinates(subIntake.getX(), (subIntake.getY() + 1.0), Point.CARTESIAN);
            Log.d("Reached setting coordinates: ", "True");
            Log.d("Sub Y Coord: ", "" + subIntake.getY());
            telemetry.addLine("Sub Y Coord #1:" + subIntake.getY());
            Log.d("Time: ", "" + System.currentTimeMillis() + " Last: " + lastReadTime);
            telemetry.update();
            gamepad1.dpad_right = false;
            lastReadTime = System.currentTimeMillis();
        }
//        telemetry.addLine("Out of gamepad input loop 1");
        if (gamepad1.dpad_left && secondPosition && System.currentTimeMillis() - lastReadTime > 200) {
//                subIntake2 = addYToIt(subIntake2, -1.0);
            subIntake2.setCoordinates(subIntake2.getX(), (subIntake2.getY() - 1.0), Point.CARTESIAN);
            Log.d("Reached setting coordinates: ",
                    "True");
            Log.d("Sub 2 Y Coord: ", "" + subIntake2.getY());
            telemetry.addLine("Sub Y Coord #2:" + subIntake2.getY());
            Log.d("Time: ", "" + System.currentTimeMillis() + " Last: " + lastReadTime);
            telemetry.update();
            gamepad1.dpad_left = false;
            lastReadTime = System.currentTimeMillis();
        }
        if (gamepad1.dpad_right && secondPosition && System.currentTimeMillis() - lastReadTime > 200) {
//                subIntake2 = addYToIt(subIntake2, 1.0);
            subIntake2.setCoordinates(subIntake2.getX(), (subIntake2.getY() + 1.0), Point.CARTESIAN);
            Log.d("Reached setting coordinates: ", "True");
            Log.d("Sub 2 Y Coord: ", "" + subIntake2.getY());
            telemetry.addLine("Sub Y Coord #2:" + subIntake2.getY());
            Log.d("Time: ", "" + System.currentTimeMillis() + " Last: " + lastReadTime);
            telemetry.update();
            gamepad1.dpad_right = false;
            lastReadTime = System.currentTimeMillis();
            ;
        }
//        telemetry.addLine("Out of gamepad input loop");
//        telemetry.update();
        if (gamepad1.back) {
            secondPosition = true;
        }
    }

    private Command scorePreload() {
        Path toScoring = new Path(new BezierLine(
                start, scoringInitial
        ));
        toScoring.setLinearHeadingInterpolation(startHeading, initialScoringHeading, 0.8);
        toScoring.setPathEndTimeoutConstraint(300);
        toScoring.setZeroPowerAccelerationMultiplier(2.0);

        return new ParallelCommandGroup(
                new FollowPath(follower, toScoring, true),
                // extend outtake slides
                new SequentialCommandGroup(
                        new ExtendBucketCommand(robot.outtake, true),
                        new WaitCommand(500)
                ),
                new SequentialCommandGroup(
                        new WaitCommand(400),
                        // begin to extend intake slides to prepare for intaking
                        new IntakeAuto.IntakeAutoPrep(robot.intake, false)
                )
        );
    }

    private Command intakeAndScore(Pose position, boolean fast, boolean first, boolean last) {
        Path toIntake = new Path(new BezierLine(
                first ? scoringInitial : scoring,
                new Point(position)
        ));
        toIntake.setLinearHeadingInterpolation(first ? initialScoringHeading : scoringHeading, position.getHeading(), 0.8);
        toIntake.setZeroPowerAccelerationMultiplier(2.0);
//        toIntake.setPathEndTimeoutConstraint(100);

        Path toScoring = new Path(new BezierLine(
                new Point(position),
                scoring
        ));
        toScoring.setLinearHeadingInterpolation(position.getHeading(), scoringHeading, 0.8);
//        toScoring.setPathEndTimeoutConstraint(100);
        toScoring.setZeroPowerAccelerationMultiplier(2.0);

        return new SequentialCommandGroup(
                // Score the sample & go to intake at once
                new ParallelCommandGroup(
                        // I changed all scoring to use the same method
                        new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new SequentialCommandGroup(
                                new WaitCommand(200),
                                new ParallelCommandGroup(
                                        new FollowPath(follower, toIntake, true),
                                        new IntakeAuto(robot.intake, robot.outtake, fast ? 1500 : 2000)
                                )
                        )
                ),

                new InstantCommand(() -> {
                    Log.d("Follower: ", "in conditional command");
                }),

                new ConditionalCommand(
                        new InstantCommand(),
                        tryAgain(last),
                        () -> robot.intake.sampleIntaked()
                ),

                new InstantCommand(() -> {
                    Log.d("Finished intaking", "True");
                }),

                // Score the sample
                new ParallelCommandGroup(
                        new FollowPath(follower, toScoring, true),
                        new SequentialCommandGroup(
                                fast ? new IntakeRetractCommand(robot.intake, robot.outtake, true)
                                        : new IntakeRetractCommand(robot.intake, robot.outtake, false),
                                new ParallelCommandGroup(
                                        // extend the outtake slides to prepare for scoring
                                        new ExtendBucketCommand(robot.outtake,true),
                                        new SequentialCommandGroup(
                                                new WaitCommand(150),
                                                last ? new InstantCommand() : new IntakeAuto.IntakeAutoPrep(robot.intake, false)
                                        )
                                )
                        )
                )
        );
    }

    // tries to intake sample again if it didn't get one 😔
    private Command tryAgain(boolean lastSample) {
        Pose failedPose = follower.getPose();
        Path retry;
        Path retry2;
        if (lastSample) {
            // the sample close to the wall often gets pinned between the intake and the wall, so move backwards if that happens
            double retryRadius = intake3.getHeading() - Math.toRadians(5);
            retry = new Path(new BezierLine(
                    new Point(intake3),
                    new Point(new Pose(intake3.getX(), intake3.getY() + 2.0, retryRadius))
            ));
            retry2 = new Path(new BezierLine(
                    new Point(new Pose(intake3.getX(), intake3.getY() + 2.0, retryRadius)),
                    new Point(new Pose(intake3.getX(), intake3.getY() - 2.0, retryRadius))
            ));
            retry.setLinearHeadingInterpolation(failedPose.getHeading(), retryRadius);
            retry2.setConstantHeadingInterpolation(retryRadius);
            return new SequentialCommandGroup(
                    new InstantCommand(() -> {
                        Logger.d("Failed heading: ", failedPose.toString());
                        Logger.d("Failed heading: ", "x val" + failedPose.getX());
                        Logger.d("Failed heading: ", "y val" + failedPose.getY());
                        Logger.d("Failed heading: ", "normal:" + failedPose.getHeading());
                        Logger.d("Failed heading: ", "degrees:" + Math.toDegrees(failedPose.getHeading()));
                        Logger.d("Failed heading: ", "radians:" + Math.toRadians(failedPose.getHeading()));
                    }),
                    new FollowPath(follower, retry, true),
                    new IntakeAuto(robot.intake, 500, 1.0),
                    new FollowPath(follower, retry2, true),
                    new IntakeAuto(robot.intake, 500, 1.0)
            );
        } else {
            return new org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup(
                    new InstantCommand(() -> {
                        Logger.d("Failed heading: ", "Pose: " + failedPose.toString());
                        Logger.d("Failed heading: ", "normal:" + failedPose.getHeading());
                        Logger.d("Failed heading: ", "degrees:" + Math.toDegrees(failedPose.getHeading()));
                        Logger.d("Failed heading: ", "radians:" + Math.toRadians(failedPose.getHeading()));
                        Logger.d("Failed heading: ", "Follower's heading: " + Math.toDegrees(follower.getTotalHeading()));
                    }),
                    new InstantCommand(() -> {
                        follower.turn(Math.toRadians(25), false);
                    }),
                    new IntakeAuto(robot.intake, 500, 1.0),
                    new InstantCommand(() -> {
                        follower.turn(Math.toRadians(50), true);
                    }),
                    new IntakeAuto(robot.intake, 500, 1.0)
            );
        }

    }

    //    Point intaking = new Point(24.0 + 72.0, 9 + 72.0, Point.CARTESIAN);
    double intakingHeading = Math.toRadians(180);

    // drive to envisioning point
    public Command goToSub(boolean first, Pose intake) {
        // Go right in front of the sub to do vision
        Path toVision = new Path(new BezierCurve(
                first ? scoring : scoringFromSub,
                parkingControl,
                new Point(intake)
        ));
        toVision.setLinearHeadingInterpolation(scoringHeading, intakingHeading, 0.5);
        toVision.setZeroPowerAccelerationMultiplier(1.25);
//        toVision.setPathEndVelocityConstraint(5.0);
//        toVision.setPathEndTimeoutConstraint(100);

        return new SequentialCommandGroup(
                new ParallelCommandGroup(
                        first ? new ScoreBucketCommand(robot.outtake, robot.intake, true, false) : new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new IntakeAuto.IntakeAutoPrep(robot.intake, true),
                        new SequentialCommandGroup(
                                new WaitCommand(100),
                                new FollowPath(follower, toVision, true),
                                new InstantCommand(() -> {
                                    Log.d("Followed the path :)", "YAY!");
                                })
                        )
                )
        );
    }

    public Command scoreFromSub(Point intaking) {
        // since we never know before hand where the robot will intake from sub,
        // we just take what the localization thinks the robot is

        follower.setMaxPower(1.0);
        Path toScoring = new Path(new BezierCurve(
                new Point(follower.getPose()),
                parkingControl,
                scoringFromSub
        ));
        toScoring.setLinearHeadingInterpolation(intakingHeading, scoringHeading, 0.8);
        toScoring.setPathEndTimeoutConstraint(50);
        toScoring.setPathEndVelocityConstraint(3);
//        toScoring.setPathEndTValueConstraint(0.95);
        toScoring.setZeroPowerAccelerationMultiplier(3.5);

        return new ParallelCommandGroup(
                new InstantCommand(() -> follower.setMaxPower(1.0)),
                new InstantCommand(robot.intake::sweeperStored),
                new SequentialCommandGroup(
                        new WaitCommand(300), // Let transfer start
                        new FollowPath(follower, toScoring, true)
                ),
                new SequentialCommandGroup(
                        new IntakeRetractCommand(robot.intake, robot.outtake, false),
                        new ExtendBucketCommand(robot.outtake, true),
                        new WaitCommand(400)
                )
        );
    }

    Point intake8 = new Point(30.0 + 72.0, 60.0 + 72.0, Point.CARTESIAN);

    public Command lastCycle() {
        Path toIntake = new Path(new BezierCurve(
                scoring,
                intake8
        ));
        toIntake.setLinearHeadingInterpolation(scoringHeading, intakingHeading, 0.8);

        Path toScoring = new Path(new BezierCurve(
                intake8,
                scoring
        ));
        toScoring.setLinearHeadingInterpolation(intakingHeading, scoringHeading, 0.8);

        return new SequentialCommandGroup(
                new ParallelCommandGroup(
                        new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new IntakeAuto.IntakeAutoPrep(robot.intake, false)
                ),
                new WaitCommand(150),
                new ParallelCommandGroup(
                        new FollowPath(follower, toIntake, true),
                        new IntakeAuto(robot.intake, robot.outtake, 1500)
                ),
                new ParallelCommandGroup(
                        new FollowPath(follower, toScoring, true),
                        new SequentialCommandGroup(
                                new IntakeRetractCommand(robot.intake, robot.outtake, true),
                                new ExtendBucketCommand(robot.outtake, true)
//                                new WaitCommand(200)
                        )
                )
        );
    }

    Point addYToIt(Point p, double y) {
        Log.d("Going to submersible at this y coord:", " " + (p.getY() + y));
        Log.d("OG Y Coordinate:", Double.toString(p.getY()));
        Log.d("And the Y Add-on:", Double.toString(y));
        return new Point(p.getX(), (p.getY() + y), Point.CARTESIAN);
    }

    Point parkingControl = new Point(40 + 72.0, 12 + 72.0, Point.CARTESIAN);
    Point parking = new Point(21 + 72.0, 9 + 72.0, Point.CARTESIAN);
    double parkingHeading = Math.toRadians(180);

    private Command park() {
        Path toParking = new Path(new BezierCurve(
                scoring,
                parkingControl,
                parking
        ));
        toParking.setLinearHeadingInterpolation(scoringHeading, parkingHeading, 0.8);
        toParking.setPathEndVelocityConstraint(1);
        toParking.setZeroPowerAccelerationMultiplier(2);

        return new ParallelCommandGroup(
                new SequentialCommandGroup(
                        new WaitCommand(300),
                        new InstantCommand(() -> {
                            follower.setMaxPower(0.75);
                        }),
                        new FollowPath(follower, toParking, true)
                ),
                new SequentialCommandGroup(
                        new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new L1HangCommand(robot.outtake)
                )
        );
    }
}
