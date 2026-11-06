package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;

// For intaking in auto
public class IntakePrepCommand extends SequentialCommandGroup {
    public IntakePrepCommand(Intake intake) {
        addRequirements(intake);
        addCommands(
                new InstantCommand(() -> {
                    intake.wristIntake();
                    intake.intakeOn();
                    intake.slideHalfway();
                }),
                new WaitCommand(300),
                intake.slideOutTime(1200)
        );
    }
}
