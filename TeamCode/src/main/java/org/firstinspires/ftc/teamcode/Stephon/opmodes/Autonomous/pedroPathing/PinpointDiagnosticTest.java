package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous.pedroPathing;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

@TeleOp(name="Pinpoint Diagnostic Test", group="Testing")
public class PinpointDiagnosticTest extends LinearOpMode {

    GoBildaPinpointDriver pinpoint;

    @Override
    public void runOpMode() {
        // Initialize the Pinpoint
        pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        // Configure using YOUR current settings
        pinpoint.setOffsets(-8.0, 0.0, DistanceUnit.INCH); // Your forwardPodY, strafePodX in MM (convert inches to mm: -8in = -203.2mm, 0in = 0mm)
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(
                GoBildaPinpointDriver.EncoderDirection.FORWARD, // Forward encoder
                GoBildaPinpointDriver.EncoderDirection.REVERSED   // Strafe encoder
        );

        pinpoint.resetPosAndIMU();

        telemetry.addLine("Pinpoint Diagnostic Test Ready");
        telemetry.addLine("Press START, then follow instructions on screen");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) return;

        // Test sequence
        runTest("TEST 1: FORWARD", "Push robot FORWARD 24 inches (2 tiles), then press A");
        runTest("TEST 2: BACKWARD", "Push robot BACKWARD 24 inches, then press A");
        runTest("TEST 3: LEFT", "Push robot LEFT 24 inches, then press A");
        runTest("TEST 4: RIGHT", "Push robot RIGHT 24 inches, then press A");
        runTest("TEST 5: ROTATE CCW", "Rotate robot COUNTERCLOCKWISE 90 degrees, then press A");
        runTest("TEST 6: ROTATE CW", "Rotate robot CLOCKWISE 90 degrees, then press A");

        telemetry.addLine("=== TESTING COMPLETE ===");
        telemetry.addLine("Take a screenshot and send the results!");
        telemetry.update();

        while (opModeIsActive()) {
            sleep(100);
        }
    }

    private void runTest(String testName, String instruction) {
        // Reset position
        pinpoint.resetPosAndIMU();
        sleep(500); // Let IMU settle

        // Wait for user to perform action
        telemetry.clear();
        telemetry.addLine("======================");
        telemetry.addLine(testName);
        telemetry.addLine("======================");
        telemetry.addLine();
        telemetry.addLine(instruction);
        telemetry.addLine();
        telemetry.addLine("Current Position:");

        while (opModeIsActive() && !gamepad1.a) {
            pinpoint.update();
            Pose2D pos = pinpoint.getPosition();

            telemetry.addData("X (inches)", pos.getX(DistanceUnit.INCH));
            telemetry.addData("Y (inches)", pos.getY(DistanceUnit.INCH));
            telemetry.addData("Heading (degrees)", pos.getHeading(AngleUnit.DEGREES));
            telemetry.addLine();
            telemetry.addData("Raw Forward Encoder", pinpoint.getEncoderX());
            telemetry.addData("Raw Strafe Encoder", pinpoint.getEncoderY());
            telemetry.addLine();
            telemetry.addLine("Press A when done with movement");
            telemetry.update();
            sleep(50);
        }

        // Record final position
        pinpoint.update();
        Pose2D finalPos = pinpoint.getPosition();

        // Show results
        telemetry.clear();
        telemetry.addLine("=== " + testName + " RESULTS ===");
        telemetry.addData("Final X", "%.2f inches", finalPos.getX(DistanceUnit.INCH));
        telemetry.addData("Final Y", "%.2f inches", finalPos.getY(DistanceUnit.INCH));
        telemetry.addData("Final Heading", "%.2f degrees", finalPos.getHeading(AngleUnit.DEGREES));
        telemetry.addLine();
        telemetry.addData("Raw Forward Encoder", pinpoint.getEncoderX());
        telemetry.addData("Raw Strafe Encoder", pinpoint.getEncoderY());
        telemetry.addLine();
        telemetry.addLine("Press B to continue to next test");
        telemetry.update();

        // Wait for B to continue
        while (opModeIsActive() && !gamepad1.b) {
            sleep(100);
        }

        // Wait for button release
        while (opModeIsActive() && gamepad1.b) {
            sleep(100);
        }
    }
}