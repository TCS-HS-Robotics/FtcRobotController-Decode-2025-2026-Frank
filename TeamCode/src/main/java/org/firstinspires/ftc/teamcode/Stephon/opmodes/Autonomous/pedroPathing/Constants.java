package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(11.339809)

            // Automatic Tuners
            .forwardZeroPowerAcceleration(-57.51648875839661)
            .lateralZeroPowerAcceleration(-63.76013638540157)

            // Manual Tuners
            .translationalPIDFCoefficients(new PIDFCoefficients(0.1, 0, 0.01, 0.07))
            .headingPIDFCoefficients(new PIDFCoefficients(2, 0, 0.3, 0.05))
            .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.015, 0, 0.00001, 0.6, 0.04))
            .centripetalScaling(0.0003);


    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
            .rightFrontMotorName("rightFront")
            .rightRearMotorName("rightBack")
            .leftRearMotorName("leftBack")
            .leftFrontMotorName("leftFront")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)

            // Automatic Tuners
            .xVelocity(74.0302003875)
            .yVelocity(49.16369773083785);


    public static PinpointConstants localizerConstants = new PinpointConstants()
            .forwardPodY(-5.875)  // Keep this for now, we'll verify later
            .strafePodX(-8)     // Keep this for now, we'll verify later
            .distanceUnit(DistanceUnit.INCH)
            .hardwareMapName("pinpoint")
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)

            // THESE ARE THE KEY CHANGES:
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)  // Changed from REVERSED
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED); // Changed from FORWARD

    public static PathConstraints pathConstraints = new PathConstraints(
            0.99,
            100,
            0.75,
            1
    );

    // Slower constraints for intake paths
    public static PathConstraints intakePathConstraints = new PathConstraints(
            0.3,   // maxVelocity - much slower for intake
            50,    // maxAcceleration
            0.5,   // maxAngularVelocity
            0.5    // maxAngularAcceleration
    );

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();
    }
}



