package org.firstinspires.ftc.teamcode.commands;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.Subsystem;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.util.Timing;

import java.util.concurrent.TimeUnit;

public class DualServoMoveCommand extends CommandBase {
    final ServoEx servo;
    final ServoEx servo1;
    final double newAngle;
    private double initialAngle;
    final boolean usePosition;
    private final Timing.Timer timer;

    public DualServoMoveCommand(Subsystem subsystem, ServoEx servo, ServoEx servo1, double newAngle, long time_ms, boolean usePosition) {
        addRequirements(subsystem);
        this.servo = servo;
        this.servo1 = servo1;
        this.newAngle = newAngle;
        this.timer = new Timing.Timer(time_ms, TimeUnit.MILLISECONDS);
        this.usePosition = usePosition;
    }

    @Override
    public void initialize() {
        if (usePosition) {
            this.initialAngle = servo.getPosition();
            this.initialAngle = servo1.getPosition();
        } else {
            this.initialAngle = servo.getAngle();
            this.initialAngle = servo1.getAngle();
        }
        this.timer.start();
    }

    @Override
    public void execute() {
        double complete = (double)timer.elapsedTime()/(double)(timer.elapsedTime() + timer.remainingTime());
        double angle = (newAngle - initialAngle) * complete + initialAngle;
        if (usePosition) {
            servo.setPosition(angle);
            servo1.setPosition(angle);
        } else {
            servo.turnToAngle(angle);
            servo1.turnToAngle(angle);
        }
    }

    @Override
    public boolean isFinished() {
        return timer.done();
    }

    @Override
    public void end(boolean interrupted) {
        if (usePosition) {
            servo.setPosition(newAngle);
            servo1.setPosition(newAngle);
        } else {
            servo.turnToAngle(newAngle);
            servo1.turnToAngle(newAngle);
        }
    }
}
