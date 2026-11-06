package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

import java.util.function.BooleanSupplier;

/**
 * Transfers sample to outtake: retract horizontal slide, claw closes
 */
public class IntakeRetractCommand extends SequentialCommandGroup {
    public IntakeRetractCommand(Intake intake, Outtake outtake) {
        this(intake, outtake, false);
    }

    public IntakeRetractCommand(Intake intake, Outtake outtake, boolean fast) {
        addRequirements(intake, outtake);
        addCommands(
                new SequentialCommandGroup(
                        new InstantCommand(() -> {
                            intake.INTAKING = false;
                        }),

                        fast ? new InstantCommand(intake::intakeTransferOn) :
                                new SequentialCommandGroup(
                                        new InstantCommand(intake::wristTransfer),
                                        new InstantCommand(intake::intakeOn),
                                        new WaitCommand(150),
                                        new InstantCommand(intake::intakeReverse),
                                        new WaitCommand(50)),

                        new ParallelCommandGroup(
                                // Move in
                                new InstantCommand(() -> {
                                    // for intaking that's on the ground in auto, you can raise the wrist
                                    // in parallel with the slide movements, saving time :)
                                    if (fast) {
                                        intake.wristTransfer();
                                    }
                                    outtake.bucketDown();
                                    outtake.sampleArmTransfer();
                                    intake.slideTransfer();
                                    outtake.sampleClawOpen();
                                    intake.intakeTransferOn();
                                })
                        ),

                        fast ? new WaitCommand(400) : new WaitCommand(700),

//                        new InstantCommand(intake::wristTransfer),
                        // move the outtake arm a bit down to grab the sample better
                        new InstantCommand(outtake::sampleArmGrab),
                        new WaitCommand(200),
                        new InstantCommand(outtake::sampleClawClose),
                        new InstantCommand(intake::intakeOff)
                )
        );
    }
}
