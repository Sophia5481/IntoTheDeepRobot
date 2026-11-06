package org.firstinspires.ftc.teamcode.commands.intake;

import com.arcrobotics.ftclib.command.CommandBase;

import org.firstinspires.ftc.teamcode.subsystems.Intake;
import org.firstinspires.ftc.teamcode.subsystems.Outtake;

public class IntakeCommand extends CommandBase {
    private final Intake intake;
    private final Outtake outtake;
    private boolean stopWhenIntaked = false;
    private boolean auto;

    public IntakeCommand(Intake intake, Outtake outtake, boolean stopWhenIntaked, boolean auto) {
        this.intake = intake;
        this.outtake = outtake;
        this.stopWhenIntaked = stopWhenIntaked;
        this.auto = auto;
        addRequirements(intake);
    }

    @Override
    public void initialize() {
        if (!auto) {
            intake.slideHalfway(); // Slides will be pre-extended before intaking in auto
            intake.wristHalfway();
        } else {
            intake.wristIntake();
        }
        intake.intakeOn();
        intake.displayColor = true;
    }

    long queueOffStart = -1;
    public void execute() {
        if (intake.isIntakeReversing()) {
            return;
        }
        if (intake.sampleIntaked()) {
            if (queueOffStart == -1) {
                queueOffStart = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - queueOffStart > 500) {
                intake.intakeOff();
            }
        } else {
            queueOffStart = -1;
            intake.intakeOn();
        }
    }

    @Override
    public boolean isFinished() {
        return stopWhenIntaked && intake.sampleIntaked();
    }

    @Override
    public void end(boolean interrupted) {
        intake.intakeOff();
        intake.slideIn();
        intake.wristTransfer();
        outtake.sampleArmTransfer();
        outtake.sampleClawOpen();
        intake.displayColor = false;
    }
}