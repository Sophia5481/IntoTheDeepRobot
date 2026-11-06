package org.firstinspires.ftc.teamcode.opmode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import java.util.List;

@Disabled
@TeleOp(name = "LimelightFirstTest", group = "TeleOp")
public class TestVisionOp extends LinearOpMode {
    private Limelight3A limelight = null;
    private LLResult result = null;

    @Override
    public void runOpMode() {
        telemetry.addLine("Initializing...");
        telemetry.update();

        limelight = hardwareMap.get(Limelight3A.class, "limelight");

        waitForStart();

        if (opModeIsActive()) {
            limelight.pipelineSwitch(0);
            limelight.start();
        }

        while (opModeIsActive()) {
            result = limelight.getLatestResult();

            if (result != null) {
                telemetry.addData("Pipeline: ", result.getPipelineIndex());
                List<LLResultTypes.DetectorResult> detected =  result.getDetectorResults();
                telemetry.addData("Detected objects: ", detected.size());
                telemetry.update();
            }
            else {
                telemetry.addData("Result: ", "null");
                telemetry.update();
            }
        }
    }
}
