package org.firstinspires.ftc.teamcode.commands.intake;

import android.util.Log;

import com.acmerobotics.roadrunner.Pose2d;
import com.arcrobotics.ftclib.command.CommandBase;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathCallback;
import com.pedropathing.pathgen.PathChain;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;

import org.firstinspires.ftc.teamcode.ConvertToRobotMovement;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;
import org.firstinspires.ftc.teamcode.subsystems.Intake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.firstinspires.ftc.teamcode.utils.Logger;

public class LimelightCommand extends CommandBase {
    // return the sample data in this format
    // we don't use corner_y but may use it if the current sample offset is inaccurate.
    public class ResultParameters {
        public double x;
        public double y;
        public double corner_y;

        public ResultParameters(double ext, double theta) {
            this.x = ext;
            this.y = theta;
        }

        public ResultParameters(double ext, double theta, double cy) {
            this.x = ext;
            this.y = theta;
            this.corner_y = cy;
        }
    }

    Intake intake;
    Limelight3A limelight;
    Follower m_follower;
    public static LLResult ll;
    private final boolean reset;
    private Path intakePath;
    // the pose that the robot is using vision on
    private boolean spec;
    private double slideOffset;
    private final int detect_wait_time = 200;
    private ResultParameters defaultPose;

    private List<ResultParameters> yellow_results = new ArrayList<ResultParameters>();
    private List<ResultParameters> blue_results = new ArrayList<ResultParameters>();
    private List<ResultParameters> red_results = new ArrayList<ResultParameters>();

    String target_color = null;

    public int SKIP_COUNT = 0; // For skipping to the next sample, in teleop

    // default pose will be (0, 0)
    public LimelightCommand(Intake intake, Follower follower, boolean reset, boolean spec) {
        this.limelight = intake.getLimelight();
        this.intake = intake;
        this.reset = reset;
        m_follower = follower;
        this.spec = spec;
        addRequirements(intake);
        // if spec, we're aiming for the colored samples
        target_color = Robot.RED_ALLIANCE ? "red" : "blue";
        defaultPose = new ResultParameters(0, 0);

        Logger.d("Limelight ", "target color: " + target_color);
    }

    // set custom default pose from gamepad inputs
    public LimelightCommand(Intake intake, Follower follower, boolean reset, boolean spec, double def_x, double def_y) {
        this.limelight = intake.getLimelight();
        this.intake = intake;
        this.reset = reset;
        m_follower = follower;
        this.spec = spec;
        addRequirements(intake);
        // if spec, we're aiming for the colored samples
        target_color = Robot.RED_ALLIANCE ? "red" : "blue";
        defaultPose = new ResultParameters(def_x, def_y);

        Logger.d("Limelight ", "target color: " + target_color);
    }

    long initTime;
    @Override
    public void initialize() {
        if (reset) {
            ll = null;
        }
        extended = false;

        // get limelight ready to roll!
        limelight.setPollRateHz(100);
        limelight.pipelineSwitch(0);
        limelight.start();
        initTime = System.currentTimeMillis();

        ResultParameters coordinates = new ResultParameters(0, 0);

        // get initial values
        initTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - initTime < 100) {
            continue;
        }

        // give limelight time to detect
        boolean found = false;
        while (!found) {
            ll = limelight.getLatestResult();
            if (ll == null /* || hasTargetColorCount(ll.getDetectorResults(), target_color) < 2 */) {
                Logger.d("Limelight", "did not get results");
                yellow_results = new ArrayList<ResultParameters>();
                blue_results = new ArrayList<ResultParameters>();
                red_results = new ArrayList<ResultParameters>();
            }
            else {
                found = true;
                break;
            }
        }

        // Logger.d("LL_READ", "timeValid: " + ((System.currentTimeMillis() - initTime) < 100 && reset));
        Logger.d("Limelight", "timeSinceUpdate: " + limelight.getTimeSinceLastUpdate());

        List<LLResultTypes.DetectorResult> detectorResults = ll.getDetectorResults();

        for (LLResultTypes.DetectorResult dr : detectorResults) {
            String color = dr.getClassName();
            Logger.d("Limelight", "detected color " + color);
            ResultParameters converted = resultIsValid(dr, color);
            switch (color) {
                case "yellow":
                    if (converted != null) {
                        yellow_results.add(converted);
                    }
                    break;
                case "blue":
                    if (converted != null) {
                        blue_results.add(converted);
                    }
                    break;
                case "red":
                    if (converted != null) {
                        red_results.add(converted);
                    }
                    break;
                default:
                    break;
            }
        }

        Logger.d("Limelight", "detected yellow results " + yellow_results.size());
        Logger.d("Limelight", "detected blue results " + blue_results.size());
        Logger.d("Limelight", "detected red results " + red_results.size());

        // determine the best sample based on distance from blue samples
        coordinates = findBest(yellow_results, blue_results, red_results);

        // if no good coordinates, just do the default values
        if (coordinates == null) {
            Logger.d("Limelight", "did not find the best result");
            coordinates = defaultPose /* new ResultParameters(0, 0) */;
        }
        else {
            Logger.d("Limelight", "x value: " + coordinates.x);
            Logger.d("Limelight", "y value: " + (coordinates.y));
        }

        // begin to move intake and robot towards the target coordinates
        intake.wristPreIntake();
        Logger.d("Limelight", "slide offset:" + ConvertToRobotMovement.convertToSlideOffset(coordinates.y, spec));
        slideOffset = ConvertToRobotMovement.convertToSlideOffset(coordinates.y, spec);
        // if sample, intake slides can extend while the robot is driving
        if (!spec) {
            intake.slidePos(slideOffset);
        }

        // get the robot's curent position, calculate movements based on the position
        Pose vision = m_follower.getPose();
        // we are certain of what the heading should be so we don't need to use vision.getHeading() anymore
        double heading;
        if (spec) {
            heading = Math.toRadians(-90.0);
        } else {
            heading = Math.toRadians(180);
        }
        Logger.d("Limelight", "envision actual pose is" + vision.toString());

        Pose newPos = ConvertToRobotMovement.getPose(coordinates.x, spec, new Point(new Pose2d(vision.getX(), vision.getY(), vision.getHeading())));

        PathChain intakePath = new PathChain(new Path(new BezierLine(
                vision,
                newPos
        )));
        intakePath.getPath(0).setConstantHeadingInterpolation(heading);
        intakePath.getPath(0).setZeroPowerAccelerationMultiplier(2.5);

        if (spec) {
            intakePath.setCallbacks(new PathCallback(0.1, () -> {
                intake.slidePos(slideOffset);
            }, PathCallback.PARAMETRIC, 0));
        }

        m_follower.setMaxPower(spec ? 0.5 : 0.5);
        m_follower.followPath(intakePath, true);
//        m_follower.holdPoint(newPos);
    }

    @Override
    public void execute() {
    }

    private ResultParameters resultIsValid(LLResultTypes.DetectorResult dr, String color) {
        double px = dr.getTargetXPixels();
        double py = dr.getTargetYPixels();

        double minY = py;
        List<List<Double>> corners = dr.getTargetCorners();
        for (List<Double> corner : corners) {
            if (corner.get(1) < minY) {
                minY = corner.get(1);
            }
        }
        double corner_y = minY;

        double y = 0.000173485 * py * py - 0.15299 * py + 40.67602;

        double m = -0.00158467 * y - 0.0118417;
        double b = 0.429344 * y + 10.42236;
        double x = m * px + b;

//        double theta = Math.asin(x / Intake.ARM_LENGTH);
//        double ext = y - Intake.ARM_LENGTH * Math.cos(theta);

        Logger.d("Limelight", "px: " + px);
        Logger.d("Limelight", "py: " + py);
        Logger.d("Limelight", "y: " + y);
        Logger.d("Limelight", "x: " + x);
        Logger.d("Limelight", "corners " + dr.getTargetCorners());
//        Logger.d("LL_VALID", "ext: " + ext);
//        Logger.d("LL_VALID", "theta: " + Math.toDegrees(theta));

        // Check constraints
//        if (y > 0 && // Not too close to the robot
////                Math.abs(theta) < Math.toRadians(135) && // Not out of the range of the turret
////                Math.abs(Math.abs(theta) - Math.toRadians(90)) > Math.toRadians(10) && // If it requires the turret to be all the way left and the fisheye needs to move it more left it won't work
//                y < 36 &&) { // Meet 42" extension limit
//            return new ResultParameters(x, y);
//        }

        // specimen and sample have different constraints
        if (Robot.RED_ALLIANCE && color.equals("blue")) { //don't cut it out so we can use it for calculating optimal sample
            return new ResultParameters(x, y);
        }
        else if (!Robot.RED_ALLIANCE && color.equals("red")) { //same thing
            return new ResultParameters(x, y);
        }

        if (spec) {
            // specimen has less margin for x
            if (y > 1.5 && // Not too close to the robot
                    y < 12 && // Not too far from the robot
                    x > -2 && x < 12.5) {  // robot can't strafe too much or else the regression error will acumulate
                return new ResultParameters(x, y, corner_y);
            }
        } else {
            if (y > 8.0 && // Not too close to the robot
                    y < 20 && // Not too far from the robot
                    x < 10 && x > -3) {  // robot can't strafe too much or else the regression error will acumulate
                return new ResultParameters(x, y, corner_y);
            }
        }

        Logger.d("Limelight", "Detected object too far away");
        // if detection is invalid, just return default value
        return null;
    }

    boolean extended = false;
    @Override
    public boolean isFinished() {
        return !m_follower.isBusy() || System.currentTimeMillis() - initTime > 1000;
    }

    @Override
    public void end(boolean interrupted) {
//        if (spec) {
//            intake.slidePos(slideOffset);
//            new WaitCommand(500);
//        }
        limelight.stop();
        limelight.setPollRateHz(0);
    }

    private double distance(ResultParameters dr1, ResultParameters dr2) {
        double x1 = dr1.x;
        double y1 = dr1.y;
        double x2 = dr2.x;
        double y2 = dr2.y;
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private ResultParameters findBest(List<ResultParameters> yellow_results, List<ResultParameters> blue_results, List<ResultParameters> red_results) {
        List<ResultParameters> target_array = null;
        ResultParameters bestResult = null;
        List<ResultParameters> targetResults = new ArrayList<ResultParameters>();
        List<ResultParameters> excludedResults = new ArrayList<ResultParameters>();

        switch (target_color) {
            case "blue":
                if (spec) {
                    targetResults.addAll(blue_results);
                    excludedResults.addAll(yellow_results);
                    excludedResults.addAll(red_results);
                }
                else {
                    targetResults.addAll(yellow_results);
                    targetResults.addAll(blue_results);
                    excludedResults.addAll(red_results);
                    Logger.d("Limelight", "targeting yellow and blue ");
                }
                bestResult = findBestByDistance(targetResults, excludedResults);
                break;
            case "red":
                if (spec) {
                    targetResults.addAll(red_results);
                    excludedResults.addAll(yellow_results);
                    excludedResults.addAll(blue_results);
                }
                else {
                    targetResults.addAll(red_results);
                    targetResults.addAll(yellow_results);
                    excludedResults.addAll(blue_results);
                }
                bestResult = findBestByDistance(targetResults, excludedResults);
                break;
            default:
                break;
        }

        return bestResult;
    }

    private ResultParameters findBestByDistance(List<ResultParameters> target_results, List<ResultParameters> excluded_results) {
        Logger.d("Limelight", "trying to find best result from a pool of " + target_results.size());
        ResultParameters best = null;
        double minimalDistance = 1;

        if (target_results == null || target_results.size() == 0) {
            return null;
        }

        List<Double> minDistance = new ArrayList<>();
        for (ResultParameters rp : target_results) {
            double currDistance = Integer.MAX_VALUE;
            for (ResultParameters excluded : excluded_results) {
                currDistance = Math.min(distance(rp, excluded), currDistance);
            }
            minDistance.add(currDistance);
            Logger.d("Limelight", "minDistance added is " + currDistance);
        }

        double optimal = 0;
        int optimalIndex = 0;
        for (int i = 0; i < minDistance.size(); i++) {
            if (minDistance.get(i) > optimal) {
                optimal = minDistance.get(i);
                optimalIndex = i;
            }
        }

        Logger.d("Limelight", "optimal index is" + optimalIndex);
        return target_results.get(optimalIndex);


//        for (double idealDistance = 5; idealDistance >= minimalDistance; idealDistance -= 0.5) {
//            for (ResultParameters rp : target_results) {
//                boolean isGood = true;
//                for (ResultParameters excluded : excluded_results)
//                    if (distance(rp, excluded) < idealDistance) {
//                        isGood = false;
//                        Logger.d("Limelight", "detected excluded colors too close");
//                        break;
//                    }
//
//                if (isGood) {
//                    return rp;
//                }
//            }
//        }
//
//        return best;
    }

    private int hasTargetColorCount(List<LLResultTypes.DetectorResult> detectorResults, String targetColor) {
        int count = 0;
        for (LLResultTypes.DetectorResult dr : detectorResults) {
            if (dr.getClassName().equals(targetColor)) {
                Logger.d("Limelight", "detected target color " + targetColor);
                count ++;
            }
        }
        return count;
    }
}