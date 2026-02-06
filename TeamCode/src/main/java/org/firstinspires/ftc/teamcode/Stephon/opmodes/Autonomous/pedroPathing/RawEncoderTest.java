package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous.pedroPathing;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Raw Encoder Test", group="Testing")
public class RawEncoderTest extends LinearOpMode {

    GoBildaPinpointDriver pinpoint;

    @Override
    public void runOpMode() {
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);

        // Use defaults for now
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD,
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );

        pinpoint.recalibrateIMU();
        pinpoint.resetPosAndIMU();

        waitForStart();

        while (opModeIsActive()) {
            pinpoint.update();

            telemetry.addLine("=== RAW ENCODER VALUES ===");
            telemetry.addLine();
            telemetry.addData("X Encoder (Forward)", pinpoint.getEncoderX());
            telemetry.addData("Y Encoder (Strafe)", pinpoint.getEncoderY());
            telemetry.addLine();
            telemetry.addLine("TEST INSTRUCTIONS:");
            telemetry.addLine("1. Push robot FORWARD");
            telemetry.addLine("   → Which encoder changes?");
            telemetry.addLine("   → Does it go UP or DOWN?");
            telemetry.addLine();
            telemetry.addLine("2. Push robot LEFT");
            telemetry.addLine("   → Which encoder changes?");
            telemetry.addLine("   → Does it go UP or DOWN?");
            telemetry.update();

            sleep(100);
        }
    }
}