package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

// SQUID CONTROLLER RAHHHH
public class MotorController {
    private MotorEx motor;
    double prevPower = 0;

    double kp;
    double kf;
    double kd = 0.0;

    public double targetPosition = 0;
    public double currentPosition = 0;
    public double tolerance; // Inches
    private double lastError = 0;

    ElapsedTime timer = new ElapsedTime();

    public MotorController(MotorEx motor, double kp, double kf, double tolerance) {
        this.motor = motor;
        this.kp = kp;
        this.kf = kf;
        this.tolerance = tolerance;
    }

    public MotorController(MotorEx motor, double kp, double kd, double kf, double tolerance) {
        this.motor = motor;
        this.kp = kp;
        this.kf = kf;
        this.kd = kd;
        this.tolerance = tolerance;
    }

    public void periodic() {
        if (motor.getPositionCoefficient() > 0.0) {
            return;
        }

        currentPosition = motor.getDistance();
        double error = (targetPosition - currentPosition);
        double derivative = (error - lastError) / timer.seconds();
        double output = Range.clip(kp * error + kd * derivative + kf, -1.0, 1.0);
        if (Math.abs(output - prevPower) < 0.01) {
            return;
        }
        motor.set(output);
        lastError = error;
        Log.d("Outtake slides", "output: " + output);
        Log.d("Outtake slides", "target pos: " + targetPosition);
        Log.d("Outtake slides", "current pos: " + currentPosition);
        prevPower = output;
        timer.reset();
    }

    public void home() throws InterruptedException {
        motor.setPositionCoefficient(1.0);
        while (motor.motorEx.getCurrent(CurrentUnit.AMPS) < 3.5) {
            motor.set(-0.5);
        }
        Thread.sleep(200); // Let it stop spinning
        motor.set(0.0);
        Thread.sleep(200); // Let it come out of the pushed position
        motor.stopAndResetEncoder();
        Thread.sleep(100);
        motor.setPositionCoefficient(0.0);
    }

    public boolean atTarget() {
        Log.d("Outtake slides: ", "at target?" + (Math.abs(currentPosition - targetPosition) < tolerance ? "true" : "false"));
        return Math.abs(currentPosition - targetPosition) < tolerance;
    }

    public Command moveCommand(double distance) {
        return new MotorMoveCommandNew(this, distance);
    }
}