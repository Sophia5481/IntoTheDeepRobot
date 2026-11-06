package org.firstinspires.ftc.teamcode.commands.outtake;

import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.SequentialCommandGroup;
import com.arcrobotics.ftclib.command.WaitCommand;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class GiveToHP extends SequentialCommandGroup {
    public GiveToHP(Outtake outtake, boolean preTransferred) {
        addRequirements(outtake);

        addCommands(
                new SequentialCommandGroup(
                        preTransferred ? new InstantCommand(() -> {
                            outtake.sampleClawClose();
                        }) : new InstantCommand(),
                        preTransferred? new WaitCommand(150) : new InstantCommand(),
                        new ParallelCommandGroup(
                                outtake.sampleHP(),
                                new SequentialCommandGroup(
                                        new WaitCommand(100),
                                        new InstantCommand(outtake::sampleArmHP)
                                )
                        )
                )
        );
    }
}
