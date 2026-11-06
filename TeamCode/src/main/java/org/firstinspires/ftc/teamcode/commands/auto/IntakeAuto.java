package org.firstinspires.ftc.teamcode.commands.auto;

import android.util.Log;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.command.WaitUntilCommand;

import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

/**
 * Intake automation command
 * Extends slide, turns intake on, check the color sensor until confirmed sample got in
 */
public class IntakeAuto extends SequentialCommandGroup {
    public static class IntakeAutoPrep extends SequentialCommandGroup {
        public IntakeAutoPrep(Intake intake, boolean sub) {
            addCommands(
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.slidePos(0.35);
                        } else {
                            intake.slidePos(0.5);
                        }
                    }),
                    new WaitCommand(200),
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.wristTransfer();
                            intake.intakeOn();
                        } else {
                            intake.wristIntake();
                        }
//                        intake.intakeOn();
                    })
            );
        }

        // creates an offset for any sub intaking
        // used when we know there is a hole in the submersible so the slides can extend out a bit more
        public IntakeAutoPrep(Intake intake, boolean sub, double offset) {
            addCommands(
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.slidePos(offset);
                        } else {
                            intake.slidePos(offset);
                        }
                    }),
                    new WaitCommand(200),
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.wristPreIntake();
                            intake.intakeOn();
                        } else {
                            intake.wristIntake();
                        }
                        intake.intakeOn();
                    })
            );
        }

        // for spec sub intaking we can't turn on the intake while extending the slides
        public IntakeAutoPrep(Intake intake, boolean sub, double offset, boolean intakeOn) {
            addCommands(
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.slidePos(offset);
                        } else {
                            intake.slidePos(0.6);
                        }
                    }),
                    new WaitCommand(200),
                    new InstantCommand(() -> {
                        if (sub) {
                            intake.wristPreIntake();
                            if (intakeOn) {
                                intake.intakeOn();
                            }
                        } else {
                            intake.wristIntake();
                            intake.intakeOn();
                        }
                    })
            );
        }
    }

    public IntakeAuto(Intake intake, Outtake outtake, long deadlineTimeMillis) {
        addCommands(
                // do everything in parallel to speed up sample detection!
            new ParallelCommandGroup(
                    new InstantCommand(() -> intake.slidePos(1.0)),
                    new InstantCommand(() -> intake.wristIntake(1.0)),
                    new InstantCommand(() -> {
                        Log.d("Sub Auto: ", "wrist intake position: " + intake.getWristAngle());
                    }),

                    new InstantCommand(() -> intake.intakeOn()),
//                    new InstantCommand(() -> Log.d("Tried to extend slides", "true")),
                    new WaitUntilCommand(intake::sampleIntaked).withTimeout(deadlineTimeMillis),
                    new InstantCommand(() -> {
                        Log.d("Sub Auto: ", "Found sample " + intake.sampleIntaked());

                    })
            )
//            new InstantCommand(intake::wristTransfer),
//            new InstantCommand(() -> {
//                Log.d("Wrist Transfer:", "true");
//            })
//            new WaitCommand(200),
            // change this to be in parallel with the driving to scoring
//            new IntakeRetractCommand(intake, outtake, true)
        );
    }

    public IntakeAuto(Intake intake, long deadlineTimeMillis, double offset) {
        addCommands(
                // do everything in parallel to speed up sample detection!
                new ParallelCommandGroup(
                        new InstantCommand(() -> intake.intakeOn()),
                        // if offset is set to 0.0, then the slides were already pre-extended;
                        // the intake just needs to go down and turn on
                        (offset == 0.0) ? new InstantCommand() : new InstantCommand(() -> intake.slidePos(offset)),
//                        new InstantCommand(() -> Log.d("Tried to extend slides", "true")),
                        new InstantCommand(() -> intake.wristIntake(offset)),
                        new WaitUntilCommand(intake::sampleIntaked).withTimeout(deadlineTimeMillis)
                )
        );
    }
}
