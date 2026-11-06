package org.firstinspires.ftc.teamcode.opmode;

import android.util.Log;

import com.arcrobotics.ftclib.hardware.ServoEx;
import com.arcrobotics.ftclib.hardware.SimpleServo;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.List;

@TeleOp(name="NeuralDetectionOpmode", group="test")
public class LimelightNeuralDetection extends OpMode {
    Limelight3A limelight3A;
    String alliance = "blue";
    MotorEx motor;
    //    int distance = 0.0336x2 + 0.8172x + 10.824; //cm linear y distance to camera
    ServoEx arm;
    ServoEx turret;
    LLResult ll;
//    FisheyePipeline fp;
    ServoEx wrist;
    ServoEx claw;

    double fp_angle = 0;

    final double ARM_LENGTH = 8;
    public static double ARM_UP = 0.2;
    public static double ARM_STORED = 0.55; //0.1
    public static double ARM_DOWN = 0.75;

    public static double TURRET_MINANGLE = -127;
    public static double TURRET_MAXANGLE = 190;

    public static double CLAW_OPEN = 0.3;
    public static double CLAW_CLOSED = 0.50;

    public static double WRIST_MINANGLE = -185;
    public static double WRIST_MAXANGLE = 140;

    @Override
    public void init() {
        limelight3A = hardwareMap.get(Limelight3A.class, "limelight");
    }

    public void start() {
        limelight3A.pipelineSwitch(0);
        limelight3A.start();
    }

    @Override
    public void loop() {
        limelight3A.pipelineSwitch(0);
        ll = limelight3A.getLatestResult();
        if (ll != null && ll.isValid()) {
            List<LLResultTypes.DetectorResult> detectorResults = ll.getDetectorResults();
            telemetry.addData("numResults", detectorResults.size());
            for (LLResultTypes.DetectorResult dr : detectorResults) {
                telemetry.addData("Class name: ", "" + dr.getClassName());
                Log.d("class name: ", "whitespace" + dr.getClassName() + "whitespace?");
                Log.d("alliance: ", alliance);
                if (dr.getClassName().equals(alliance)) {
                    Log.d("Correct sample? ", "true");
                } else {
                    Log.d("Correct sample? ", "false");
                }

//                telemetry.update();
//                if (dr.getClassName().equals(alliance)) {
                    double pixel_x = dr.getTargetXPixels();
                    double pixel_y = dr.getTargetYPixels();
                    Log.d("pixel_x", "" + pixel_x);
                    Log.d("pixel_y", "" + pixel_y);
                    telemetry.addData("pixel_x", pixel_x);
                    telemetry.addData("pixel_y", pixel_y);
//                    telemetry.update();

                    //regression
                    double y = 0.000173485*pixel_y*pixel_y - 0.15299*pixel_y + 40.67602;

                    double m = -0.00131877*y - 0.0151449;
                    double b = 0.330476*y + 11.64727;
                    double x = m*pixel_x + b;

                    double theta = Math.asin(x / ARM_LENGTH);
                    double ext = y - ARM_LENGTH * Math.cos(theta);

                    telemetry.addData("theta", Math.toDegrees(theta));
                    telemetry.addData("ext", ext);

//                    if (ext > 0 && Math.abs(theta) < Math.toRadians(135)) {
                        telemetry.addData("x", x);
                        telemetry.addData("y", y);
                        telemetry.addData("ext", ext);
                        telemetry.addData("theta", Math.toDegrees(theta));
//                        telemetry.addData("motorpos", motor.getDistance() - 2.5);
//                        break;
//                    }
                    Log.d("X pos: ", "" + x);
                    Log.d("Y pos: ", "" + y);
//                    telemetry.update();
//                }
            }
            telemetry.update();
        }
        /**
         List<LLResultTypes.ColorResult> colorResults = ll.getColorResults();
         for (LLResultTypes.ColorResult cr : colorResults) {

         double ax = cr.getTargetXDegrees();
         double ay = cr.getTargetYDegrees();
         double y = 0.0336 * ay * ay + 0.8172 * ay + 10.824;
         double m = 0.0147 * y + 0.2083;
         double x = m * ax - 5;

         double theta = Math.asin(x/ARM_LENGTH);
         double ext = y - ARM_LENGTH * Math.cos(theta);



         if (ext > 0 && Math.abs(theta) < Math.toRadians(135)) {
         telemetry.addData("x", x);
         telemetry.addData("y", y);
         telemetry.addData("ext", ext);
         telemetry.addData("theta", Math.toDegrees(theta));
         telemetry.addData("motorpos", motor.getDistance()-2.5);
         if (gamepad1.x) {
         wrist.turnToAngle(0);
         turret.turnToAngle(Math.toDegrees(-theta));
         //motor.setTargetDistance(); // Offset for position of turret relative to edge of sub
         double power = ((ext+2.5) - motor.getDistance())*0.3;
         motor.set(power);
         telemetry.addData("power", power);
         arm.setPosition(ARM_UP);
         telemetry.update();
         }
         if (gamepad1.b) {
         wrist.turnToAngle(0);
         double cx = fp.cX;
         double cy = fp.cY;
         telemetry.addData("cx", cx);
         telemetry.addData("cy", cy);

         double turret_angle = turret.getAngle(AngleUnit.RADIANS);
         double FE_x = cx * Math.cos(turret_angle) - cy * Math.sin(turret_angle);
         double FE_y = cx * Math.sin(turret_angle) + cy * Math.cos(turret_angle);
         telemetry.addData("FE_x", FE_x);
         telemetry.addData("FE_y", FE_y);

         turret.rotateByAngle(0.0002 * -FE_x, AngleUnit.RADIANS);
         telemetry.addData("turret_angle", turret.getAngle());

         double FE_power = FE_y * 0.005;
         telemetry.addData("FE_power", FE_power);

         motor.set(FE_power);
         telemetry.update();

         fp_angle = fp.angle;
         }
         if (gamepad1.a) {
         motor.set(0);
         if (fp_angle > 90) {
         wrist.turnToAngle(fp_angle - 90);
         } else {
         wrist.turnToAngle(fp_angle + 90);
         }
         arm.setPosition(ARM_DOWN);
         }
         break;
         }
         }
         } **/


        /*LLResult llResult = limelight3A.getLatestResult();
        if (llResult != null && llResult.isValid()) {
            double ax = llResult.getTx(); //in cm, negative = to the left, positive = to the right
            double ay = llResult.getTy(); //in cm, negative = down, positive = up
            double y = 0.0336 * ay * ay + 0.8172 * ay + 10.824;
            double m = 0.0147 * y + 0.2083;
            double x = m * ax - 6; //subtract 6 to account for displacement from limelight to center of robot
            //height of sample vs. camera
            telemetry.addData("Tx", llResult.getTx());
            telemetry.addData("Ty", llResult.getTy());
            telemetry.addData("Ta", llResult.getTa()); // how big target looks
            telemetry.addData("x distance: ", x);
            telemetry.addData("y distance: ", y);
            telemetry.addData("motor dist", motor.getDistance() - 3); // center of turret is 1.5in behind tip of slides, tip of slides is 1.5in behind sub wall when stored
            // arm is 8in
            //arm = 0.42
        }
        else {
            telemetry.addLine("None found");
        }*/

//        LLResult result = limelight3A.getLatestResult();
//        if (result != null) {
//            Pose3D botpose = result.getBotpose();
//            double captureLatency = result.getCaptureLatency();
//            double targetingLatency = result.getTargetingLatency();
//            double parseLatency = result.getParseLatency();
//            telemetry.addData("LL Latency", captureLatency + targetingLatency);
//            telemetry.addData("Parse Latency", parseLatency);
//
//            List<LLResultTypes.ColorResult> colorResults = result.getColorResults();
//            for (LLResultTypes.ColorResult cr : colorResults) {
//                List<List<Double>> targetCorners = cr.getTargetCorners();
//                if (targetCorners != null && !targetCorners.isEmpty()) {
//                    StringBuilder cornersOutput = new StringBuilder();
//                    for (List<Double> corner : targetCorners) {
//                        cornersOutput.append(String.format("(%.2f, %.2f) ", corner.get(0), corner.get(1)));
//                    }
//                    telemetry.addData("Target Corners", cornersOutput.toString());
//                } else {
//                    telemetry.addData("Target Corners", "No corners detected");
//                }
//
//                telemetry.addData("Color", "X: %.2f, Y: %.2f", cr.getTargetXDegrees(), cr.getTargetYDegrees());
//            }
//        }
//        else {
//            telemetry.addData("Limelight", "No data available");
//        }
//
//        telemetry.update();
    }

}