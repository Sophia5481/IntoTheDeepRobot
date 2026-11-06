package org.firstinspires.ftc.teamcode.commands.outtake;

import android.util.Log;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class ExtendSpecimenCommand extends SequentialCommandGroup {
    // raises outtake slides to prepare scoring specimen
    public ExtendSpecimenCommand(Outtake outtake, boolean high) {
        addRequirements(outtake);

        addCommands(
                new ParallelCommandGroup(
                        new SequentialCommandGroup(
                                new InstantCommand(() -> Log.d("Extend specimen command: ", "true")),
//                                new WaitCommand(50),
                                new InstantCommand(outtake::specimenArmOuttake),
                                new InstantCommand(outtake::guideOut)
                        ),
                        high ? outtake.specimenHigh() : outtake.specimenLow()
                )
        );
    }
}
