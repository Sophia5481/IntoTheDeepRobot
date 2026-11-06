package org.firstinspires.ftc.teamcode;

import static android.graphics.Insets.add;

import android.util.Log;

import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;

import java.util.ArrayList;

// a class storing all of our poses the robot needs to move to based on vision
// they are all public as they are only modified by the Limelight and ConverToRobotMovement class.
public class VisionPoses {
    public static Pose envisioning = new Pose(36 + 72.0, 9 + 72.0, Math.toRadians(180));
    public static Pose sub1 = new Pose(36 + 72.0, 9 + 72.0, Math.toRadians(180));
    public static Pose sub2 = new Pose(36 + 72.0, 9 + 72.0, Math.toRadians(180));
    public static Pose sub3 = new Pose(36 + 72.0, 9 + 72.0, Math.toRadians(180));
    public static Path subPath1;
    public static ArrayList<Pose> subIntake = new ArrayList<Pose>();
    public static double slideOffset = 0.0;
    public static Pose spec1 = new Pose(0, 34, Math.toRadians(-90));

    public VisionPoses() {
        subIntake.add(sub1);
        subIntake.add(sub2);
        subIntake.add(sub3);
    }

    public static Path getSubPath1() {
        Log.d("Sub Intake: ", "Envisioning x: " + envisioning.getX() + " Envisioning y: " + envisioning.getY());
        Log.d("Sub Intake: ", "Sub1 x: " + envisioning.getX() + " Sub1 y: " + envisioning.getY());
        subPath1 = new Path(new BezierLine(
                envisioning,
                sub1
        ));
        return subPath1;
    }
}
