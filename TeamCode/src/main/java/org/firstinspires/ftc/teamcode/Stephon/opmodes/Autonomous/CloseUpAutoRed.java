package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Stephon.Robot;

@Autonomous(name="Close Up Auto Red", group="2")
public class CloseUpAutoRed extends LinearOpMode {

    Robot robot;
    ElapsedTime runtime = new ElapsedTime();

    // Constants
    String ALLIANCE_COLOR = "red";
    private static final double BASKET_HEIGHT = 53;

    // Timing constants
    int BACKUP_TIME = 1750;             // 2 seconds to back up
    int AUTO_AIM_TIME = 2000;           // 2 seconds to auto-aim
    int TIME_TO_SHOOT = 1100;           // Time per shot
    int WAIT_BETWEEN_SHOTS = 1550;      // Wait between shots for shooter to spin up
    int STRAFE_TIME = 1500;             // 1.5 seconds to strafe out

    // Speed constants
    double BACKUP_SPEED = 0.3;
    double STRAFE_SPEED = 0.4;

    @Override
    public void runOpMode() {

        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);

        telemetry.addLine("Close Up Auto - RED Alliance");
        telemetry.addLine("Ready to start");
        telemetry.update();

        waitForStart();

        // --- STATE 1: Back up for 2 seconds ---
        telemetry.addLine("STATE 1: Backing up...");
        telemetry.update();
        robot.drivetrain.driveBackward(BACKUP_SPEED);
        runtime.reset();
        while (opModeIsActive() && (runtime.milliseconds() < BACKUP_TIME)) {
            telemetry.addLine("STATE 1: Backing up...");
            telemetry.addData("Time", "%.1f / 2.0 sec", runtime.milliseconds() / 1000.0);
            telemetry.update();
        }
        robot.drivetrain.stop();

        // --- STATE 2: Auto-aim until aligned (max 2 seconds) ---
        robot.autoShootIsActive = true;
        runtime.reset();
        while (opModeIsActive() && (runtime.milliseconds() < AUTO_AIM_TIME)) {
            robot.handleAutoShoot(BASKET_HEIGHT, ALLIANCE_COLOR);
            telemetry.addLine("STATE 2: Auto-aiming...");
            telemetry.addData("Time", "%.1f / 2.0 sec", runtime.milliseconds() / 1000.0);
            telemetry.update();
        }
        robot.autoShootIsActive = false;
        robot.drivetrain.stop();

        // --- STATE 3: Shoot three balls ---
        telemetry.addLine("STATE 3: Shooting...");
        telemetry.update();
        robot.shootThreeBalls(TIME_TO_SHOOT, WAIT_BETWEEN_SHOTS, runtime);

        // --- STATE 4: Put shooter to minimum (flat) position ---
        telemetry.addLine("STATE 4: Lowering shooter...");
        telemetry.update();
        robot.shooter.setTiltAngle(robot.shooter.MIN);

        // --- STATE 5: Strafe LEFT (red team) ---
        robot.drivetrain._strafeLeft(STRAFE_SPEED);
        runtime.reset();
        while (opModeIsActive() && (runtime.milliseconds() < STRAFE_TIME)) {
            telemetry.addLine("STATE 5: Strafing LEFT...");
            telemetry.addData("Time", "%.1f / 1.5 sec", runtime.milliseconds() / 1000.0);
            telemetry.update();
        }
        robot.drivetrain.stop();

        telemetry.addLine("Autonomous Complete!");
        telemetry.update();
    }
}