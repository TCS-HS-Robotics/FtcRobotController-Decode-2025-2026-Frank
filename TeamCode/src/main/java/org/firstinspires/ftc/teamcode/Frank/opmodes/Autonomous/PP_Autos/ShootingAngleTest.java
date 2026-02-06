package org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous.PP_Autos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.teamcode.Frank.Robot;
import org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous.pedroPathing.Constants;
import org.firstinspires.ftc.teamcode.Frank.subsystems.LocalizationSystem;

/**
 * TEST AUTONOMOUS: Find the optimal SHOOTING_ANGLE
 *
 * This OpMode drives to a scoring position and uses auto-aim to find
 * the exact heading angle your robot should use when shooting.
 *
 * INSTRUCTIONS:
 * 1. Set START_POSE to your starting position
 * 2. Set END_POSE to your scoring position (where you want to shoot from)
 * 3. Set ALLIANCE_COLOR to "red" or "blue"
 * 4. Run the auto - it will drive to END_POSE and auto-aim for 10 seconds
 * 5. Read the "FINAL HEADING" value from telemetry - that's your SHOOTING_ANGLE!
 */
@Autonomous(name = "🔧 Shooting Angle Test", group = "(0) Testing")
public class ShootingAngleTest extends OpMode {

    private Follower follower;
    private Robot robot;
    private LocalizationSystem localization;
    private ElapsedTime timer;
    private ElapsedTime totalTimer;

    // =====================================================
    // CONFIGURE THESE VALUES FOR YOUR TEST
    // =====================================================

    // Alliance color: "red" or "blue"
    private static final String ALLIANCE_COLOR = "blue";

    // Starting position (where robot starts)
    private final Pose START_POSE = new Pose(56.957, 8.554, Math.toRadians(90));

    // Ending/scoring position (where robot will shoot from)
    // NOTE: The heading here will be adjusted during path following
    private final Pose END_POSE = new Pose(56.957, 14.95);

    // Initial heading guess for interpolation (adjust if needed)
    private static final double INITIAL_HEADING_GUESS = 112; // degrees

    // Basket height for auto-aim calculation
    private static final double BASKET_HEIGHT = 53.0;

    // Time to wait for auto-aim stabilization (milliseconds)
    private static final int AUTO_AIM_TIME = 10000;

    // =====================================================

    private PathChain drivePath;

    private enum TestState {
        DRIVING,
        INIT_DRIVETRAIN,
        AUTO_AIMING,
        DISPLAY_RESULT
    }

    private TestState currentState = TestState.DRIVING;
    private double finalHeading = 0;
    private double startingHeading = 0; // Heading when path completes (from Pedro Pathing)
    private boolean tagDetected = false;

    @Override
    public void init() {
        // Initialize Pedro Pathing follower
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(START_POSE);

        // Initialize robot WITHOUT drivetrain (Pedro Pathing controls motors)
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);

        timer = new ElapsedTime();
        totalTimer = new ElapsedTime();

        // Build path from start to end position
        drivePath = follower.pathBuilder()
                .addPath(new BezierLine(START_POSE, END_POSE))
                .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(INITIAL_HEADING_GUESS))
                .build();

        // Lower shooter to starting position
        robot.shooter.setTiltAngle(robot.shooter.MIN);

        telemetry.addLine("=== SHOOTING ANGLE TEST ===");
        telemetry.addLine();
        telemetry.addData("Alliance", ALLIANCE_COLOR);
        telemetry.addData("Start", "(%.2f, %.2f) @ %.1f°",
            START_POSE.getX(), START_POSE.getY(), Math.toDegrees(START_POSE.getHeading()));
        telemetry.addData("End", "(%.2f, %.2f)", END_POSE.getX(), END_POSE.getY());
        telemetry.addData("Initial Heading Guess", "%.1f°", INITIAL_HEADING_GUESS);
        telemetry.addLine();
        telemetry.addLine("Press START when ready");
        telemetry.update();
    }

    @Override
    public void start() {
        timer.reset();
        totalTimer.reset();
        follower.followPath(drivePath);
        currentState = TestState.DRIVING;
    }

    @Override
    public void loop() {
        // Only update follower during DRIVING state - stops Pedro Pathing motor control after path completes
        if (currentState == TestState.DRIVING) {
            follower.update();
        }

        Pose currentPose = follower.getPose();

        switch (currentState) {

            case DRIVING:
                telemetry.addLine("=== DRIVING TO POSITION ===");
                telemetry.addData("X", "%.2f", currentPose.getX());
                telemetry.addData("Y", "%.2f", currentPose.getY());
                telemetry.addData("Heading", "%.2f°", Math.toDegrees(currentPose.getHeading()));

                if (!follower.isBusy()) {
                    // Save the heading from Pedro Pathing before switching
                    startingHeading = Math.toDegrees(currentPose.getHeading());
                    currentState = TestState.INIT_DRIVETRAIN;
                }
                break;

            case INIT_DRIVETRAIN:
                // Now that Pedro Pathing is done, reinitialize Robot WITH drivetrain
                telemetry.addLine("=== INITIALIZING DRIVETRAIN ===");
                telemetry.addLine("Please wait...");
                telemetry.update();

                // Create new Robot instance WITH drivetrain for auto-aiming
                robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, true);

                // Initialize LocalizationSystem for Pinpoint heading tracking
                localization = new LocalizationSystem(hardwareMap, telemetry);

                // Enable auto-shoot mode
                robot.autoShootIsActive = true;

                timer.reset();
                currentState = TestState.AUTO_AIMING;
                break;

            case AUTO_AIMING:
                // Update Pinpoint to get fresh heading data
                localization.update();

                // Use the full handleAutoShoot which turns the drivetrain to align
                robot.handleAutoShoot(BASKET_HEIGHT, ALLIANCE_COLOR);

                // Get heading: starting heading (from Pedro Pathing) + change (from LocalizationSystem)
                // LocalizationSystem returns relative heading since it was initialized
                double relativeHeading = localization.getHeading();
                finalHeading = startingHeading + relativeHeading;

                // Check if we can see tag
                tagDetected = robot.vision.getAllianceColorTag(ALLIANCE_COLOR) != null;

                double elapsed = timer.milliseconds();
                double remaining = (AUTO_AIM_TIME - elapsed) / 1000.0;

                telemetry.addLine();
                telemetry.addLine("=== AUTO-AIMING (DRIVETRAIN ACTIVE) ===");
                telemetry.addLine();
                telemetry.addData("Time Remaining", "%.1f seconds", Math.max(0, remaining));
                telemetry.addData("Tag Detected", tagDetected ? "YES - ALIGNING" : "NO - SEARCHING");
                telemetry.addData("Starting Heading", "%.2f°", startingHeading);
                telemetry.addData("Relative Change", "%.2f°", relativeHeading);
                telemetry.addLine();
                telemetry.addLine(">>> CURRENT HEADING: " + String.format("%.2f°", finalHeading) + " <<<");

                if (elapsed >= AUTO_AIM_TIME) {
                    currentState = TestState.DISPLAY_RESULT;
                }
                break;

            case DISPLAY_RESULT:
                // Stop all mechanisms including drivetrain
                robot.shooter.stopShooter();
                robot.tube.stop();
                if (robot.drivetrain != null) {
                    robot.drivetrain.stop();
                }

                // Display final result prominently
                telemetry.addLine("╔════════════════════════════════════════╗");
                telemetry.addLine("║           TEST COMPLETE!               ║");
                telemetry.addLine("╚════════════════════════════════════════╝");
                telemetry.addLine();
                telemetry.addLine("Alliance: " + ALLIANCE_COLOR.toUpperCase());
                telemetry.addLine();
                telemetry.addLine("▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼");
                telemetry.addLine();
                telemetry.addData("   FINAL HEADING (SHOOTING_ANGLE)", "%.2f°", finalHeading);
                telemetry.addLine();
                telemetry.addLine("▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲");
                telemetry.addLine();
                telemetry.addData("Tag was detected", tagDetected ? "YES" : "NO");
                telemetry.addLine();
                telemetry.addLine("Copy this value to your autonomous:");
                telemetry.addLine("  private static final double SHOOTING_ANGLE = " +
                    String.format("%.1f", finalHeading) + ";");
                telemetry.addLine();
                telemetry.addData("Final Position", "(%.2f, %.2f)",
                    currentPose.getX(), currentPose.getY());
                break;
        }

        telemetry.update();
    }
}