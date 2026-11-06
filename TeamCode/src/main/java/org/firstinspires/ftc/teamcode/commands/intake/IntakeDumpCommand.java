package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;

public class IntakeDumpCommand extends SequentialCommandGroup {
    boolean longTime;
    public IntakeDumpCommand(Intake intake) {
        addCommands(
                //new InstantCommand(intake::latchOpen),
                new InstantCommand(intake::intakeReverse),
                new WaitCommand(250),
                //new InstantCommand(intake::latchStored),
                new InstantCommand(intake::intakeOn)
        );
    }
}
