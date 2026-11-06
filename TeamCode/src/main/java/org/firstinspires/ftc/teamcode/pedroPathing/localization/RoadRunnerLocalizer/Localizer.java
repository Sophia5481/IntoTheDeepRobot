package org.firstinspires.ftc.teamcode.pedroPathing.localization.RoadRunnerLocalizer;

import com.acmerobotics.roadrunner.Time;
import com.acmerobotics.roadrunner.Twist2dDual;

public interface Localizer {
    Twist2dDual<Time> update();
}
