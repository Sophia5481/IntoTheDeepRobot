package org.firstinspires.ftc.teamcode.commands.auto;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;

import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class BlindSubIntake extends SequentialCommandGroup {
    public static boolean gotSample = false;
    public static int retries = 2;
    public BlindSubIntake (Intake intake, Follower follower, int level) {
        addRequirements(intake);
        addCommands(
                new InstantCommand(() -> gotSample = false),
                new InstantCommand(() -> intake.slidePos(0.2)),
                new InstantCommand(intake::wristIntake),
                new InstantCommand(intake::intakeOn),
                new WaitCommand(400),
                new WaitUntilCommand(intake::sampleIntaked).withTimeout(1000),

                // Fail safes
                new InstantCommand(intake::updateSampleColor),
                (level == retries) ? new InstantCommand() : new ConditionalCommand(
                        new SequentialCommandGroup(
                                // if the intake gets the wrong color then reverse it out
                                // if it doesn't get a sample then move the slides to try to get it in -- most of the time it's just a little bit off
                                new ConditionalCommand(new IntakeReverseCommand(intake, false),
                                        new SequentialCommandGroup(
                                                new InstantCommand(() -> intake.slidePos(intake.getSlidePos() - 0.2)),
                                                new WaitCommand(200),
                                                new InstantCommand(() -> intake.slidePos(intake.getSlidePos() + 0.4)),
                                                new WaitCommand(300)
                                        ),
                                        intake::needReverse
                                        /* () -> intake.needReverse() || intake.isJammed() */),
                                new ConditionalCommand(
                                        new SequentialCommandGroup(
                                                // if there are jams then reverse for a small amount of time
                                                // to get rid of them
                                                new InstantCommand(intake::intakeReverse),
                                                new InstantCommand(intake::wristTransfer),
                                                new WaitCommand(100),
                                                new InstantCommand(intake::intakeOff),
                                                new InstantCommand(intake::slideIn),
                                                new InstantCommand(() -> follower.holdPoint(new Pose(follower.getPose().getX() - 3, follower.getPose().getY(), follower.getPose().getHeading()))),
//                                                new WaitCommand(500),
                                                // re-try vision!!!
                                                new IntakeAuto.IntakeAutoPrep(intake, true),
                                                new IntakeSubAuto(intake, follower, level+1)
                                        ),
                                        new InstantCommand(),
                                        () -> !intake.forceSampleIntaked() || intake.needReverse() /* intake.isJammed() */
                                )

                        ),
                        new InstantCommand(),
                        () -> !intake.forceSampleIntaked() || intake.needReverse()
                ),

                new InstantCommand(() -> {
                    if (intake.forceSampleIntaked()) {
                        gotSample = true;
                    }
                }),

                new InstantCommand(intake::wristPreIntake),
                new WaitCommand(300),
                new InstantCommand(intake::intakeReverseSlow),
                new WaitCommand(100)
        );
    }
}
