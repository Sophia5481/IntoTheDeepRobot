package org.firstinspires.ftc.teamcode.opmode;

import static org.firstinspires.ftc.teamcode.VisionPoses.slideOffset;
import static org.firstinspires.ftc.teamcode.VisionPoses.subIntake;
import static org.firstinspires.ftc.teamcode.VisionPoses.subPath1;

import android.util.Log;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.arcrobotics.ftclib.command.Command;
import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.ConditionalCommand;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.ParallelCommandGroup;
import com.arcrobotics.ftclib.command.ParallelRaceGroup;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.util.Timing;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.ConvertToRobotMovement;
import org.firstinspires.ftc.teamcode.ManualInput;
import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.VisionPoses;
import org.firstinspires.ftc.teamcode.commands.FollowPath;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeSubAuto;
import org.firstinspires.ftc.teamcode.commands.intake.IntakeRetractCommand;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;
//import org.firstinspires.ftc.teamcode.subsystems.Limelight;
import org.firstinspires.ftc.teamcode.utils.Logger;

import com.pedropathing.pathgen.Point;

@Autonomous(name = "Test Sub Intake", group = "Auto")
public class SubIntakeTest extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private Limelight3A limelight;
    private GamepadEx g1;
    private VisionPoses visionPose;

    //    Pose2d start = new Pose2d(-8.5 + 72.0, 65.0 + 72.0, Math.toRadians(-90));
    // After converting to pedro coordinate system, add 72.0 to both x and y
    // The center of the field is now (72, 72), not (0, 0)
    Pose envisioning = new Pose(36 + 72.0, 9 + 72.0, Math.toRadians(180));

    Pose envisioningTestStart = new Pose(36 + 72.0 + 2, 9 + 72.0, Math.toRadians(180));

    @Override
    public void initialize() {
        Logger.d("limelight", "verify the log time format");
        telemetry.addLine("Initializing...");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(envisioningTestStart);
        follower.setMaxPower(1.0);
        follower.updatePose();
        robot = new Robot(true, hardwareMap);

        while (opModeInInit()) {
            // first do alliance selection
            robot.allianceConfig(telemetry, gamepad1);
        }

        waitForStart();

        schedule(
//                new SequentialCommandGroup(
//                        new LimelightCommand(robot.intake, follower, false, false),
//                        new FollowPath(follower, VisionPoses.getSubPath1()),
                        new IntakeSubAuto(robot.intake, follower, 0)
//                        new IntakeRetractCommand(robot.intake, robot.outtake, false)
//                )
        );
    }

    public Command envisionTestingPath() {
        Path toEnvision = new Path(new BezierLine(
                envisioningTestStart,
                envisioning
        ));
        toEnvision.setConstantHeadingInterpolation(Math.toRadians(180));

        return new FollowPath(follower, toEnvision, true);
    }

    @Override
    public void run() {
        super.run();
        follower.drawOnDashBoard();
        follower.update();
        robot.resetCache();
    }
}
