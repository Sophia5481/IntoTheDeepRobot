package org.firstinspires.ftc.teamcode.opmode;

import static org.firstinspires.ftc.teamcode.VisionPoses.slideOffset;

import com.arcrobotics.ftclib.command.CommandOpMode;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.WaitCommand;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.Point;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.teamcode.Robot;
import org.firstinspires.ftc.teamcode.VisionPoses;
import org.firstinspires.ftc.teamcode.commands.SequentialCommandGroup;
import org.firstinspires.ftc.teamcode.commands.auto.IntakeSubAuto;
import org.firstinspires.ftc.teamcode.commands.intake.LimelightCommand;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.FConstants;
import org.firstinspires.ftc.teamcode.pedroPathingNew.constants.LConstants;

@Autonomous(name = "Test Spec Sub Intake", group = "Auto")
public class SpecSubIntakeTest extends CommandOpMode {
    private Follower follower;
    private Robot robot;
    private Limelight3A limelight;
    private GamepadEx g1;
    private VisionPoses visionPose;

    //    Pose2d start = new Pose2d(-8.5 + 72.0, 65.0 + 72.0, Math.toRadians(-90));
    private double startHeading = Math.toRadians(-90.0);
    // After converting to pedro coordinate system, add 72.0 to both x and y
    // The center of the field is now (72, 72), not (0, 0)
    Pose envisioning = new Pose(0, 31.5, Math.toRadians(-90));

    Path intakePath;

    @Override
    public void initialize() {
        telemetry.addLine("Initializing...");
        telemetry.update();

        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(envisioning);
        follower.setMaxPower(0.5);
        follower.updatePose();
        robot = new Robot(true, hardwareMap);

        while (opModeInInit()) {
            // first do alliance selection
            robot.allianceConfig(telemetry, gamepad1);
        }

        waitForStart();

        schedule(
                new SequentialCommandGroup(
                        new LimelightCommand(robot.intake, follower,false, true),
                        new WaitCommand(500),
//                        new FollowPath(follower, VisionPoses.getSubPath1()),
                        new InstantCommand(robot.intake::wristIntake)
//                        new IntakeSubAuto(robot.intake, slideOffset, follower)
//                        new IntakeRetractCommand(robot.intake, robot.outtake, false)
                )
        );
    }

    @Override
    public void run() {
        super.run();
        follower.drawOnDashBoard();
        follower.update();
        robot.resetCache();
    }
}
