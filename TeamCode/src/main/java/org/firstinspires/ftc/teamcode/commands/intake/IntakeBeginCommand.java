package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * Extends horizontal slides and prepares to intake sample
 */
public class IntakeBeginCommand extends SequentialCommandGroup {
    public IntakeBeginCommand(Intake intake, double pos) {
        addRequirements(intake);
        addCommands(
                new SequentialCommandGroup(
//                        new InstantCommand(intake::intakeOn),
                        new InstantCommand(intake::wristPreIntake),
                        new WaitCommand(300),
                        new InstantCommand(() -> {
                            intake.INTAKING = true;
//                            intake.intakeOn();
                            intake.slidePos(pos);
                            intake.intakeOff();
                            intake.wristPreIntake();
                        })
                        //                        new InstantCommand(intake::intakeOn)
                )
        );
    }
}


