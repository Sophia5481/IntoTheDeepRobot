package org.firstinspires.ftc.teamcode.commands.outtake;

import android.util.Log;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class ScoreBucketCommand extends SequentialCommandGroup {
    //Scores the sample: claw opens, slide go down

    // These are the old versions of the commands
    // Kept here so the old autos don't bug out when I delete them
    // Used to score sample in the 0+4 part of left submersible auto by flinging it into the basket
//    public ScoreBucketCommand(Outtake outtake, Intake intake, boolean auto) {
//        addRequirements(outtake, intake);
//        addCommands(
//                auto ? new SequentialCommandGroup(
//                        new InstantCommand(outtake::sampleArmOuttake),
//                        new WaitCommand(50),
//                        new InstantCommand(outtake::sampleClawOpen)
//                ) :
//                        new InstantCommand(outtake::sampleClawOpen),
//                new WaitCommand(100),
//                new InstantCommand(outtake::sampleArmTransfer),
////                new InstantCommand(outtake::sampleClawClose),
//                new WaitCommand(250),
//                new InstantCommand(intake::wristStored),
//                outtake.bucketDown(),
//                new InstantCommand(() -> Log.d("ScoreBucketCommand", "ScoreBucketCommand done"))
//        );
//    }

    // scoring for HP without using intake
    public ScoreBucketCommand(Outtake outtake) {
        addRequirements(outtake);
        addCommands(
                new InstantCommand(() -> {
                    outtake.sampleClawWide();
                }),
                new WaitCommand(50),
                new InstantCommand(outtake::sampleArmTransfer),
//                new InstantCommand(outtake::sampleClawClose),
                new WaitCommand(250),
                outtake.bucketDown()
        );
    }

    // driver 2 scoring
    public ScoreBucketCommand(Outtake outtake, Intake intake, boolean driver2) {
        addRequirements(outtake, intake);
        addCommands(
                new InstantCommand(() -> {
                    outtake.sampleArmOuttake();
                    outtake.sampleClawOpen();
                }),
                new WaitCommand(400),
                new InstantCommand(outtake::sampleArmTransfer),
//                new InstantCommand(outtake::sampleClawClose),
                new WaitCommand(250),
                new InstantCommand(intake::wristStored),
                driver2 ? new InstantCommand() : outtake.bucketDown()
        );
    }

    // Used in left submersible auto after intaking from sub
    // Used in manual drive
    public ScoreBucketCommand(Outtake outtake, Intake intake, boolean auto, boolean throwSample) {
        addRequirements(outtake);
        addCommands(
                throwSample ? new SequentialCommandGroup(
                        new InstantCommand(outtake::sampleArmOuttake),
                        new WaitCommand(50),
                        new InstantCommand(outtake::sampleClawOpen)
                ) : new InstantCommand(() -> {
                    outtake.sampleClawOpen();
                    intake.wristHorizontal();
                }),
                new WaitCommand(250),
                new InstantCommand(outtake::sampleArmTransfer),
                new WaitCommand(250),
                outtake.bucketDown(),
                new WaitCommand(300),
                auto ? new InstantCommand() : new InstantCommand(intake::wristTransfer)
        );
    }

}
