/*package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.arcrobotics.ftclib.controller.PIDController;
import com.arcrobotics.ftclib.controller.wpilibcontroller.ElevatorFeedforward;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class MotorPeriodicController {
    private MotorCoefficients coeffs;
    private ElevatorFeedforward feedforward;
    private PIDController pidController;
    private MotorEx motor;
    double prevPower = 0;

    public MotorPeriodicController(MotorCoefficients coeffs, MotorEx motor) {
        this.coeffs = coeffs;
        this.pidController = new PIDController(coeffs.P, coeffs.I, coeffs.D);
        this.feedforward = new ElevatorFeedforward(coeffs.kS, coeffs.kG, coeffs.kV, coeffs.kA);
        this.motor = motor;
        this.motor.setZeroPowerBehavior(Motor.ZeroPowerBehavior.FLOAT);
        this.motor.setRunMode(Motor.RunMode.RawPower);
        motor.setPositionCoefficient(0.0);
        motor.set(0);
        prevPower = 0;
    }

    public void periodic() {
        Log.d("Outtake slides: ", "get position coefficient: " + motor.getPositionCoefficient());
        if (motor.getPositionCoefficient() > 0.0) {
            return;
        }

        Log.d("Outtake slides: ", "get current position: " + motor.getCurrentPosition());
        double output = pidController.calculate(motor.getCurrentPosition(), motor.getVeloCoefficients()[0]);
        output += feedforward.calculate(0, 0);
        if (Math.abs(output - prevPower) < 0.01) {
            return;
        }
        motor.set(VoltageSensing.mapMotorPower(output));
        prevPower = output;
    }

    public void home() throws InterruptedException {
        motor.setPositionCoefficient(1.0);
        while (motor.motorEx.getCurrent(CurrentUnit.AMPS) < 4.0) {
            motor.set(-0.5);
        }
        Thread.sleep(200); // Let it stop spinning
        motor.set(0.0);
        Thread.sleep(200); // Let it come out of the pushed position
        motor.stopAndResetEncoder();
        motor.setPositionCoefficient(0.0);
    }
}*/
