package org.firstinspires.ftc.teamcode.Frank.opmodes.Teleop;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@TeleOp
public class PinpointPoseTest extends OpMode {
    private GoBildaPinpointDriver pinpoint;

    @Override
    public void init() {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        // Make sure these match your physical setup
        pinpoint.setOffsets(20.60, -15.84, DistanceUnit.INCH);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.REVERSED
        );

        pinpoint.resetPosAndIMU();
    }

    @Override
    public void loop() {
        pinpoint.update();
        Pose2D pose = pinpoint.getPosition();

        telemetry.addData("X (inches)", pose.getX(DistanceUnit.INCH));
        telemetry.addData("Y (inches)", pose.getY(DistanceUnit.INCH));
        telemetry.addData("Heading (degrees)", Math.toDegrees(pose.getHeading(AngleUnit.RADIANS)));
        telemetry.update();
    }
}