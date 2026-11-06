package org.firstinspires.ftc.teamcode.subsystems;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * Class for our Blinkin LED strip
 *
 * Memory so we don't spam logs in case of constant pattern setting
 */
public class LED extends SubsystemBase {
    private RevBlinkinLedDriver ledDriver;

    BlinkinPattern pattern = null;

    public LED(HardwareMap hardwareMap) {
        try {
            ledDriver = hardwareMap.get(RevBlinkinLedDriver.class, "blinkin");
        } catch (IllegalArgumentException e) {
            RobotLog.addGlobalWarningMessage("Failed to find Rev Blinkin Driver");
            this.ledDriver = null;
        }
        setPattern(BlinkinPattern.BLACK);
    }

    public void setPattern(BlinkinPattern pattern) {
        if (this.ledDriver == null) {
            return;
        }
        if (pattern != this.pattern) ledDriver.setPattern(pattern);
        this.pattern = pattern;
    }

    public BlinkinPattern getPattern() {
        return pattern;
    }
}
