package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;

public class ManualInput {
    // Credit to team 20077 for their open source gamepad reading code
    private Telemetry telemetry;
    private Gamepad g1 = new Gamepad(), p1 = new Gamepad();
    private ArrayList<ManualPose> manualPoses = new ArrayList<>();
    private int currentPose = 0;

    public ManualInput(Telemetry telemetry, int sample, boolean spec) {
        this.telemetry = telemetry;

        for(int i = 0; i < sample; i++)
            manualPoses.add(new ManualPose(telemetry, spec));
    }

    public void update(Gamepad g) {
        p1.copy(g1);
        g1.copy(g);

        if(g1.dpad_up && !p1.dpad_up)
            getCurrent().f(1);
        else if(g1.dpad_down && !p1.dpad_down)
            getCurrent().b(1);
        else if(g1.dpad_left && !p1.dpad_left)
            getCurrent().l(1);
        else if(g1.dpad_right && !p1.dpad_right)
            getCurrent().r(1);

        if (g1.y && !p1.y)
            getCurrent().f(0.5);
        else if (g1.a && !p1.a)
            getCurrent().b(0.5);
        else if (g1.x && !p1.x)
            getCurrent().l(0.5);
        else if (g1.b && !p1.b)
            getCurrent().r(0.5);

        if(g1.right_bumper && !p1.right_bumper)
            getCurrent().t(true);
        else if(g1.left_bumper && !p1.left_bumper)
            getCurrent().t(false);

        if(g1.back && !p1.back) {
            currentPose++;
            if(currentPose >= manualPoses.size())
                currentPose = 0;
        } else if(g1.start && !p1.start) {
            currentPose--;
            if(currentPose < 0)
                currentPose = manualPoses.size() - 1;
        }

        telemetry.addData("Current Sample Pose (0 is the first)", currentPose);
        getCurrent().update();

        telemetry.addLine();

        for(ManualPose mp : manualPoses)
            telemetry.addData("Pose " + manualPoses.indexOf(mp), mp.getPose().toString());
    }

    public ManualPose getCurrent() {
        return manualPoses.get(currentPose);
    }

    public ArrayList<ManualPose> getManualPoses() {
        return manualPoses;
    }
}