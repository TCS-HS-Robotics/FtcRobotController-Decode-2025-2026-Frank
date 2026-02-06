package org.firstinspires.ftc.teamcode.Frank.opmodes.Teleop;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.configurables.annotations.IgnoreConfigurable;
import com.bylazar.configurables.PanelsConfigurables;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Frank.Robot;
import org.firstinspires.ftc.teamcode.Frank.hardware.Shooter;

@Configurable
@TeleOp(name = "Transfer Efficiency Tuner", group = "(0) Testing")
public class TransferEfficiencyTuner extends OpMode {

    // ===== CONFIGURABLE VIA PANELS =====
    public static double transferEfficiencyFar = Shooter.TRANSFER_EFFICIENCY_FAR;
    public static double transferEfficiencyClose = Shooter.TRANSFER_EFFICIENCY_CLOSE;
    public static double distanceThreshold = 80.0;
    public static double testDistance = 134.0;
    public static double basketHeight = 53.0;

    private static final String ALLIANCE_COLOR = "blue";

    @IgnoreConfigurable
    private Robot robot;

    @IgnoreConfigurable
    private TelemetryManager panelsTelemetry;

    @IgnoreConfigurable
    private boolean backButtonWasPressed = false;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        // Sync initial values from shooter
        transferEfficiencyFar = robot.shooter.getTransferEfficiencyFar();
        transferEfficiencyClose = robot.shooter.getTransferEfficiencyClose();
        distanceThreshold = robot.shooter.getDistanceThreshold();

        PanelsConfigurables.INSTANCE.refreshClass(this);

        if (robot.vision.isConnected()) {
            telemetry.addLine("Limelight: CONNECTED");
        } else {
            telemetry.addLine("Limelight: NOT CONNECTED - Press BACK on gamepad2 to restart");
        }
        telemetry.update();
    }

    @Override
    public void loop() {
        // Pull latest values from Panels and push to shooter
        PanelsConfigurables.INSTANCE.refreshClass(this);
        robot.shooter.setTransferEfficiency(transferEfficiencyFar, transferEfficiencyClose);
        robot.shooter.setDistanceThreshold(distanceThreshold);

        // Limelight restart with BACK button on gamepad2
        if (gamepad2.back && !backButtonWasPressed) {
            robot.vision.restartConnection();
            backButtonWasPressed = true;
        } else if (!gamepad2.back) {
            backButtonWasPressed = false;
        }

        telemetry.addLine("LL: " + robot.vision.getDiagnostics());

        // Drivetrain control (gamepad1)
        robot.drivetrain.driveControl();

        // Auto-shoot: Y to toggle, vision-based alignment + ballistics
        robot.handleAutoShoot(basketHeight, ALLIANCE_COLOR);

        // Shooter angle control (gamepad2 left stick + presets)
        robot.shooter.angleControl(false);

        // Shooter intake/outtake (gamepad2 triggers)
        robot.shooter.handleIntakeOuttake();
        robot.tube.handleIntakeOuttake();

        robot.tube.handleStopper();

        // Shoot three balls with dpad_up
        robot.handleShootThreeBalls();

        // Panels telemetry for transfer efficiency
        String activeEfficiency = testDistance > distanceThreshold ? "FAR" : "CLOSE";
        double activeValue = testDistance > distanceThreshold ? transferEfficiencyFar : transferEfficiencyClose;

        panelsTelemetry.debug("=== TRANSFER EFFICIENCY ===");
        panelsTelemetry.debug("FAR: " + transferEfficiencyFar);
        panelsTelemetry.debug("CLOSE: " + transferEfficiencyClose);
        panelsTelemetry.debug("Threshold: " + distanceThreshold + " in");
        panelsTelemetry.debug("Active: " + activeEfficiency + " (" + activeValue + ")");
        panelsTelemetry.debug("Motor Power: " + robot.shooter.getOutTakePower());
        panelsTelemetry.update(telemetry);

        telemetry.update();
    }
}