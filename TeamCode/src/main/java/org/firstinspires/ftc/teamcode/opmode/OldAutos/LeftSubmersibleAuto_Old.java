package org.firstinspires.ftc.teamcode.opmode.OldAutos;

import android.util.Log;

import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.FollowPathOld;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeAuto;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
import org.firstinspires.ftc.teamcode.pedroPathing.follower.Follower;
import org.firstinspires.ftc.teamcode.pedroPathing.localization.Pose;
import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierCurve;
import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.BezierLine;
import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Path;
import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;

@Disabled
@Autonomous(name = "Left Auto 0+8... maybe :) [old]", group = "Auto")
public class LeftSubmersibleAuto_Old extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private GamepadEx g1;

    //    Pose2d start = new Pose2d(-8.5 + 72.0, 65.0 + 72.0, Math.toRadians(-90));
    private double startHeading = Math.toRadians(-90.0);
    // After converting to pedro coordinate system, add 72.0 to both x and y
    // The center of the field is now (72, 72), not (0, 0)
    Point start = new Point(40.0 + 72, 65.0 + 72.0, Point.CARTESIAN);

    // The initial scoring position is adjusted for drift
    public static Point scoringInitial = new Point(57.0 + 72.0, 58.0 + 72.0, Point.CARTESIAN);
    public static Point scoring = new Point(58.0 + 72.0, 57.0 + 72.0, Point.CARTESIAN);
    public static double initialScoringHeading = Math.toRadians(-125.0);
    public static double scoringHeading = Math.toRadians(-125.0);

    private Point subIntake = new Point(24.0 + 72.0, 0.0 + 72.0, Point.CARTESIAN);
    private Point subIntake2 = new Point(24.0 + 72.0, 0.0 + 72.0, Point.CARTESIAN);

    public static Pose intake1 = new Pose(59.0 + 72.0, 56.0 + 72.0, Math.toRadians(-106));
    public static Pose intake2 = new Pose(60.0 + 72.0, 57.5 + 72.0, Math.toRadians(-87));
    public static Pose intake3 = new Pose(60.0 + 72.0, 55.0 + 72.0, Math.toRadians(-65));
    public static Pose envisioning = new Pose(36 + 72.0, 12 + 72.0, Math.toRadians(180));

    // the coordinates that the robot must offset from its position to get a yellow sample
    // unused vision coordinates (for now!)
    private static double submersibleXCoord = 0.0;
    private static double submersibleYCoord = 0.0;
    private static double cameraDistanceToIntake = 7.0;

    // used for setting up gamepad inputs
    boolean secondPosition = false;
    boolean readGamepad = false;
    long lastReadTime = 0;

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();

        follower = new Follower(hardwareMap);
        follower.setStartingPose(new Pose2d(start.getX(), start.getY(), startHeading));
        follower.setMaxPower(1.0);
        robot = new Robot(true, hardwareMap);

        // set the nominal voltage for the follower
//        FollowerConstants.nominalVoltage = 13.5;
        // I don't have enough time to test this :(
//        FollowerConstants.useVoltageCompensationInAuto = false;

        while (opModeInInit()) {
            // first do alliance selection
            // once start is pressed, the gamepad inputs will be read for sub intaking
            if (!readGamepad) {
                robot.allianceConfig(telemetry, gamepad1);
                if (gamepad1.start) {
                    readGamepad = true;
                }
            } else {
                readGamepadInputs();
            }
        }

        waitForStart();

        schedule(
                new SequentialCommandGroup(
                        scorePreload(),
                        intakeAndScore(intake1, true, true),
                        intakeAndScore(intake2, true, false),
                        // the third cycle needs more time for transfer as the robot is turning a lot more
                        intakeAndScore(intake3, false, false),
                        // Intake from sub

                        // 5th CYCLE -----------------------------------------------------
                        goToSub(subIntake),
                        new InstantCommand(() -> {
                            Log.d("Finished the path to go to sub!!", "TRUE");
                        }),
//                        new LimelightCommand(robot.intake, follower, envisioning,  false, false),
                        // by setting the slide offset to 0.0, the robot will not move the slides
                        // from where it was already pre-extended from the LimelightCommand
//                        new IntakeSubAuto(robot.intake, 0.0, follower),
                        scoreFromSub(subIntake),

                        // 6th CYCLE -----------------------------------------------------
                        goToSub(subIntake),
                        new InstantCommand(() -> {
                            Log.d("Finished the path to go to sub!!", "TRUE");
                        }),
//                        new LimelightCommand(robot.intake, follower, envisioning,  false, false),
//                        new IntakeSubAuto(robot.intake, 0.0, follower),
                        scoreFromSub(subIntake),

                        // 7th CYCLE -------------------------------------------------------
                        goToSub(subIntake),
                        new InstantCommand(() -> {
                            Log.d("Finished the path to go to sub!!", "TRUE");
                        }),
//                        new LimelightCommand(robot.intake, follower, envisioning,  false, false),
//                        new IntakeSubAuto(robot.intake, 0.0, follower),
                        scoreFromSub(subIntake),

                        // 8th CYCLE --------------------------------------------------------
                        // ... but it never has time for it 😭😭😭😭
                        lastCycle(),

                        park()
                )
        );
    }

    @Override
    public void run() {
        super.run();
//        Log.d("Follower ", "Heading error: " + Math.toDegrees(follower.getHeadingError()));
//        Log.d("Follower ", "Heading value: " + Math.toDegrees(follower.getTotalHeading()));
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
                start, scoring
        ));
        toScoring.setLinearHeadingInterpolation(startHeading, initialScoringHeading, 0.8);
        toScoring.setPathEndTimeoutConstraint(250);
        toScoring.setZeroPowerAccelerationMultiplier(2.5);

        return new ParallelCommandGroup(
                new FollowPathOld(follower, toScoring, true),
                // extend outtake slides
                new ExtendBucketCommand(robot.outtake, robot.intake, false),
                new SequentialCommandGroup(
                        new WaitCommand(400),
                        // begin to extend intake slides to prepare for intaking
                        new IntakeAuto.IntakeAutoPrep(robot.intake, false)
                )
        );
    }

    private Command intakeAndScore(Pose position, boolean fast, boolean first) {
        Path toIntake = new Path(new BezierLine(
                scoring,
                new Point(new Pose2d(position.getX(), position.getY(), position.getHeading()))
        ));
        toIntake.setLinearHeadingInterpolation(first ? initialScoringHeading : scoringHeading, position.getHeading(), 0.8);
        toIntake.setPathEndTimeoutConstraint(300);

        Path toScoring = new Path(new BezierLine(
                new Point(new Pose2d(position.getX(), position.getY(), position.getHeading())),
                scoring
        ));
        toScoring.setLinearHeadingInterpolation(position.getHeading(), scoringHeading, 0.8);
        toScoring.setPathEndTimeoutConstraint(300);
        toScoring.setZeroPowerAccelerationMultiplier(2.5);

        return new SequentialCommandGroup(
                // Score the sample & go to intake at once
                new ParallelCommandGroup(
                        first ? new ScoreBucketCommand(robot.outtake, robot.intake, true, true) : new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new SequentialCommandGroup(
                                new WaitCommand(200),
                                new ParallelCommandGroup(
                                        new FollowPathOld(follower, toIntake, true),
                                        new IntakeAuto(robot.intake, robot.outtake, fast ? 1200 : 2000)
                                )
                        )
                ),

                new InstantCommand(() -> {
                    Log.d("Finished intaking", "True");
                }),

                // Score the sample
                new ParallelCommandGroup(
                        new FollowPathOld(follower, toScoring, true),
                        // I HAVE INCREASED THE SCORING HEADING BECAUSE HOLD POINT DOESN'T TURN FAR ENOUGH
//                        new InstantCommand(() -> {
//                            follower.holdPoint(new BezierPoint(new Point(scoring.getX(), scoring.getY(), Point.CARTESIAN)),
//                                    scoringHeading + Math.toRadians(30));
//                        }),
                        new SequentialCommandGroup(
                                fast ? new IntakeRetractCommand(robot.intake, robot.outtake, true)
                                        : new IntakeRetractCommand(robot.intake, robot.outtake, false),
                                new ParallelCommandGroup(
                                        // extend the outtake slides to prepare for scoring
                                        new ExtendBucketCommand(robot.outtake, true),
                                        new SequentialCommandGroup(
                                                new WaitCommand(150),
                                                new IntakeAuto.IntakeAutoPrep(robot.intake, false)
                                        )
                                )
                        )
                )
        );
    }

    //    Point intaking = new Point(24.0 + 72.0, 9 + 72.0, Point.CARTESIAN);
    double intakingHeading = Math.toRadians(180);

    // drive to envisioning point
    public Command goToSub(Point intaking) {
        // Go right in front of the sub to do vision
        Path toVision = new Path(new BezierCurve(
                scoring,
                parkingControl,
                new Point(new Pose2d(envisioning.getX(), envisioning.getY(), envisioning.getHeading()))
        ));
        toVision.setLinearHeadingInterpolation(scoringHeading, intakingHeading, 0.8);
        toVision.setZeroPowerAccelerationMultiplier(2.0);
//        toVision.setPathEndVelocityConstraint(5.0);
        toVision.setPathEndTimeoutConstraint(500);
        toVision.setPathEndTValueConstraint(1.0);

        return new SequentialCommandGroup(
                new ParallelCommandGroup(
                        new ScoreBucketCommand(robot.outtake, robot.intake, true, false),
                        new SequentialCommandGroup(
                                new WaitCommand(200),
                                new FollowPathOld(follower, toVision, true),
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
        Path toScoring = new Path(new BezierCurve(
                new Point(new Pose2d(envisioning.getX(), envisioning.getY(), envisioning.getHeading())),
                parkingControl,
                scoring
        ));
        toScoring.setLinearHeadingInterpolation(intakingHeading, scoringHeading, 0.8);
        toScoring.setPathEndTimeoutConstraint(100);
//        toScoring.setPathEndTValueConstraint(0.95);
        toScoring.setZeroPowerAccelerationMultiplier(2.5);

        return new ParallelCommandGroup(
                new InstantCommand(() -> follower.setMaxPower(1.0)),
                new InstantCommand(robot.intake::sweeperStored),
                new SequentialCommandGroup(
                        new WaitCommand(300), // Let transfer start
                        new FollowPathOld(follower, toScoring, true)
                ),
                new SequentialCommandGroup(
                        new IntakeRetractCommand(robot.intake, robot.outtake, false),
                        new ExtendBucketCommand(robot.outtake, true)
//                new WaitCommand(200)
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
                        new FollowPathOld(follower, toIntake, true),
                        new IntakeAuto(robot.intake, robot.outtake, 1500)
                ),
                new ParallelCommandGroup(
                        new FollowPathOld(follower, toScoring, true),
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

    Point parkingControl = new Point(50 + 72.0, 12 + 72.0, Point.CARTESIAN);
    Point parking = new Point(21 + 72.0, 9 + 72.0, Point.CARTESIAN);
    double parkingHeading = Math.toRadians(0);

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
                        new FollowPathOld(follower, toParking, true)
//                        new InstantCommand(robot.outtake::outtakeParking)
                ),
                new ScoreBucketCommand(robot.outtake, robot.intake, true, false)
        );
    }
}

