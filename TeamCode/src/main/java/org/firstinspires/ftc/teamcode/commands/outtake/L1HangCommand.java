package org.firstinspires.ftc.teamcode.commands.outtake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;

import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class L1HangCommand extends SequentialCommandGroup {
    public L1HangCommand(Outtake outtake) {
        addRequirements(outtake);

        addCommands(
                new InstantCommand(outtake::guideOut),
                outtake.specimenL1()
        );
    }
}
