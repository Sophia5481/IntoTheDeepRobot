/*package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.Subsystem;
import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.ElevatorFeedforward;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;

public class MotorMoveCommand extends CommandBase {
    private final MotorEx motor;
    private final int targetPosition;
    private final MotorCoefficients coeffs;
    private PIDController controller;
    private ElevatorFeedforward feedforward;

    public MotorMoveCommand(Subsystem subsystem, MotorEx motor, int targetPosition, MotorCoefficients coeffs) {
        addRequirements(subsystem);
        this.motor = motor;
        this.motor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        this.motor.setRunMode(Motor.RunMode.RawPower);
        this.coeffs = coeffs;
        this.targetPosition = targetPosition;
    }

    double startPos;
    double targPos;
    @Override
    public void initialize() {
        this.motor.setPositionCoefficient(1.0);
        Log.d("Outtake slides: ", "init pos: " + this.motor.getCurrentPosition());
        Log.d("Outtake slides: ", "target pos: " + targetPosition);
        startPos = this.motor.getCurrentPosition();
        targPos = targetPosition - startPos;
        prevTime = System.currentTimeMillis();
        controller = new PIDController(coeffs.P, coeffs.I, coeffs.D);
        if (targPos < 0) {
            feedforward = new ElevatorFeedforward(coeffs.kS, coeffs.kG_down, coeffs.kV_down, coeffs.kA_down);
        } else {
            feedforward = new ElevatorFeedforward(coeffs.kS, coeffs.kG, coeffs.kV, coeffs.kA);
        }

        // Reset values
        s = 0;
        v = 0;
        a = 0;
        done = false;
        decel = false;
    }

    double s;
    double v;
    double a;
    long prevTime;

    public double getS() {
        return s + startPos;
    }
    public double getV() {
        return v;
    }
    public double getA() {
        return a;
    }
    public double accelDist() {
        return v*(v/coeffs.accel) - 0.5*coeffs.accel*Math.pow(v/coeffs.accel, 2);
    }

    boolean done;
    boolean decel;

    @Override
    public void execute() {
        double dT = (System.currentTimeMillis() - prevTime) / 1000.0;
        prevTime = System.currentTimeMillis();

        // v = u+at, v = 0, u = v, a = -coeffs.accel, coeffs.accel*t = u, t = u/coeffs.accel
        // s = ut + 1/2at^2, a = -coeffs.accel, u = v, t = u/coeffs.accel (see previous)
        double decelDist = v*(v/coeffs.accel) - 0.5*coeffs.accel*Math.pow(v/coeffs.accel, 2);
        if (Math.abs(s - targPos) <= decelDist + 0.01 || decel) {
            decel = true;
            a = -coeffs.accel * Math.signum(targPos);
            v += a * dT;
            if (Math.signum(v) != Math.signum(targPos)) { // When this flips sign, the motion profile is done
                done = true;
            }
            s += v*dT + 0.5*a*Math.pow(dT, 2.0);
        } else if (Math.abs(v) < coeffs.vel) {
            a = coeffs.accel * Math.signum(targPos);
            v += a * dT;
            s += v*dT + 0.5*a*Math.pow(dT, 2.0);
        } else {
            a = 0.0;
            v = coeffs.vel * Math.signum(targPos);
            s += v*dT;
        }

        double output = feedforward.calculate(v, a) + controller.calculate(motor.getCurrentPosition() - startPos, s);
        motor.set(VoltageSensing.mapMotorPower(output));
    }

    @Override
    public boolean isFinished() {
        if (done) {
            Log.d("Outtake slides: ", "Finished = true");
        } else {
            Log.d("Outtake slides: ", "Finished = false");
        }
        return done;
    }

    @Override
    public void end(boolean interrupted) {
        this.motor.setVeloCoefficients(targetPosition, 0, 0);
        this.motor.setPositionCoefficient(0.0);
        Log.d("Outtake slides: ", "end");
    }
}*/
