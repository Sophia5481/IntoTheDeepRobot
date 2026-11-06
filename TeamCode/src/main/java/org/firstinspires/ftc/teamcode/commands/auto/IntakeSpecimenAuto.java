package org.firstinspires.ftc.teamcode.commands.auto;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class IntakeSpecimenAuto extends SequentialCommandGroup {

    public IntakeSpecimenAuto(Intake intake, Outtake outtake, long deadlineTimeMillis) {
        addCommands(
                new InstantCommand(intake::wristIntake),
//                new InstantCommand(() -> intake.slidePos(1.0)),
                new InstantCommand(() -> {
                    intake.slideOutTime(800);
                }),
                new InstantCommand(intake::intakeOn)
        );
    }

    public static class SpecimenAutoReverse extends SequentialCommandGroup {
        public SpecimenAutoReverse(Intake intake) {
            addCommands(
                    new InstantCommand(() -> {
                        intake.slidePos(1.0);
                    }),
                    new InstantCommand(intake::intakeReverse),
                    new WaitCommand(350),
                    new InstantCommand(() -> {
                        intake.intakeOff();
                        intake.wristTransfer();
                        intake.slideIn();
                    })
            );
        }
    }
}
