//package org.firstinspires.ftc.teamcode.subsystems;
//
//import android.util.Log;
//
//import com.arcrobotics.ftclib.command.SubsystemBase;
//import com.arcrobotics.ftclib.hardware.SimpleServo;
//import com.arcrobotics.ftclib.hardware.motors.MotorEx;
//import com.qualcomm.hardware.limelightvision.LLResult;
//import com.qualcomm.hardware.limelightvision.LLResultTypes;
//import com.qualcomm.hardware.limelightvision.Limelight3A;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//
//import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
//import org.firstinspires.ftc.teamcode.Robot;
//import org.firstinspires.ftc.teamcode.commands.MotorController;
//import org.firstinspires.ftc.teamcode.opmode.PerspectiveSampleDetectionPipeline;
//import org.openftc.easyopencv.OpenCvCameraFactory;
//
//import java.util.List;
//
//public class Limelight extends SubsystemBase {
//    // the x and y coordinates of the sample relative to the robot
//    public class ResultParameters {
//        public double x;
//        public double y;
//
//        public ResultParameters(double ext, double theta) {
//            this.x = ext;
//            this.y = theta;
//        }
//    }
//
//    ResultParameters coordinates = new ResultParameters(0, 0);
//
//    public Limelight3A limelight;
//    public static LLResult ll = null;
//    private final boolean reset = false;
//    public boolean lookForSample = false;
//    private long initTime;
//
//    public Limelight(final HardwareMap hMap) {
//        limelight = hMap.get(Limelight3A.class, "limelight");
//    }
//
//    public void initialize() {
//        initTime = System.currentTimeMillis();
//        limelight.pipelineSwitch(0);
//        limelight.setPollRateHz(100);
//    }
//
//    // sets whether the limelight will look for samples in the periodic function.
//    public void turnOnVision() {
//        lookForSample = true;
//    }
//
//    public void turnOffVision() {
//        lookForSample = false;
//    }
//
//    public void startCamera() {
//        limelight.start();
//    }
//
//    public void stopCamera() {
//        limelight.close();
//    }
//
//    public void switchPipeline(int pipeline) {
//        limelight.pipelineSwitch(pipeline);
//    }
//
////    public getBestResult() {
////
////    }
//
//    public void periodic() {
//        if (lookForSample) {
//            limelight.setPollRateHz(100);
//            limelight.start();
//            initTime = System.currentTimeMillis();
//
//            if (ll == null || !ll.isValid() || ((System.currentTimeMillis() - initTime) < 100 && reset) || limelight.getTimeSinceLastUpdate() > 100) {
//                Log.d("LL_READ", "null: " + (ll == null));
//                if (ll != null) {
//                    Log.d("LL_READ", "isValid: " + (ll.isValid()));
//                }
//                Log.d("LL_READ", "timeValid: " + ((System.currentTimeMillis() - initTime) < 100 && reset));
//                Log.d("LL_READ", "timeSinceUpdate: " + limelight.getTimeSinceLastUpdate());
//                ll = limelight.getLatestResult();
//                return;
//            }
//
//            int skipCounter = 0;
//            List<LLResultTypes.DetectorResult> detectorResults = ll.getDetectorResults();
//            for (LLResultTypes.DetectorResult dr : detectorResults) {
//                if (!dr.getClassName().equals(Robot.RED_ALLIANCE ? "red" : "blue")) {
//                    continue;
//                }
//                ResultParameters params = resultIsValid(dr);
//                if (params == null) {
//                    Log.d("LL", "FAILED");
//                    continue;
//                }
//
//                // Extend
//                Log.d("LL", "ext: " + params.x);
//                Log.d("LL", "theta: " + Math.toDegrees(params.y));
//                return;
//            }
//        }
//    }
//
//    private ResultParameters resultIsValid(LLResultTypes.DetectorResult dr) {
//        double px = dr.getTargetXPixels();
//        double py = dr.getTargetYPixels();
//
//        double y = 0.000173485 * py * py - 0.15299 * py + 40.67602;
//
//        double m = -0.00131877 * y - 0.0151449;
//        double b = 0.330476 * y + 11.64727;
//        double x = m * px + b;
//
//        Log.d("LL_VALID", "px: " + px);
//        Log.d("LL_VALID", "py: " + py);
//        Log.d("LL_VALID", "y: " + y);
//        Log.d("LL_VALID", "x: " + x);
//
//        // Check constraints
//        if (y > 0 && // Not too close to the robot
////                x > 2 && // Not out of the range of the turret
//                y < 24) { // Meet 42" extension limit
//            return new ResultParameters(x, y);
//        }
//
//        return null;
//    }
//
//    boolean firstLook = true;
//    public ResultParameters getCoordinates() {
//        limelight.start();
//
//        if (System.currentTimeMillis() - initTime > 1000)
//            return null;
//
//        if (ll == null || !ll.isValid() || ((System.currentTimeMillis() - initTime) < 100 && reset) || limelight.getTimeSinceLastUpdate() > 100) {
//            Log.d("LL_READ", "null: " + (ll == null));
//            if (ll != null) {
//                Log.d("LL_READ", "isValid: " + (ll.isValid()));
//            }
//            Log.d("LL_READ", "timeValid: " + ((System.currentTimeMillis() - initTime) < 100 && reset));
//            Log.d("LL_READ", "timeSinceUpdate: " + limelight.getTimeSinceLastUpdate());
//            ll = limelight.getLatestResult();
//
//            int skipCounter = 0;
//            List<LLResultTypes.DetectorResult> detectorResults = ll.getDetectorResults();
//            for (LLResultTypes.DetectorResult dr : detectorResults) {
//                if (!dr.getClassName().equals(Robot.RED_ALLIANCE ? "red" : "blue")) {
//                    continue;
//                }
//                ResultParameters coordinates = resultIsValid(dr);
//                Log.d("LL", "ext: " + coordinates.x);
//                Log.d("LL", "theta: " + Math.toDegrees(coordinates.y));
//                if (coordinates == null) {
//                    Log.d("LL", "FAILED");
//                    continue;
//                } else {
//                    return coordinates;
//                }
//            }
//        }
//
//        return coordinates;
//    }
//
//    public ResultParameters getBestCoordinates() {
//        ResultParameters coordinates = null;
//
//        initTime = System.currentTimeMillis();
//        while (System.currentTimeMillis() - initTime < 100)
//            continue;
//
//        // Log.d("LL_READ", "timeValid: " + ((System.currentTimeMillis() - initTime) < 100 && reset));
//        Log.d("LL_READ", "timeSinceUpdate: " + limelight.getTimeSinceLastUpdate());
//        ll = limelight.getLatestResult();
//
//        if (ll == null) {
//            Log.d("Limelight", "did not get results");
//            return null;
//        }
//        int skipCounter = 0;
//        List<LLResultTypes.DetectorResult> detectorResults = ll.getDetectorResults();
//        for (LLResultTypes.DetectorResult dr : detectorResults) {
//            if (!dr.getClassName().equals(Robot.RED_ALLIANCE ? "red" : "blue")) {
//                Log.d("Limelight", "color mismatch");
//                continue;
//            }
//            coordinates = resultIsValid(dr);
//
//            if (coordinates == null) {
//                Log.d("LL", "FAILED");
//                continue;
//            } else {
//                Log.d("LL", "ext: " + coordinates.x);
//                Log.d("LL", "theta: " + Math.toDegrees(coordinates.y));
//                return coordinates;
//            }
//        }
//
//        return coordinates;
//    }
//}
