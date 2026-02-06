package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Stephon.Robot;

@Autonomous(name="Back Stephon Auto Red", group="1")
public class BackStephonAutoRed extends LinearOpMode {

    Robot robot;
    ElapsedTime runtime = new ElapsedTime();

    // Constants
    String ALLIANCE_COLOR = "red";
    private static final double BASKET_HEIGHT = 53;

    // Timing constants
    int AUTO_AIM_TIME = 2000;           // 2 seconds to auto-aim
    int TIME_TO_SHOOT = 1100;           // Time per shot
    int WAIT_BETWEEN_SHOTS = 1550;      // Wait between shots for shooter to spin up
    int DRIVE_OUT_TIME = 1200;          //
    // 2 seconds to drive out of launch zone

    @Override
    public void runOpMode() {

        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);

        telemetry.addLine("Ready to start");
        telemetry.update();

        waitForStart();

        // --- STATE 1: Auto-aim until aligned (max 2 seconds) ---
        robot.autoShootIsActive = true;
        runtime.reset();
        while (opModeIsActive() && (runtime.milliseconds() < AUTO_AIM_TIME)) {
            robot.handleAutoShoot(BASKET_HEIGHT, ALLIANCE_COLOR);
            telemetry.addLine("STATE 1: Auto-aiming...");
            telemetry.addData("Time", "%.1f / 2.0 sec", runtime.milliseconds() / 1000.0);
            telemetry.update();
        }
        robot.autoShootIsActive = false;
        robot.drivetrain.stop();

        // --- STATE 2: Shoot three balls ---
        telemetry.addLine("STATE 2: Shooting...");
        telemetry.update();
        robot.shootThreeBalls(TIME_TO_SHOOT, WAIT_BETWEEN_SHOTS, runtime);

        // --- STATE 3: Put shooter to minimum (flat) position ---
        telemetry.addLine("STATE 3: Lowering shooter...");
        telemetry.update();
        robot.shooter.setTiltAngle(robot.shooter.MIN);

        // --- STATE 4: Drive forward for 2 seconds out of launch zone ---
        robot.drivetrain.driveStraight(0.3);
        runtime.reset();
        while (opModeIsActive() && (runtime.milliseconds() < DRIVE_OUT_TIME)) {
            telemetry.addLine("STATE 4: Driving out of launch zone...");
            telemetry.addData("Time", "%.1f / 2.0 sec", runtime.milliseconds() / 1000.0);
            telemetry.update();
        }
        robot.drivetrain.stop();

        telemetry.addLine("Autonomous Complete!");
        telemetry.update();
    }
}
