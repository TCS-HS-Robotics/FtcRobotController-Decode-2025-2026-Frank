package org.firstinspires.ftc.teamcode.Frank.opmodes.Teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Frank.Robot;


@TeleOp(name = "🔴 RED Alliance TeleOp", group = "2")
public class RedAllianceTeleop extends OpMode {
    private Robot frank;

    // Constants
    private static final double BASKET_HEIGHT = 53;
    private static final String ALLIANCE_COLOR = "red";

    private double autoShooterCorrection = 1;

    // Limelight restart debounce
    private boolean backButtonWasPressed = false;


    @Override
    public void init() {
        frank = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);

        // Show Limelight connection status during init
        if (frank.vision.isConnected()) {
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
            frank.vision.restartConnection();
            backButtonWasPressed = true;
        } else if (!gamepad2.back) {
            backButtonWasPressed = false;
        }

        // Show detailed Limelight diagnostics
        telemetry.addLine("LL: " + frank.vision.getDiagnostics());

        frank.drivetrain.driveControl();


        // Toggle auto-shoot with proper debouncing
        frank.handleAutoShoot(BASKET_HEIGHT, ALLIANCE_COLOR);

        frank.shooter.angleControl(false);

        // Shooter intake and outtake. They have to be separate because the shooter needs time to warm up when outtaking so they must be controlled independantly
        //frank.handleIntakeOuttake();
//        frank.handleIntake(false);
//        frank.tube.handleOuttake();
//        frank.shooter.handleOuttake();
        frank.shooter.handleIntakeOuttake();
        frank.tube.handleIntakeOuttake();

        //frank.tube.handleStopper();
        frank.tube.handleStopper();

        // Shoot three balls with dpad_up (same as autonomous)
        frank.handleShootThreeBalls();

        telemetry.update();
    }
}
