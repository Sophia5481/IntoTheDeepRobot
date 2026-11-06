package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.arcrobotics.ftclib.command.CommandOpMode;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeBeginCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ExtendSpecimenCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.GiveToHP;
import org.firstinspires.ftc.teamcode.commands.outtake.ResetOuttake;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreBucketCommand;
import org.firstinspires.ftc.teamcode.commands.outtake.ScoreSpecimenCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;

import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.pedropathing.follower.Follower;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.button.GamepadButton;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.localization.Pose;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.Range;

import java.util.concurrent.TimeUnit;

@TeleOp(name = "Manual Drive", group = "Test")
public class ManualDrive extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private GamepadEx g1, g2;
    private double intakeExtension;
    public static double isPushingSamples;
    private Timing.Timer timer;

    @Override
    public void initialize() {
        telemetry.addLine("Ready!");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        FollowerConstants.useBrakeModeInTeleOp = true;
        follower.startTeleopDrive();

        robot = new Robot(false, hardwareMap);
        timer = new Timing.Timer(120, TimeUnit.SECONDS);

        g1 = new GamepadEx(gamepad1);
        g2 = new GamepadEx(gamepad2);

        intakeExtension = 0;

        while (opModeInInit()) {
            robot.allianceConfig(telemetry, gamepad1);
        }

        telemetry.addLine("Ready!");
        telemetry.update();

        waitForStart();
        timer.start();
        robot.startTele();

        // Intake commands (G1): toggle a to intake & outtake, b to dump, start to reverse, DPAD down to clear up the area, DPAD up to extend all the way
//        new GamepadButton(g1, GamepadKeys.Button.A)
//                .toggleWhenPressed(new SequentialCommandGroup(new WaitCommand(400),
//                        new IntakeCommand(robot.intake, robot.outtake, false, false)))
////                        new IntakeRetractionCommand(robot.intake, robot.outtake), true)
//                .whenPressed(() -> intakeExtension = 0);
        // a to extend slides to prepare for intaking
        new GamepadButton(g1, GamepadKeys.Button.A)
                .whenPressed(new SequentialCommandGroup(
                        new InstantCommand(() -> {
                            // using the variable intakeExtension doesn't seem to work
                            intakeExtension = 1.0;
                            Log.d("intakeExtension:", "" + intakeExtension);
                        }),
                        new IntakeBeginCommand(robot.intake, 1.0)));
        // b to transfer sample
        new GamepadButton(g1, GamepadKeys.Button.B)
                .whenPressed(new IntakeRetractCommand(robot.intake, robot.outtake, false));

//        new GamepadButton(g1, GamepadKeys.Button.DPAD_UP)
//                .whenPressed(new InstantCommand(robot.intake::slideOut));
        // DPAD left to give to HP zone
        new GamepadButton(g1, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(new GiveToHP(robot.outtake, false));
        // start to toggle between reverse intake and intkae
        new GamepadButton(g1, GamepadKeys.Button.START)
                .toggleWhenPressed(new SequentialCommandGroup(
                        new InstantCommand(() -> {
                            robot.intake.INTAKING = false;
                            robot.intake.intakeReverse();
                            robot.intake.wristReverse();
                        }),
                        new WaitCommand(700),
                        new InstantCommand(() -> {
                            robot.intake.INTAKING = true;
                            robot.intake.intakeOn();
                            robot.intake.wristIntake();
                        })));
        // back to toggle sweeper
        new GamepadButton(g1, GamepadKeys.Button.BACK)
                .whenPressed(new SequentialCommandGroup(new InstantCommand(robot.intake::sweeperOut),
                        new WaitCommand(500), new InstantCommand(robot.intake::sweeperStored)));
        // dpad up to raise intake wrist to solve potential jams
        new GamepadButton(g1, GamepadKeys.Button.DPAD_UP)
                .whenPressed(new InstantCommand(robot.intake::wristPreIntake));
        // dpad down to lower intake wrist and intake samples
        new GamepadButton(g1, GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(new InstantCommand(() -> {
                    robot.intake.wristIntake(intakeExtension);
                    robot.intake.intakeOn();
                }));
        // dpad right to trigger auto transfer
        new GamepadButton(g1, GamepadKeys.Button.DPAD_RIGHT)
                .whenPressed(new ConditionalCommand(
                        new InstantCommand(() -> robot.intake.AUTOTRANSFER = robot.outtake),
                        new InstantCommand(() -> robot.intake.AUTOTRANSFER = null),
                        () -> robot.intake.AUTOTRANSFER == null
                ));

        new GamepadButton(g1, GamepadKeys.Button.LEFT_STICK_BUTTON)
                .whenPressed(
                        new InstantCommand(() -> {
                            Pose temp = follower.getPose();
                            follower.setPose(new Pose(temp.getX(), temp.getY(), 0));
                        }));

        // Outtake commands (G1): y to outtake high, x to score
        new GamepadButton(g1, GamepadKeys.Button.Y)
                // temporary testing: remove the robot.intake when done
                .whenPressed(new ExtendBucketCommand(robot.outtake, true, true));
        new GamepadButton(g1, GamepadKeys.Button.X)
                // just for testing rn
                .whenPressed(new ScoreBucketCommand(robot.outtake, robot.intake, false, false));

        // Specimen commands (G2): a to close/open claw, b to give to human player, y to outtake, x to score
        new GamepadButton(g2, GamepadKeys.Button.A)
                .whenPressed(new ConditionalCommand(new InstantCommand(robot.outtake::specimenClawClose), new InstantCommand(robot.outtake::specimenClawOpen), robot.outtake::isClawOpen));
        new GamepadButton(g2, GamepadKeys.Button.B)
                .whenPressed(new ResetOuttake(robot.outtake, robot.intake));
        new GamepadButton(g2, GamepadKeys.Button.Y)
                .whenPressed(new ExtendSpecimenCommand(robot.outtake, true));
        new GamepadButton(g2, GamepadKeys.Button.X)
                .whenPressed(new ScoreSpecimenCommand(robot.outtake));

        // Outtake commands (G2): right bumper
        new GamepadButton(g2, GamepadKeys.Button.RIGHT_BUMPER)
                .whenPressed(new ExtendBucketCommand(robot.outtake, false));

        new GamepadButton(g2, GamepadKeys.Button.LEFT_BUMPER)
                .whenPressed(new ExtendBucketCommand(robot.outtake, false));
        // Intake commands (G2): dpad up to raise intake and retract, dpad down to lower it
//        new GamepadButton(g2, GamepadKeys.Button.DPAD_UP)
//                .whenPressed(new SequentialCommandGroup(
//                        new InstantCommand(robot.intake::wristPreIntake),
//                        new WaitCommand(300),
//                        new ParallelCommandGroup(
//                                new InstantCommand(robot.intake::slideIn),
//                                new SequentialCommandGroup(
//                                        new InstantCommand(robot.intake::intakeReverse),
//                                        new WaitCommand(50),
//                                        new InstantCommand(robot.intake::intakeOn)
//                                )
//                        ),
//                        new InstantCommand(robot.intake::intakeOff)
//                ));
        new GamepadButton(g2, GamepadKeys.Button.DPAD_UP)
                .whenPressed(
                        robot.outtake.increaseSlideLayer()
                );

        new GamepadButton(g2, GamepadKeys.Button.DPAD_DOWN)
                .whenPressed(
                        new SequentialCommandGroup(
                                new ParallelCommandGroup(
                                        new InstantCommand(robot.outtake::specimenClawClose),
                                        new ScoreBucketCommand(robot.outtake)
                                ),
                                new WaitCommand(200),
                                new ExtendSpecimenCommand(robot.outtake, true)
                        )
                );

//        new GamepadButton(g2, GamepadKeys.Button.DPAD_DOWN)
//                .whenPressed(new SequentialCommandGroup(
//                        new InstantCommand(() -> {
//                            robot.intake.INTAKING = false;
//                        }),
//                        new InstantCommand(robot.intake::wristReverse),
//                        new WaitCommand(100),
//                        new InstantCommand(robot.intake::intakeReverse),
//                        new WaitCommand(500),
//                        new InstantCommand(robot.intake::intakeOff)
//                ));

        new GamepadButton(g2, GamepadKeys.Button.DPAD_LEFT)
                .whenPressed(
                        new SequentialCommandGroup(
                                new IntakeRetractCommand(robot.intake, robot.outtake, false),
                                new GiveToHP(robot.outtake, false)
                        )
                );

        // Other commands (G2): back to hang, start to reset
        new GamepadButton(g2, GamepadKeys.Button.BACK)
                .toggleWhenPressed(new InstantCommand(robot.intake::hangExtended), new InstantCommand(() -> {
                    robot.intake.hangHanging();
                    robot.outtake.specimenClawClose();
                    robot.outtake.specimenArmVertical();
                }));
        new GamepadButton(g2, GamepadKeys.Button.START)
                .whenPressed(new InstantCommand(robot::home));

        telemetry.addLine("Ready");
        telemetry.update();
    }

    long lastUpdate = System.currentTimeMillis();
    @Override
    public void run() {
        super.run();

        double slowmode = 1.0;
        if (robot.intake.slideIsOut()) {
            slowmode = 0.75;
        }

        intakeExtension -= gamepad1.right_stick_y * 0.05;
        intakeExtension = Range.clip(intakeExtension, 0, 1);

        Log.d("intakeExtension:", "" + intakeExtension);
        if (Math.abs(gamepad1.right_stick_y) > 0.01 && robot.intake.INTAKING) {
            Log.d("intakeExtension:", "" + intakeExtension);
            robot.intake.slidePos(intakeExtension);
        }

        // Drive
        double turn = (gamepad1.left_trigger - gamepad1.right_trigger) * 0.3;
        if (gamepad1.left_bumper) {
            turn += 0.05;
        }
        if (gamepad1.right_bumper) {
            turn -= 0.05;
        }
        follower.setTeleOpMovementVectors(-gamepad1.left_stick_x*slowmode, -gamepad1.left_stick_y*slowmode, turn, false);
        follower.update();

        // Timer-based notifications
        robot.intake.sampleIntaked();
        if (timer.remainingTime() > 30) {
            robot.intake.noDisplayColor = false;
        } else if (timer.remainingTime() < 30 && timer.remainingTime() >= 29) {
            gamepad1.rumbleBlips(4);
        } else if (timer.remainingTime() < 29 && timer.remainingTime() >= 25) {
            robot.intake.noDisplayColor = true;
            robot.intake.displayColor(1.0);
        } else if (timer.remainingTime() < 25 && timer.remainingTime() >= 8) {
            robot.intake.noDisplayColor = false;
        } else if (timer.remainingTime() < 8 && timer.remainingTime() >= 7) {
            gamepad2.rumbleBlips(4);
        } else if (timer.remainingTime() < 7 && timer.remainingTime() >= 0) {
            robot.intake.noDisplayColor = true;
            robot.intake.displayColor(1.0);
        } else if (timer.remainingTime() < 0) {
            robot.intake.noDisplayColor = false;
        }

        telemetry.addData("Loop Rate (Hz)", 1000.0/(double)(System.currentTimeMillis() - lastUpdate));
        telemetry.addData("Time Left (s)", timer.remainingTime());
        lastUpdate = System.currentTimeMillis();
        telemetry.update();

        robot.resetCache();
    }
}
