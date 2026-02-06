package org.firstinspires.ftc.teamcode.Stephon.opmodes.Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Stephon.Robot;


@TeleOp(name = "🔵 BLUE Alliance TeleOp", group = "1")
public class BlueAllianceTeleop extends OpMode {
    private Robot stephon;

    // Constants
    private static final double BASKET_HEIGHT = 53;
    private static final String ALLIANCE_COLOR = "blue";

    private double autoShooterCorrection = -1;

    // Limelight restart debounce
    private boolean backButtonWasPressed = false;


    @Override
    public void init() {
        stephon = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);

        // Show Limelight connection status during init
        if (stephon.vision.isConnected()) {
            telemetry.addLine("Limelight: CONNECTED");
        } else {
            telemetry.addLine("Limelight: NOT CONNECTED - Press BACK on gamepad2 to restart");
        }
        telemetry.update();
    }

    @Override
    public void loop() {
        // Limelight restart with BACK button on gamepad2 (for connection failures)
        if (gamepad2.back && !backButtonWasPressed) {
            stephon.vision.restartConnection();
            backButtonWasPressed = true;
        } else if (!gamepad2.back) {
            backButtonWasPressed = false;
        }

        // Show detailed Limelight diagnostics
        telemetry.addLine("LL: " + stephon.vision.getDiagnostics());

        stephon.drivetrain.driveControl();


        // Toggle auto-shoot with proper debouncing
        stephon.handleAutoShoot(BASKET_HEIGHT, ALLIANCE_COLOR);

        stephon.shooter.angleControl(false);

        // Shooter intake and outtake. They have to be separate because the shooter needs time to warm up when outtaking so they must be controlled independantly
        //stephon.handleIntakeOuttake();
//        stephon.handleIntake(false);
        stephon.shooter.handleIntakeOuttake();
        stephon.tube.handleIntakeOuttake();

        stephon.tube.handleStopper();
        //stephon.tube.handleIntakeAssist();

        // Shoot three balls with dpad_up (same as autonomous)
        stephon.handleShootThreeBalls();

        telemetry.update();
    }
}