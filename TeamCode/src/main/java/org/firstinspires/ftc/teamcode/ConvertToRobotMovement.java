package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.pedropathing.localization.Pose;

import org.firstinspires.ftc.teamcode.pedroPathing.pathGeneration.Point;

public class ConvertToRobotMovement {
    public static Pose target;
    public static Pose visionPose;
    // distance of robot from sub when intaking a sample during spec auto
    public static double specYDist = 7.5;
    // distance of robot from sub when using vision
    public static double sampleYDist = 0.0;
    // the code returns the coordinates of the center of the sample, but what we actually want to do
    // is have the intake's bottom roller slightly behind the sample to intake it better
    public static double sampleOffset = 4.5;

    public static double xOffsetGamepad = 5.0;
    // envisioning is the pose that the robot uses vision
    public static Pose getPose(double x, boolean spec, Point envisioning) {
        if (spec) {
            // maybe I reversed the 0.6 offset???
            target = new Pose(envisioning.getX() + x, 40, Math.toRadians(-90));
            Log.d("Target: ", "X: " + target.getX() + " Y: " + target.getY() + " Heading: " + target.getHeading());
            return target;
        } else {
            // We need to add the strafe offset to the robot's current y-value
            // My bad that the x increases as the robot goes left idk what I was thinking
            target = new Pose(envisioning.getX(), envisioning.getY() - (x + 0.6), Math.toRadians(180));
            Log.d("Target: ", "X: " + target.getX() + " Y: " + target.getY() + " Heading: " + target.getHeading());
            return target;
            // x-distance from vision pose and intaking pose is 12 inches
        }
    }

    // give the y-coordinate of the sample relative to the center of the robot in field coordinates
    // return the slide offset
    public static double convertToSlideOffset(double y, boolean spec) {
        // use regression or trig
        if (spec) {
            return clamp((y + specYDist - sampleOffset) * 0.0644818 - 0.0771029, 0.5, 1);
        } else {
            // use regression here
            return clamp((y - sampleYDist - sampleOffset) * 0.0644818 - 0.0771029, 0.4, 1);
        }
    }

    public static double strafeGamepadOffset(double x) {
        return x + xOffsetGamepad;
    }

    public static double bottomRollerDistance(double y) {
        return y * 0.0644818 - 0.0771029;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }
}