package org.firstinspires.ftc.teamcode.commands.auto;

import android.util.Log;

import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;

import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeReverseCommand;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * Gets a samples from the submersible
 * Moves the horizontal slides forwards
 * If it gets a wrong colored sample,
 * automatically gets rid of it and extends further for another one
 */
public class IntakeSubAuto extends SequentialCommandGroup {

    public static Pose envisioning = new Pose(24 + 72.0 + 3.25, 14 + 72.0 + 1.0, Math.toRadians(180));
    public static int retries = 2;

    public IntakeSubAuto(Intake intake, Follower follower, int level) {
        addRequirements(intake);
        addCommands(
                new LimelightCommand(intake, follower, true, false),
                // TODO: Figure out why it needs so much time to correct
                new WaitCommand(200),
                new InstantCommand(intake::wristIntake),
                new InstantCommand(intake::intakeOn),
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
                                                new InstantCommand(() -> follower.holdPoint(envisioning)),
                                                new WaitCommand(500),
                                                // re-try vision!!!
                                                new IntakeSubAuto(intake, follower, level+1)
                                        ),
                                        new InstantCommand(),
                                        () -> !intake.forceSampleIntaked() || intake.needReverse() /* intake.isJammed() */
                                )

                        ),
                        new InstantCommand(),
                        () -> !intake.forceSampleIntaked() || intake.needReverse()
                )
        );
    }
}
