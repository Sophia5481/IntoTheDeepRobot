package org.firstinspires.ftc.teamcode.commands.outtake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class ResetOuttake extends SequentialCommandGroup {
    public ResetOuttake(Outtake outtake, Intake intake) {
        addRequirements(outtake, intake);

        addCommands(
                new InstantCommand(() -> {
                    outtake.sampleClawOpen();
                    outtake.sampleArmTransfer();
                    outtake.specimenClawClose();
                    outtake.specimenArmIntake();
                }),
                new WaitCommand(500),
                outtake.bucketDown(),
                outtake.specimenDown(),
                new InstantCommand(outtake::specimenClawOpen)
        );
    }
}
