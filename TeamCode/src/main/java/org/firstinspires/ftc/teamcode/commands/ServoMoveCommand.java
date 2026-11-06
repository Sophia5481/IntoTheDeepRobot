package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.Subsystem;
import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.util.Timing;

import java.util.concurrent.TimeUnit;

public class ServoMoveCommand extends CommandBase {
    final ServoEx servo;
    final double newAngle;
    private double initialAngle;
    final boolean usePosition;
    private final Timing.Timer timer;

    public ServoMoveCommand(Subsystem subsystem, ServoEx servo, double newAngle, long time_ms, boolean usePosition) {
        addRequirements(subsystem);
        this.servo = servo;
        this.newAngle = newAngle;
        this.timer = new Timing.Timer(time_ms, TimeUnit.MILLISECONDS);
        this.usePosition = usePosition;
    }

    @Override
    public void initialize() {
        if (usePosition) {
            this.initialAngle = servo.getPosition();
        } else {
            this.initialAngle = servo.getAngle();
        }
        this.timer.start();
    }

    @Override
    public void execute() {
        double complete = (double)timer.elapsedTime()/(double)(timer.elapsedTime() + timer.remainingTime());
        double angle = (newAngle - initialAngle) * complete + initialAngle;
        if (usePosition) {
            servo.setPosition(angle);
        } else {
            servo.turnToAngle(angle);
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
        } else {
            servo.turnToAngle(newAngle);
        }
    }
}
