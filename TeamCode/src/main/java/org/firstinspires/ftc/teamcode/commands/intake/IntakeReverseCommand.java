package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;

/**
 * Gets rid of samples of the wrong color
 */
public class IntakeReverseCommand extends SequentialCommandGroup {
    public IntakeReverseCommand(Intake intake, boolean reIntake) {
        addRequirements(intake);
        addCommands(
                new ConditionalCommand(
                        new SequentialCommandGroup(
                                // Be sure that its the right color
                                new InstantCommand(() -> intake.INTAKING = false),
                                new WaitCommand(200),
                                new InstantCommand(intake::updateSampleColor),
                                new ConditionalCommand(new SequentialCommandGroup(
                                        // Reverse sequence
                                        new InstantCommand(intake::wristReverse),
                                        new WaitCommand(200),
                                        new InstantCommand(intake::intakeReverseSlow),
                                        new WaitUntilCommand(() -> !intake.sampleIntaked()),
                                        new WaitCommand(300),
                                        reIntake ? new InstantCommand(() -> {
                                            intake.intakeOn();
                                            intake.wristIntake();
                                        }) : new InstantCommand()
                                ), new InstantCommand(), intake::isWrongColor),
                                new InstantCommand(() -> intake.INTAKING = true)
                        ),
                        new InstantCommand(),
                        intake::sampleIntaked
                )
        );
    }
}


