package org.firstinspires.ftc.teamcode.commands;

public class MotorCoefficients {
    public double P;
    public double I;
    public double D;
    public double kV;
    public double kA;
    public double kS;
    public double kG;
    public double accel;
    public double vel;

    public double kV_down;
    public double kA_down;
    public double kG_down;

    public MotorCoefficients(double P, double I, double D, double kV, double kA, double kS, double kG, double accel, double vel, double kA_down, double kV_down, double kG_down) {
        this.P = P;
        this.I = I;
        this.D = D;
        this.kV = kV;
        this.kA = kA;
        this.kS = kS;
        this.kG = kG;
        this.accel = accel;
        this.vel = vel;

        this.kA_down = kA_down;
        this.kV_down = kV_down;
        this.kG_down = kG_down;
    }
}