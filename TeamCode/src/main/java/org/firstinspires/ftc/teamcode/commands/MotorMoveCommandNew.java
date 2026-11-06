package org.firstinspires.ftc.teamcode.commands;

import android.util.Log;

import com.arcrobotics.ftclib.command.CommandBase;

public class MotorMoveCommandNew extends CommandBase {
    public double targetPosition;
    MotorController motorController;
    public long startTime;

    public MotorMoveCommandNew(MotorController motorController, double targetPosition) {
        this.motorController = motorController;
        this.targetPosition = targetPosition;
    }

    @Override
    public void initialize() {
        motorController.targetPosition = targetPosition;
        startTime = System.currentTimeMillis();
        Log.d("Outtake slides ", "startTime: " + startTime);
    }

    @Override
    public boolean isFinished() {
        Log.d("Outtake slides: ", "time: " + System.currentTimeMillis());
        return motorController.atTarget() || System.currentTimeMillis() - startTime > 1500;
    }
}