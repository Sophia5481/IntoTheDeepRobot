package org.firstinspires.ftc.teamcode.commands.outtake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class ScoreSpecimenCommand extends SequentialCommandGroup {
    // scores the specimen by opening claw and retracting outtake slides
    public ScoreSpecimenCommand(Outtake outtake) {
        addRequirements(outtake);

        addCommands(
                new InstantCommand(outtake::specimenHalfOpen),
                new InstantCommand(outtake::specimenArmIntake),
                new InstantCommand(outtake::guideStored),
                new WaitCommand(300),
                new InstantCommand(outtake::specimenClawClose),
                outtake.specimenDown(),
                new InstantCommand(outtake::specimenClawOpen)
        );
    }
}
