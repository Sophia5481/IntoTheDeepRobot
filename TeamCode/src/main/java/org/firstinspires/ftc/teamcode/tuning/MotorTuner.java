/*package org.firstinspires.ftc.teamcode.tuning;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.commands.MotorCoefficients;
import org.firstinspires.ftc.teamcode.commands.MotorMoveCommand;
import org.firstinspires.ftc.teamcode.commands.MotorPeriodicController;
import org.firstinspires.ftc.teamcode.commands.VoltageSensing;

@Config
@TeleOp(name = "Motor Tuner", group = "Test")
public class MotorTuner extends LinearOpMode {
    public static String NAME = "bucket";
    public static int TARGET = 840;
    public static int SLEEP = 500;
    public static boolean INVERTED = true;
    public static MotorCoefficients coeffs = new MotorCoefficients(0.015, 0.05, 0.0001,
            0.0005, 0.00002, 0.01, 0.2,
            6000, 2000,
            0.00002, 0.0003, 0.11);

    private MotorEx motor;
    final private Telemetry telem = FtcDashboard.getInstance().getTelemetry();

    @Override
    public void runOpMode() {
        motor = new MotorEx(hardwareMap, NAME);
        motor.setInverted(INVERTED);
        VoltageSensing.initialize(hardwareMap);

        telem.addData("MotorPos", motor.getCurrentPosition());
        telem.addData("MotorVel", motor.getCorrectedVelocity());
        telem.addData("S", 0);
        telem.addData("V", 0);
        telem.addData("A", 0);
        telem.addData("AccelDist", 0);
        telem.addData("Power", motor.get());
        telem.addData("Target", motor.getCurrentPosition());
        telem.update();

        int off = motor.getCurrentPosition();

        waitForStart();

        while (opModeIsActive()) {
            MotorMoveCommand command = new MotorMoveCommand(null, motor, TARGET + off, coeffs);
            command.initialize();

            while (!command.isFinished() && opModeIsActive()) {
                command.execute();
                telem.addData("MotorPos", motor.getCurrentPosition());
                telem.addData("MotorVel", motor.getCorrectedVelocity());

                telem.addData("S", command.getS());
                telem.addData("V", command.getV());
                telem.addData("A", command.getA());
                telem.addData("AccelDist", command.accelDist());
                telem.addData("Power", motor.get());
                telem.addData("Target", TARGET + off);
                telem.update();
            }
            command.end(false);

            pause();

            command = new MotorMoveCommand(null, motor, off, coeffs);
            command.initialize();

            while (!command.isFinished() && opModeIsActive()) {
                command.execute();
                telem.addData("MotorPos", motor.getCurrentPosition());
                telem.addData("MotorVel", motor.getCorrectedVelocity());
                telem.addData("S", command.getS());
                telem.addData("V", command.getV());
                telem.addData("A", command.getA());
                telem.addData("AccelDist", command.accelDist());
                telem.addData("Power", motor.get());
                telem.addData("Target", off);
                telem.update();
            }
            command.end(false);

            pause();
        }
    }

    private void pause() {
        long endTime = System.currentTimeMillis() + SLEEP;
        while (System.currentTimeMillis() - endTime < 0 && opModeIsActive()) {
            telem.addData("MotorPos", motor.getCurrentPosition());
            telem.addData("MotorVel", motor.getCorrectedVelocity());
            telem.addData("S", 0);
            telem.addData("V", 0);
            telem.addData("A", 0);
            telem.addData("AccelDist", 0);
            telem.addData("Power", motor.get());
            telem.addData("Target", motor.getVeloCoefficients()[0]);
            telem.update();

            MotorPeriodicController controller = new MotorPeriodicController(coeffs, motor);
            controller.periodic();
        }
    }
}*/
