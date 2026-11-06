package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class PushSampleCommand extends SequentialCommandGroup {
    public PushSampleCommand(Intake intake) {
//        isPushingSamples = true;
        addCommands(
                new InstantCommand(intake::wristHalfway),
                new InstantCommand(intake::slideBarelyOut),
                new WaitCommand(500),
                new InstantCommand(intake::wristPushing),
                new WaitCommand(500),
                new InstantCommand(intake::slidePushing),
                new WaitCommand(1000),
                new InstantCommand(intake::slideHalfway),
                new InstantCommand(intake::wristHalfway),
                new WaitCommand(500),
                new InstantCommand(intake::wristIntake),
                new WaitCommand(500),
                new InstantCommand(intake::slidePushing)
        );
    }
}
