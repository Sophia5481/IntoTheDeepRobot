package org.firstinspires.ftc.teamcode.pedroPathing.localization;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.DualNum;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.acmerobotics.roadrunner.Time;
import com.acmerobotics.roadrunner.Twist2dDual;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.Vector2dDual;
import com.acmerobotics.roadrunner.ftc.Encoder;
import com.acmerobotics.roadrunner.ftc.FlightRecorder;
import com.acmerobotics.roadrunner.ftc.OverflowEncoder;
import com.acmerobotics.roadrunner.ftc.PositionVelocityPair;
import com.acmerobotics.roadrunner.ftc.RawEncoder;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

@Config
public final class TwoDeadWheelLocalizer implements Localizer {
        public static class Params {
                public double parYTicks = 731.5328; // y position of the parallel encoder (in tick units)
                public double perpXTicks = 760.1767; // x position of the perpendicular encoder (in tick units)
        }

        public static Params PARAMS = new Params();

        public final Encoder par, perp;

        public final double inPerTick;

        public final IMU imu;
//        public final BNO055IMU imu;

        private int lastParPos, lastPerpPos;
        private boolean initialized;

        private double lastRawHeadingVel, headingVelOffset;
        private Rotation2d lastHeading;

        private Pose2d pose = new Pose2d(0.0,0.0,0.0);

       public TwoDeadWheelLocalizer(HardwareMap hardwareMap, IMU imu, double inPerTick) {
               // TODO: make sure your config has **motors** with these names (or change them)
               //   the encoders should be plugged into the slot matching the named motor
               //   see https://ftc-docs.firstinspires.org/en/latest/hardware_and_software_configuration/configuring/index.html
                par = new OverflowEncoder(new RawEncoder(hardwareMap.get(DcMotorEx.class, "rightFront")));
                // TODO: Reverse these encoders if necessary
//                par.setDirection(DcMotorSimple.Direction.REVERSE);
                perp = new OverflowEncoder(new RawEncoder(hardwareMap.get(DcMotorEx.class, "rightRear")));
                perp.setDirection(DcMotorSimple.Direction.REVERSE);
                this.imu = imu;

                lastParPos = par.getPositionAndVelocity().position;
                lastPerpPos = perp.getPositionAndVelocity().position;
                lastHeading = Rotation2d.exp(imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
//                lastHeading = Rotation2d.exp(imu.getAngularOrientation().firstAngle);

                this.inPerTick = inPerTick;

                FlightRecorder.write("TWO_DEAD_WHEEL_PARAMS", PARAMS);
        }

        public void update() {
                PositionVelocityPair parPosVel = par.getPositionAndVelocity();
                PositionVelocityPair perpPosVel = perp.getPositionAndVelocity();

                AngularVelocity angularVelocity = imu.getRobotAngularVelocity(AngleUnit.RADIANS);
                Rotation2d heading = Rotation2d.exp(imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));
                FlightRecorder.write("IMU_HEADING", Math.toDegrees(heading.log()));
                Log.d("IMU_HEADING", String.valueOf(Math.toDegrees(heading.log())));

            // see https://github.com/FIRST-Tech-Challenge/FtcRobotController/issues/617
                double rawHeadingVel = angularVelocity.zRotationRate;
    //                double rawHeadingVel = imu.getAngularVelocity().zRotationRate;
                if (Math.abs(rawHeadingVel - lastRawHeadingVel) > Math.PI) {
                    headingVelOffset -= Math.signum(rawHeadingVel) * 2 * Math.PI;
                }
                lastRawHeadingVel = rawHeadingVel;
                double headingVel = headingVelOffset + rawHeadingVel;

                if (!initialized) {
                    initialized = true;

                    lastParPos = parPosVel.position;
                    lastPerpPos = perpPosVel.position;
                    lastHeading = heading;

                    Twist2dDual<Time> t = new Twist2dDual<>(
                            Vector2dDual.constant(new Vector2d(0.0, 0.0), 2),
                            DualNum.constant(0.0, 2)
                    );

                    pose = pose.plus(t.value());
                    return;
                }

                int parPosDelta = parPosVel.position - lastParPos;
                int perpPosDelta = perpPosVel.position - lastPerpPos;
                double headingDelta = heading.minus(lastHeading);

                Twist2dDual<Time> twist = new Twist2dDual<>(
                                new Vector2dDual<>(
                                                new DualNum<Time>(new double[] {
                                                                parPosDelta - PARAMS.parYTicks * headingDelta,
                                                                parPosVel.velocity - PARAMS.parYTicks * headingVel,
                                                }).times(inPerTick),
                                                new DualNum<Time>(new double[] {
                                                                perpPosDelta - PARAMS.perpXTicks * headingDelta,
                                                                perpPosVel.velocity - PARAMS.perpXTicks * headingVel,
                                                }).times(inPerTick)),
                                new DualNum<>(new double[] {
                                                headingDelta,
                                                headingVel,
                                }));

                lastParPos = parPosVel.position;
                lastPerpPos = perpPosVel.position;
                lastHeading = heading;

                pose = pose.plus(twist.value());

            Log.d("Position_x: ", Double.toString(pose.position.x));
            Log.d("Position_y: ", Double.toString(pose.position.y));
            Log.d("Heading: ", Double.toString(pose.heading.toDouble()));
        }

        public void setPoseEstimate(Pose2d poseEstimate) {
            this.pose = poseEstimate;
        }

        public Pose2d getPoseEstimate() {
            return this.pose;
        }

        public void resetHeading(double newHeading) {
            this.pose = new Pose2d(this.pose.position.x, this.pose.position.y, newHeading);
        }
}
