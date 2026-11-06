package org.firstinspires.ftc.teamcode.commands.outtake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class ExtendBucketCommand extends SequentialCommandGroup {
    // raises outtake slides to prepare scoring sample
    // used in teleop
    public ExtendBucketCommand(Outtake outtake, boolean high) {
        addRequirements(outtake);

        addCommands(
                new InstantCommand(outtake::sampleArmGrab),
                new WaitCommand(50),
                new InstantCommand(outtake::sampleClawClose),
                new WaitCommand(50),
                new ParallelCommandGroup(
//                        new InstantCommand(intake::wristHorizontal),
                        high ? outtake.bucketHigh() : outtake.bucketLow(),
                        new SequentialCommandGroup(
                                new WaitCommand(300),
                                new InstantCommand(outtake::sampleArmOuttake),
                                new WaitCommand(400)
                        )
                )
        );
    }

    // Todo: Make intake stored after extending the bucket
    // this makes the outtake pre
    public ExtendBucketCommand(Outtake outtake, Intake intake, boolean waitTime) {
        addRequirements(outtake);

        addCommands(
                new ParallelCommandGroup(
                        new InstantCommand(intake::wristTransfer),
                        new SequentialCommandGroup(
                                new InstantCommand(outtake::sampleClawClose),
                                new WaitCommand(50),
                                outtake.bucketHigh()
                        ),
                        new SequentialCommandGroup(
                                new WaitCommand(500),
//                                waitTime ? new InstantCommand(outtake::sampleArmPreOuttake) : new InstantCommand(outtake::sampleArmOuttake),
                               new InstantCommand(outtake::sampleArmPreOuttake),
                                waitTime ? new WaitCommand(300) : new InstantCommand()
                        )
                )
        );
    }

    // NEW SCORING FOR TELEOP: DIFFERENT FROM AUTO
    public ExtendBucketCommand(Outtake outtake, boolean high, boolean tele) {
        addRequirements(outtake);

        addCommands(
                new InstantCommand(outtake::sampleArmGrab),
                new WaitCommand(50),
                new InstantCommand(outtake::sampleClawClose),
                new WaitCommand(50),
                new ParallelCommandGroup(
//                        new InstantCommand(intake::wristHorizontal),
                        high ? (tele ? outtake.bucketHighTele() : outtake.bucketHigh()) : outtake.bucketLow(),
                        new SequentialCommandGroup(
                                new WaitCommand(300),
                                new InstantCommand(tele ? outtake::sampleArmOuttakeTele : outtake::sampleArmOuttake),
                                new WaitCommand(400)
                        )
                )
        );
    }
}
