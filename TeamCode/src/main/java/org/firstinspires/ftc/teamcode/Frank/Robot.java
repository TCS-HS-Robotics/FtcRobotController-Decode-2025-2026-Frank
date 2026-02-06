package org.firstinspires.ftc.teamcode.Frank;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Frank.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.Frank.hardware.Shooter;
import org.firstinspires.ftc.teamcode.Frank.hardware.Tube;
import org.firstinspires.ftc.teamcode.Frank.subsystems.VisionSystem;
import org.firstinspires.ftc.teamcode.Frank.util.AprilTag;

// Connect to robot from wifi: adb connect 192.168.43.1:5555

public class Robot {

    public Drivetrain drivetrain;
    public Shooter shooter;
    public Tube tube;
    public VisionSystem vision;
    public IMU imu;  // For MegaTag2 localization accuracy

    private LED LEDTagIndicatorRedLeft;
    private LED LEDTagIndicatorGreenLeft;
    private LED LEDTagIndicatorRedRight;
    private LED LEDTagIndicatorGreenRight;

    //private HardwareMap hw;
    private Telemetry telemetry;
    private Gamepad gamepad1, gamepad2;

    // Switches
    public boolean autoShootIsActive = false;
    private boolean yButtonWasPressed = false;
    private boolean dpadUpWasPressed2 = false; // Debounce for shoot three balls

    // Runtime for shoot three balls in teleop
    private ElapsedTime shootRuntime = new ElapsedTime();


    // Shooter mode control
    public enum ShooterMode {
        AUTOMATIC,  // Auto-detect motor speed and fire
        MANUAL      // Driver controls with D-pad
    }
    private ShooterMode shooterMode = ShooterMode.AUTOMATIC; // Default to automatic
    private boolean dpadUpWasPressed = false; // Debounce for D-pad up

    // Constants
    // Camera offset: negative for blue, positive for red
    private final double BLUE_OFFSET_CAM_TO_SHOOTER = -5; // in inches
    private final double RED_OFFSET_CAM_TO_SHOOTER = 5;   // in inches (opposite sign)

    // Camera position offset - camera is 16" BEHIND the shooter
    // When camera measures distance D, actual shooter distance is D - 16
    private final double CAMERA_TO_SHOOTER_DISTANCE = 16.0; // inches

    // Angle-based correction feature flag - SET TO FALSE TO DISABLE
    private final boolean USE_ANGLE_CORRECTION = true;
    // Correction strength: 1.0 = subtle, 1.5 = moderate, 2.0 = aggressive
    private final double ANGLE_CORRECTION_POWER = 1.5;

    public Robot(HardwareMap hw, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2) {
        this(hw, telemetry, gamepad1, gamepad2, true); // Default: initialize drivetrain
    }

    /**
     * Constructor with option to skip drivetrain initialization.
     * Use skipDrivetrain=true for autonomous when Pedro Pathing controls the motors.
     *
     * @param hw HardwareMap
     * @param telemetry Telemetry
     * @param gamepad1 Gamepad 1 (can be null for autonomous)
     * @param gamepad2 Gamepad 2 (can be null for autonomous)
     * @param initDrivetrain false to skip drivetrain (for Pedro Pathing autonomous)
     */
    public Robot(HardwareMap hw, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2, boolean initDrivetrain) {
        this.telemetry = telemetry;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;

        // Only initialize drivetrain if requested (skip for Pedro Pathing autonomous)
        if (initDrivetrain) {
            this.drivetrain = new Drivetrain(hw, telemetry, gamepad1);
        } else {
            this.drivetrain = null;
        }

        this.shooter = new Shooter(hw, telemetry, gamepad2);
        this.tube = new Tube(hw, telemetry, gamepad2);
        this.vision = new VisionSystem(hw, telemetry);

        // Initialize IMU for MegaTag2 localization (field-centric aiming)
        this.imu = hw.get(IMU.class, "imu");
        IMU.Parameters imuParams = new IMU.Parameters(
            new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD
            )
        );
        this.imu.initialize(imuParams);

        this.LEDTagIndicatorRedLeft = hw.get(LED.class, "LEDTagIndicatorRedLeft");
        this.LEDTagIndicatorGreenLeft = hw.get(LED.class, "LEDTagIndicatorGreenLeft");

        this.LEDTagIndicatorRedRight = hw.get(LED.class, "LEDTagIndicatorRedRight");
        this.LEDTagIndicatorGreenRight = hw.get(LED.class, "LEDTagIndicatorGreenRight");

        this.LEDTagIndicatorRedLeft.off();
        this.LEDTagIndicatorGreenLeft.off();

        this.LEDTagIndicatorRedRight.off();
        this.LEDTagIndicatorGreenRight.off();
    }

    public void handleIntake(boolean telemetryActive) {

        // Handle intake
        if (this.gamepad2.left_trigger > 0.5) {
            this._intake();
        }
        // Stop when no triggers pressed
        else {
            this.shooter.stopShooter();
            this.tube.stop();
        }

        if (telemetryActive) {
            telemetry.addLine("Shooter Power: " + this.shooter.getOutTakePower());
            telemetry.addLine("Mode: " + shooterMode);
        }

    }

    public void handleAutoShoot(double basketHeight, String allianceColor) {

        // Update vision results from Limelight
        vision.mainLoop(null);

        // Update vision system with IMU heading for MegaTag2 accuracy
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        vision.updateRobotOrientation(heading);

        if (this.gamepad2.y && !yButtonWasPressed) {
            this.autoShootIsActive = !this.autoShootIsActive;
            yButtonWasPressed = true;
        } else if (!this.gamepad2.y) {
            yButtonWasPressed = false;
        }

        // Get the alliance color tag (ignoring code tags)
        AprilTag tag = this.vision.getAllianceColorTag(allianceColor);

        // Set LEDs based on final state (turn off first, then on to avoid color mixing)
        if (!this.autoShootIsActive) {
            // Not auto shooting: all LEDs off
            this.LEDTagIndicatorRedLeft.off();
            this.LEDTagIndicatorGreenLeft.off();
            this.LEDTagIndicatorRedRight.off();
            this.LEDTagIndicatorGreenRight.off();
        } else if (tag != null) {
            // Auto shooting and sees tag: green on, red off
            this.LEDTagIndicatorRedLeft.off();
            this.LEDTagIndicatorRedRight.off();
            this.LEDTagIndicatorGreenLeft.on();
            this.LEDTagIndicatorGreenRight.on();
        } else {
            // Auto shooting but no tag: red on, green off
            this.LEDTagIndicatorGreenLeft.off();
            this.LEDTagIndicatorGreenRight.off();
            this.LEDTagIndicatorRedLeft.on();
            this.LEDTagIndicatorRedRight.on();
        }

        if (tag != null && this.autoShootIsActive) {
            telemetry.addLine("Sees correct alliance tag");

            // ===== ANGLE-BASED CORRECTION START (COMMENTED OUT - Using ridge aiming instead) =====
//            // Calculate angle to tag (in radians)
//            double angleToTag = Math.atan2(tag.x, tag.y);
//            double angleToTagDegrees = Math.toDegrees(angleToTag);
//
//            // Base correction value
//            double baseCorrection = (allianceColor.equals("blue")) ? BLUE_SHOOTING_CORRECTION : RED_SHOOTING_CORRECTION;
//
//            // Apply angle-based scaling if enabled
//            double correction;
//            if (USE_ANGLE_CORRECTION) {
//                // When perpendicular (0°), use full correction
//                // When at an angle, reduce correction proportionally
//                double correctionFactor = Math.pow(Math.cos(angleToTag), ANGLE_CORRECTION_POWER);
//                correction = baseCorrection * correctionFactor;
//
//                // Debug telemetry
//                telemetry.addData("Angle to Tag", "%.1f°", angleToTagDegrees);
//                telemetry.addData("Correction Factor", "%.2f", correctionFactor);
//                telemetry.addData("Base Correction", "%.2f", baseCorrection);
//                telemetry.addData("Adjusted Correction", "%.2f", correction);
//            } else {
//                // Use fixed correction (original behavior)
//                correction = baseCorrection;
//                telemetry.addLine("Angle correction: DISABLED");
//            }
            // ===== ANGLE-BASED CORRECTION END =====

            // Camera offset correction: use alliance-specific offset
            // Blue = -5, Red = +5 (opposite signs due to field symmetry)
            double offsetCamToShooter = allianceColor.equals("blue") ? BLUE_OFFSET_CAM_TO_SHOOTER : RED_OFFSET_CAM_TO_SHOOTER;
            double targetX = -offsetCamToShooter;
            double error = tag.x - targetX; // Positive = need to turn right, negative = turn left

            // Alignment parameters
            double buffer = 0.25; // Dead zone - stop correcting when within this range
            double maxTurnSpeed = 0.2; // Maximum turn speed
            double minTurnSpeed = 0.08; // Minimum turn speed (motors can't go much lower)
            double proportionalGain = 0.025; // Higher = more aggressive, Lower = smoother/slower

            // Proportional control: turn slower as you get closer to aligned
            // turnSpeed scales with error - bigger error = faster turn, smaller error = slower turn
            double turnSpeed = Math.abs(error) * proportionalGain;
            turnSpeed = Math.max(minTurnSpeed, Math.min(maxTurnSpeed, turnSpeed)); // Clamp to range

            // Debug telemetry
            telemetry.addData("Tag X", "%.2f", tag.x);
            telemetry.addData("Target X", "%.2f", targetX);
            telemetry.addData("Error", "%.2f", error);
            telemetry.addData("Turn Speed", "%.3f", turnSpeed);

            if (error > buffer) {
                this.drivetrain.turnRight(turnSpeed);
                telemetry.addLine("Turning RIGHT");
            } else if (error < -buffer) {
                this.drivetrain.turnLeft(turnSpeed);
                telemetry.addLine("Turning LEFT");
            } else {
                this.drivetrain.stop();
                telemetry.addLine("ALIGNED - Stopped");
            }

            //tag.printTelemetry(telemetry);

            // Aim at the back ridge/peak of the goal (10" behind AprilTag)
            // This automatically targets the visible triangle with most surface area
            // Subtract camera offset since camera is behind shooter
            double distance = tag.y + 10 - CAMERA_TO_SHOOTER_DISTANCE;

            // DEBUG: Show distance calculation
            telemetry.addData("Tag Y (camera dist)", "%.1f\"", tag.y);
            telemetry.addData("Ridge offset", "+10\"");
            telemetry.addData("Camera offset", "-%.0f\"", CAMERA_TO_SHOOTER_DISTANCE);
            telemetry.addData("Final distance", "%.1f\"", distance);

            if (distance <= 0) {
                telemetry.addLine("⚠️ DISTANCE NEGATIVE - TOO CLOSE!");
                this.shooter.stopShooter();
            } else {
                this.shooter.goToOptimalShootingAngleAndSpeed(distance, basketHeight);
            }
            telemetry.update();
        } else if (this.autoShootIsActive) {
            // Auto shooting but no tag visible (LEDs already set above)
            telemetry.addLine("no tag detected");
            telemetry.update();
        } else {
            // Not auto shooting (LEDs already set above)
            telemetry.addLine("Auto shoot disabled");
            telemetry.update();
        }
    }

    public void shootThreeBalls(int timeToShoot, int waitBetweenShots, ElapsedTime runtime) {
        // Get Shooter up to speed
        this.shooter.outTake();

        // Run tube intake for 1000ms while shooter spins up (positions balls)
        this.tube.inTake();
        runtime.reset();
        while (runtime.milliseconds() < 750) {
            telemetry.addLine("Positioning balls...");
            telemetry.update();
        }
        this.tube.stop();

        // Wait remaining time for shooter to spin up (3000 - 1000 = 2000ms)
        runtime.reset();
        while (runtime.milliseconds() < 2000) {
            telemetry.addLine("Waiting for shooter to spin up...");
            telemetry.update();
        }

        // Shoot first ball
        shootOneBall(timeToShoot, runtime);

        // Wait for shooter to ramp back up
        runtime.reset();
        while (runtime.milliseconds() < waitBetweenShots) {
            telemetry.addLine("Waiting for shooter to ramp up...");
            telemetry.update();
        }

        // Shoot second ball
        shootOneBall(timeToShoot, runtime);

        // Wait for shooter to ramp back up
        runtime.reset();
        while (runtime.milliseconds() < waitBetweenShots) {
            telemetry.addLine("Waiting for shooter to ramp up...");
            telemetry.update();
        }

        // Shoot third ball
        shootOneBall(timeToShoot, runtime);

        this.shooter.stopShooter();
    }

    public void shootOneBall(double timeToShoot, ElapsedTime runtime) {
        runtime.reset();
        this.tube.outTake();
        while (runtime.milliseconds() < timeToShoot) {
            telemetry.addLine("Shooting...");
            telemetry.update();
        }
        this.tube.stop();
    }

    /**
     * Aim shooter using vision WITHOUT turning the drivetrain.
     * Use this in autonomous when Pedro Pathing handles positioning.
     *
     * @param basketHeight Height of target basket
     * @param allianceColor "blue" or "red"
     * @return true if tag was detected and shooter was aimed, false otherwise
     */
    public boolean aimShooterOnly(double basketHeight, String allianceColor) {
        // Update vision
        vision.mainLoop(null);

        // Update vision with IMU heading for accuracy
        double heading = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        vision.updateRobotOrientation(heading);

        // Get the alliance color tag
        AprilTag tag = this.vision.getAllianceColorTag(allianceColor);

        if (tag != null) {
            // Calculate distance to ridge (same as handleAutoShoot)
            double distance = tag.y + 10 - CAMERA_TO_SHOOTER_DISTANCE;

            if (distance > 0) {
                // Aim shooter - this sets servo angle AND motor power
                this.shooter.goToOptimalShootingAngleAndSpeed(distance, basketHeight);

                telemetry.addLine("=== SHOOTER AIMED (NO DRIVETRAIN) ===");
                telemetry.addData("Tag Distance", "%.1f\"", tag.y);
                telemetry.addData("Shooting Distance", "%.1f\"", distance);
                return true;
            } else {
                telemetry.addLine("WARNING: Distance negative - too close!");
                return false;
            }
        } else {
            telemetry.addLine("No AprilTag detected - using preset");
            return false;
        }
    }

    /**
     * Aim shooter at a fixed preset position (no vision needed).
     * Use when you know the exact shooting position.
     *
     * @param distance Known distance to target in inches
     * @param basketHeight Height of target basket
     */
    public void aimShooterAtDistance(double distance, double basketHeight) {
        this.shooter.goToOptimalShootingAngleAndSpeed(distance, basketHeight);
        telemetry.addData("Aiming at distance", "%.1f\"", distance);
    }

    /**
     * Handle shooting three balls when gamepad2 dpad_up is pressed.
     * Uses same timing as autonomous. Call this in teleop loop.
     * WARNING: This blocks for ~10 seconds while shooting!
     */
    public void handleShootThreeBalls() {
        if (this.gamepad2.dpad_up && !dpadUpWasPressed2) {
            dpadUpWasPressed2 = true;
            // Same timing as autonomous
            int timeToShoot = 1100;
            int waitBetweenShots = 1550;
            shootThreeBalls(timeToShoot, waitBetweenShots, shootRuntime);
        } else if (!this.gamepad2.dpad_up) {
            dpadUpWasPressed2 = false;
        }
    }

//    public void handleIntakeOuttake() {
//        if (this.gamepad2.right_trigger > 0.5) {
//            this.shooter.outTake();
//        } else if (this.gamepad2.left_trigger > 0.5) {
//            this.shooter.inTake();
//            //this.tube.inTake();
//        } else {
//            this.shooter.stopShooter();
//            this.tube.stop();
//        }
//
//    }

    private void _intake() {
        this.telemetry.addLine("InTaking...");
        this.shooter.inTake();
        this.tube.inTake();
    }

    /**
     * AUTOMATIC MODE: Motors spin up, then tube automatically fires after 750ms
     */
//    private void _outtakeAutomatic() {
//        this.shooter.outTake();
//        boolean ready = this.shooter.readyToOutTake();
//        this.telemetry.addLine("Motor Ready: " + ready); // Debug info
//        if (ready) {
//            this.telemetry.addLine("OutTaking (AUTOMATIC)...");
//            this.tube.outTake();
//        }
//    }


    /**
     * MANUAL MODE: Driver controls tube outtake with D-pad Up
     * Motors spin up on right trigger, tube fires when driver presses D-pad Up
     */
//    private void _outtakeManual() {
//        //this.telemetry.addLine("OutTaking (MANUAL)...");
//
//        // Always keep motors running while right trigger is held
//        this.shooter.outTake();
//
//        // Check for D-pad UP to fire the tube
//        if (this.gamepad2.right_bumper) {
//            this.tube.outTake();
//        } else {
//            this.tube.stop();
//        }
//    }

//    private void _resetShooterTiming() {
//        shooterActivationTime = 0;
//        tubeHasBeenFired = false;
//    }

}
