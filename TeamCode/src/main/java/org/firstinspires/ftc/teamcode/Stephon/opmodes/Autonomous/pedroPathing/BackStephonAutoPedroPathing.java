//package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous.pedroPathing;
//
//import com.pedropathing.follower.Follower;
//import com.pedropathing.geometry.BezierCurve;
//import com.pedropathing.geometry.BezierLine;
//import com.pedropathing.geometry.Pose;
//import com.pedropathing.paths.PathChain;
//import com.pedropathing.telemetry.SelectableOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//
//import org.firstinspires.ftc.teamcode.Stephon.Robot;
//import org.firstinspires.ftc.teamcode.Stephon.hardware.Shooter;
//import org.firstinspires.ftc.teamcode.Stephon.util.AprilTag;
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
//
//@Autonomous(name = "Back Stephon Auto (Selectable)", group = "Stephon")
//public class BackStephonAutoPedroPathing extends SelectableOpMode {
//
//    public BackStephonAutoPedroPathing() {
//        super("Select An Auto", s -> {
//            s.add("Blue Alliance", BackBlueAuto::new);
//            s.add("Red Alliance", BackRedAuto::new);
//        });
//    }
//}
//
//class BackBlueAuto extends OpMode {
//
//    private Robot robot;
//    private Follower follower;
//    private Paths paths;
//
//    // State machine
//    private enum AutoState {
//        DETECT_CODE,
//        SHOOT_PRELOAD,
//        PICKUP,
//        SHOOT_BALLS,
//        LEAVE_LAUNCH_ZONE,
//        IDLE
//    }
//
//    private AutoState currentState = AutoState.DETECT_CODE;
//    private int detectedRow = -1; // 1, 2, or 3
//    private long stateStartTime = 0;
//    private long detectCodeStartTime = 0;
//    private final long INTAKE_RAMP_TIME = 500; // milliseconds for intake to ramp up
//    private final long PICKUP_DURATION = 1500; // milliseconds to hold at pickup
//    private final long SHOOT_COMPLETE_TIME = 5000; // milliseconds for balls to exit shooter
//    private final long DETECT_CODE_TIMEOUT = 5000; // milliseconds to wait for April tag detection
//    private boolean intakeRamped = false;
//
//    // Shooter calibration values for each position (TODO: tune these values)
//    private final Shooter.ServoPos PRELOAD_SERVO = new Shooter.ServoPos(0.5, 0.45); // Tilt positions for preload
//    private final double PRELOAD_SPEED = 0.8; // Motor speed for preload shot
//
//    private final Shooter.ServoPos ROW1_SERVO = new Shooter.ServoPos(0.45, 0.40); // Tilt positions for row 1
//    private final double ROW1_SPEED = 0.75;
//
//    private final Shooter.ServoPos ROW2_SERVO = new Shooter.ServoPos(0.50, 0.45); // Tilt positions for row 2
//    private final double ROW2_SPEED = 0.80;
//
//    private final Shooter.ServoPos ROW3_SERVO = new Shooter.ServoPos(0.55, 0.50); // Tilt positions for row 3
//    private final double ROW3_SPEED = 0.85;
//
//    // Constants
//    private final String ALLIANCE_COLOR = "blue";
//    private final double BASKET_HEIGHT = 2.0; // inches above ground
//
//    @Override
//    public void init() {
//        // Initialize Robot with empty gamepad (autonomous doesn't use gamepads)
//        robot = new Robot(hardwareMap, telemetry, new EmptyGamepad(), new EmptyGamepad());
//
//        // Initialize Pedro Pathing Follower using Constants
//        follower = Constants.createFollower(hardwareMap);
//        follower.setStartingPose(new Pose(63.01182654402103, 9.5, Math.toRadians(90)));
//
//        // Create paths for all three rows
//        paths = new Paths(follower);
//
//        // Stop the intake initially
//        robot.shooter.stopShooter();
//        robot.tube.stop();
//
//        telemetry.addLine("Back Blue Auto initialized. Waiting for start...");
//        telemetry.update();
//    }
//
//    @Override
//    public void loop() {
//        // Update follower and vision system
//        follower.update();
//        robot.vision.telemetryTags();
//
//        switch (currentState) {
//            case DETECT_CODE:
//                handleDetectCode();
//                break;
//            case SHOOT_PRELOAD:
//                handleShootPreload();
//                break;
//            case PICKUP:
//                handlePickup();
//                break;
//            case SHOOT_BALLS:
//                handleShootBalls();
//                break;
//            case LEAVE_LAUNCH_ZONE:
//                handleLeaveLaunchZone();
//                break;
//            case IDLE:
//                telemetry.addLine("Autonomous complete!");
//                break;
//        }
//
//        telemetry.addLine("Current State: " + currentState);
//        telemetry.addLine("Detected Row: " + detectedRow);
//        telemetry.addLine("Follower Busy: " + follower.isBusy());
//        telemetry.update();
//    }
//
//    private void handleDetectCode() {
//        // Initialize timer on first call
//        if (detectCodeStartTime == 0) {
//            detectCodeStartTime = System.currentTimeMillis();
//        }
//
//        // Try to detect the April tag
//        AprilTagDetection detection = robot.vision.getClosestTag();
//
//        if (detection != null) {
//            AprilTag tag = AprilTag.fromDetection(detection);
//
//            // Tags 21, 22, 23 correspond to rows 1, 2, 3 (PPG, PGP, GPP)
//            if (tag.type == AprilTag.TagType.ENCODED) {
//                if (tag.id == 21) {
//                    detectedRow = 1; // PPG
//                } else if (tag.id == 22) {
//                    detectedRow = 2; // PGP
//                } else if (tag.id == 23) {
//                    detectedRow = 3; // GPP
//                }
//
//                if (detectedRow != -1) {
//                    telemetry.addLine("Code detected! Row: " + detectedRow);
//                    transitionToState(AutoState.SHOOT_PRELOAD);
//                    detectCodeStartTime = 0;
//                    return;
//                }
//            }
//        }
//
//        // If no code detected within timeout, default to row 1
//        if (System.currentTimeMillis() - detectCodeStartTime >= DETECT_CODE_TIMEOUT) {
//            detectedRow = 1;
//            telemetry.addLine("Code detection timeout. Defaulting to Row 1");
//            transitionToState(AutoState.SHOOT_PRELOAD);
//            detectCodeStartTime = 0;
//        }
//    }
//
//    private void handleShootPreload() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the path
//                follower.followPath(paths.ShootPreLoad);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting ShootPreLoad path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 500) {
//                // Set shooter calibration and shoot
//                robot.shooter.setOutTakePower(PRELOAD_SPEED);
//                robot.shooter.setTiltAngle(PRELOAD_SERVO);
//                robot.shooter.outTake();
//                robot.tube.outTake();
//
//                // Wait for all balls to completely exit the shooter (5 seconds)
//                if (System.currentTimeMillis() - stateStartTime >= 500 + SHOOT_COMPLETE_TIME) {
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.PICKUP);
//                }
//            }
//        }
//    }
//
//    private void handlePickup() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Ramp up intake motors before actual pickup
//                robot.shooter.inTake();
//                robot.tube.inTake();
//                stateStartTime = System.currentTimeMillis();
//                intakeRamped = false;
//                telemetry.addLine("Ramping up intake motors");
//            } else {
//                long elapsedTime = System.currentTimeMillis() - stateStartTime;
//
//                // Wait for intake to ramp up
//                if (elapsedTime < INTAKE_RAMP_TIME) {
//                    telemetry.addLine("Intake ramping: " + elapsedTime + "ms");
//                } else if (!intakeRamped) {
//                    // Intake has ramped, now follow the pickup path
//                    selectPathByRow(PathType.PICKUP);
//                    intakeRamped = true;
//                    telemetry.addLine("Starting PickUp path");
//                } else if (elapsedTime >= INTAKE_RAMP_TIME + PICKUP_DURATION) {
//                    // Pickup is complete
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.SHOOT_BALLS);
//                }
//            }
//        }
//    }
//
//    private void handleShootBalls() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the shoot path
//                selectPathByRow(PathType.SHOOT_BALLS);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting ShootBalls path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 500) {
//                // Set shooter calibration for current row and shoot
//                double motorSpeed = getSpeedForRow(detectedRow);
//                Shooter.ServoPos servoPos = getServoForRow(detectedRow);
//                robot.shooter.setOutTakePower(motorSpeed);
//                robot.shooter.setTiltAngle(servoPos);
//                robot.shooter.outTake();
//                robot.tube.outTake();
//
//                // Wait for all balls to completely exit the shooter (5 seconds)
//                if (System.currentTimeMillis() - stateStartTime >= 500 + SHOOT_COMPLETE_TIME) {
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.LEAVE_LAUNCH_ZONE);
//                }
//            }
//        }
//    }
//
//    // Helper methods to get calibration values by row
//    private Shooter.ServoPos getServoForRow(int row) {
//        switch (row) {
//            case 1: return ROW1_SERVO;
//            case 2: return ROW2_SERVO;
//            case 3: return ROW3_SERVO;
//            default: return ROW1_SERVO;
//        }
//    }
//
//    private double getSpeedForRow(int row) {
//        switch (row) {
//            case 1: return ROW1_SPEED;
//            case 2: return ROW2_SPEED;
//            case 3: return ROW3_SPEED;
//            default: return ROW1_SPEED;
//        }
//    }
//
//    private void handleLeaveLaunchZone() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the leave path
//                selectPathByRow(PathType.LEAVE_LAUNCH_ZONE);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting LeaveLaunchZone path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 2000) {
//                // Path complete
//                transitionToState(AutoState.IDLE);
//            }
//        }
//    }
//
//    private void transitionToState(AutoState newState) {
//        currentState = newState;
//        stateStartTime = 0;
//        intakeRamped = false;
//        telemetry.addLine("Transitioning to: " + newState);
//    }
//
//
//    private enum PathType { PICKUP, SHOOT_BALLS, LEAVE_LAUNCH_ZONE }
//
//    private void selectPathByRow(PathType pathType) {
//        PathChain path = null;
//
//        switch (detectedRow) {
//            case 1:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.FirstRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.FirstRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.FirstRow_LeaveLaunchZone;
//                        break;
//                }
//                break;
//            case 2:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.SecondRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.SecondRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.SecondRow_Leave;
//                        break;
//                }
//                break;
//            case 3:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.ThirdRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.ThirdRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.ThirdRow_LeaveLaunchZone;
//                        break;
//                }
//                break;
//        }
//
//        if (path != null) {
//            follower.followPath(path);
//        }
//    }
//
//    // --- Paths Class for all three row trajectories ---
//    public static class Paths {
//
//        public PathChain ShootPreLoad;
//
//        // First Row (Tag 21 - PPG)
//        public PathChain FirstRow_PickUp;
//        public PathChain FirstRow_ShootBalls;
//        public PathChain FirstRow_LeaveLaunchZone;
//
//        // Second Row (Tag 22 - PGP)
//        public PathChain SecondRow_PickUp;
//        public PathChain SecondRow_ShootBalls;
//        public PathChain SecondRow_Leave;
//
//        // Third Row (Tag 23 - GPP)
//        public PathChain ThirdRow_PickUp;
//        public PathChain ThirdRow_ShootBalls;
//        public PathChain ThirdRow_LeaveLaunchZone;
//
//        public Paths(Follower follower) {
//
//            // ========== SHOOT PRELOAD (Same for all rows) ==========
//            ShootPreLoad = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(new Pose(63.012, 9.500), new Pose(63.000, 14.949))
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(113.5))
//                    .build();
//
//            // ========== FIRST ROW (Tag 21 - PPG) ==========
//            FirstRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(63.000, 14.949),
//                                    new Pose(66.607, 37.088),
//                                    new Pose(17.219, 35.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(113.5), Math.toRadians(180))
//                    .build();
//
//            FirstRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(17.219, 35.000),
//                                    new Pose(66.607, 37.088),
//                                    new Pose(63.000, 14.949)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(113.5))
//                    .build();
//
//            FirstRow_LeaveLaunchZone = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(63.000, 14.949),
//                                    new Pose(66.039, 40.305),
//                                    new Pose(103.695, 35.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(113.5), Math.toRadians(0))
//                    .build();
//
//            // ========== SECOND ROW (Tag 22 - PGP) ==========
//            SecondRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(63.000, 14.949),
//                                    new Pose(68.689, 64.526),
//                                    new Pose(18.355, 59.795)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(113.5), Math.toRadians(180))
//                    .build();
//
//            SecondRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(18.355, 59.795),
//                                    new Pose(69.256, 75.122)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(135.5))
//                    .build();
//
//            SecondRow_Leave = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(69.256, 75.122),
//                                    new Pose(102.749, 60.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(135.5), Math.toRadians(0))
//                    .build();
//
//            // ========== THIRD ROW (Tag 23 - GPP) ==========
//            ThirdRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(63.000, 9.500),
//                                    new Pose(55.254, 91.206),
//                                    new Pose(78.907, 81.556),
//                                    new Pose(18.166, 84.016)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(113.5), Math.toRadians(180))
//                    .build();
//
//            ThirdRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(18.166, 84.016),
//                                    new Pose(69.256, 75.122)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(135.5))
//                    .build();
//
//            ThirdRow_LeaveLaunchZone = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(69.256, 75.122),
//                                    new Pose(102.000, 83.070)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(135.5), Math.toRadians(0))
//                    .build();
//        }
//    }
//
//    // Empty gamepad for autonomous (no input)
//    private static class EmptyGamepad extends com.qualcomm.robotcore.hardware.Gamepad {
//        // All methods return false/0 by default
//    }
//}
//
//class BackRedAuto extends OpMode {
//
//    private Robot robot;
//    private Follower follower;
//    private Paths paths;
//
//    // State machine
//    private enum AutoState {
//        DETECT_CODE,
//        SHOOT_PRELOAD,
//        PICKUP,
//        SHOOT_BALLS,
//        LEAVE_LAUNCH_ZONE,
//        IDLE
//    }
//
//    private AutoState currentState = AutoState.DETECT_CODE;
//    private int detectedRow = -1; // 1, 2, or 3
//    private long stateStartTime = 0;
//    private long detectCodeStartTime = 0;
//    private final long INTAKE_RAMP_TIME = 500; // milliseconds for intake to ramp up
//    private final long PICKUP_DURATION = 1500; // milliseconds to hold at pickup
//    private final long SHOOT_COMPLETE_TIME = 5000; // milliseconds for balls to exit shooter
//    private final long DETECT_CODE_TIMEOUT = 5000; // milliseconds to wait for April tag detection
//    private boolean intakeRamped = false;
//
//    // Shooter calibration values for each position (TODO: tune these values)
//    private final Shooter.ServoPos PRELOAD_SERVO = new Shooter.ServoPos(0.5, 0.45); // Tilt positions for preload
//    private final double PRELOAD_SPEED = 0.8; // Motor speed for preload shot
//
//    private final Shooter.ServoPos ROW1_SERVO = new Shooter.ServoPos(0.45, 0.40); // Tilt positions for row 1
//    private final double ROW1_SPEED = 0.75;
//
//    private final Shooter.ServoPos ROW2_SERVO = new Shooter.ServoPos(0.50, 0.45); // Tilt positions for row 2
//    private final double ROW2_SPEED = 0.80;
//
//    private final Shooter.ServoPos ROW3_SERVO = new Shooter.ServoPos(0.55, 0.50); // Tilt positions for row 3
//    private final double ROW3_SPEED = 0.85;
//
//    // Constants
//    private final String ALLIANCE_COLOR = "red";
//    private final double BASKET_HEIGHT = 2.0; // inches above ground
//
//    @Override
//    public void init() {
//        // Initialize Robot with empty gamepad (autonomous doesn't use gamepads)
//        robot = new Robot(hardwareMap, telemetry, new EmptyGamepad(), new EmptyGamepad());
//
//        // Initialize Pedro Pathing Follower using Constants
//        follower = Constants.createFollower(hardwareMap);
//        // Red alliance starting pose (mirrored from blue)
//        follower.setStartingPose(new Pose(80.98817345597897, 9.5, Math.toRadians(90)));
//
//        // Create paths for all three rows
//        paths = new Paths(follower);
//
//        // Stop the intake initially
//        robot.shooter.stopShooter();
//        robot.tube.stop();
//
//        telemetry.addLine("Back Red Auto initialized. Waiting for start...");
//        telemetry.update();
//    }
//
//    @Override
//    public void loop() {
//        // Update follower and vision system
//        follower.update();
//        robot.vision.telemetryTags();
//
//        switch (currentState) {
//            case DETECT_CODE:
//                handleDetectCode();
//                break;
//            case SHOOT_PRELOAD:
//                handleShootPreload();
//                break;
//            case PICKUP:
//                handlePickup();
//                break;
//            case SHOOT_BALLS:
//                handleShootBalls();
//                break;
//            case LEAVE_LAUNCH_ZONE:
//                handleLeaveLaunchZone();
//                break;
//            case IDLE:
//                telemetry.addLine("Autonomous complete!");
//                break;
//        }
//
//        telemetry.addLine("Current State: " + currentState);
//        telemetry.addLine("Detected Row: " + detectedRow);
//        telemetry.addLine("Follower Busy: " + follower.isBusy());
//        telemetry.update();
//    }
//
//    private void handleDetectCode() {
//        // Initialize timer on first call
//        if (detectCodeStartTime == 0) {
//            detectCodeStartTime = System.currentTimeMillis();
//        }
//
//        // Try to detect the April tag
//        AprilTagDetection detection = robot.vision.getClosestTag();
//
//        if (detection != null) {
//            AprilTag tag = AprilTag.fromDetection(detection);
//
//            // Tags 21, 22, 23 correspond to rows 1, 2, 3 (PPG, PGP, GPP)
//            if (tag.type == AprilTag.TagType.ENCODED) {
//                if (tag.id == 21) {
//                    detectedRow = 1; // PPG
//                } else if (tag.id == 22) {
//                    detectedRow = 2; // PGP
//                } else if (tag.id == 23) {
//                    detectedRow = 3; // GPP
//                }
//
//                if (detectedRow != -1) {
//                    telemetry.addLine("Code detected! Row: " + detectedRow);
//                    transitionToState(AutoState.SHOOT_PRELOAD);
//                    detectCodeStartTime = 0;
//                    return;
//                }
//            }
//        }
//
//        // If no code detected within timeout, default to row 1
//        if (System.currentTimeMillis() - detectCodeStartTime >= DETECT_CODE_TIMEOUT) {
//            detectedRow = 1;
//            telemetry.addLine("Code detection timeout. Defaulting to Row 1");
//            transitionToState(AutoState.SHOOT_PRELOAD);
//            detectCodeStartTime = 0;
//        }
//    }
//
//    private void handleShootPreload() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the path
//                follower.followPath(paths.ShootPreLoad);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting ShootPreLoad path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 500) {
//                // Set shooter calibration and shoot
//                robot.shooter.setOutTakePower(PRELOAD_SPEED);
//                robot.shooter.setTiltAngle(PRELOAD_SERVO);
//                robot.shooter.outTake();
//                robot.tube.outTake();
//
//                // Wait for all balls to completely exit the shooter (5 seconds)
//                if (System.currentTimeMillis() - stateStartTime >= 500 + SHOOT_COMPLETE_TIME) {
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.PICKUP);
//                }
//            }
//        }
//    }
//
//    private void handlePickup() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Ramp up intake motors before actual pickup
//                robot.shooter.inTake();
//                robot.tube.inTake();
//                stateStartTime = System.currentTimeMillis();
//                intakeRamped = false;
//                telemetry.addLine("Ramping up intake motors");
//            } else {
//                long elapsedTime = System.currentTimeMillis() - stateStartTime;
//
//                // Wait for intake to ramp up
//                if (elapsedTime < INTAKE_RAMP_TIME) {
//                    telemetry.addLine("Intake ramping: " + elapsedTime + "ms");
//                } else if (!intakeRamped) {
//                    // Intake has ramped, now follow the pickup path
//                    selectPathByRow(PathType.PICKUP);
//                    intakeRamped = true;
//                    telemetry.addLine("Starting PickUp path");
//                } else if (elapsedTime >= INTAKE_RAMP_TIME + PICKUP_DURATION) {
//                    // Pickup is complete
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.SHOOT_BALLS);
//                }
//            }
//        }
//    }
//
//    private void handleShootBalls() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the shoot path
//                selectPathByRow(PathType.SHOOT_BALLS);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting ShootBalls path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 500) {
//                // Set shooter calibration for current row and shoot
//                double motorSpeed = getSpeedForRow(detectedRow);
//                Shooter.ServoPos servoPos = getServoForRow(detectedRow);
//                robot.shooter.setOutTakePower(motorSpeed);
//                robot.shooter.setTiltAngle(servoPos);
//                robot.shooter.outTake();
//                robot.tube.outTake();
//
//                // Wait for all balls to completely exit the shooter (5 seconds)
//                if (System.currentTimeMillis() - stateStartTime >= 500 + SHOOT_COMPLETE_TIME) {
//                    robot.shooter.stopShooter();
//                    robot.tube.stop();
//                    transitionToState(AutoState.LEAVE_LAUNCH_ZONE);
//                }
//            }
//        }
//    }
//
//    // Helper methods to get calibration values by row
//    private Shooter.ServoPos getServoForRow(int row) {
//        switch (row) {
//            case 1: return ROW1_SERVO;
//            case 2: return ROW2_SERVO;
//            case 3: return ROW3_SERVO;
//            default: return ROW1_SERVO;
//        }
//    }
//
//    private double getSpeedForRow(int row) {
//        switch (row) {
//            case 1: return ROW1_SPEED;
//            case 2: return ROW2_SPEED;
//            case 3: return ROW3_SPEED;
//            default: return ROW1_SPEED;
//        }
//    }
//
//    private void handleLeaveLaunchZone() {
//        if (!follower.isBusy()) {
//            if (stateStartTime == 0) {
//                // Start the leave path
//                selectPathByRow(PathType.LEAVE_LAUNCH_ZONE);
//                stateStartTime = System.currentTimeMillis();
//                telemetry.addLine("Starting LeaveLaunchZone path");
//            } else if (System.currentTimeMillis() - stateStartTime >= 2000) {
//                // Path complete
//                transitionToState(AutoState.IDLE);
//            }
//        }
//    }
//
//    private void transitionToState(AutoState newState) {
//        currentState = newState;
//        stateStartTime = 0;
//        intakeRamped = false;
//        telemetry.addLine("Transitioning to: " + newState);
//    }
//
//
//    private enum PathType { PICKUP, SHOOT_BALLS, LEAVE_LAUNCH_ZONE }
//
//    private void selectPathByRow(PathType pathType) {
//        PathChain path = null;
//
//        switch (detectedRow) {
//            case 1:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.FirstRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.FirstRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.FirstRow_LeaveLaunchZone;
//                        break;
//                }
//                break;
//            case 2:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.SecondRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.SecondRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.SecondRow_Leave;
//                        break;
//                }
//                break;
//            case 3:
//                switch (pathType) {
//                    case PICKUP:
//                        path = paths.ThirdRow_PickUp;
//                        break;
//                    case SHOOT_BALLS:
//                        path = paths.ThirdRow_ShootBalls;
//                        break;
//                    case LEAVE_LAUNCH_ZONE:
//                        path = paths.ThirdRow_LeaveLaunchZone;
//                        break;
//                }
//                break;
//        }
//
//        if (path != null) {
//            follower.followPath(path);
//        }
//    }
//
//    // --- Paths Class for all three row trajectories ---
//    public static class Paths {
//
//        public PathChain ShootPreLoad;
//
//        // First Row (Tag 21 - PPG)
//        public PathChain FirstRow_PickUp;
//        public PathChain FirstRow_ShootBalls;
//        public PathChain FirstRow_LeaveLaunchZone;
//
//        // Second Row (Tag 22 - PGP)
//        public PathChain SecondRow_PickUp;
//        public PathChain SecondRow_ShootBalls;
//        public PathChain SecondRow_Leave;
//
//        // Third Row (Tag 23 - GPP)
//        public PathChain ThirdRow_PickUp;
//        public PathChain ThirdRow_ShootBalls;
//        public PathChain ThirdRow_LeaveLaunchZone;
//
//        public Paths(Follower follower) {
//
//            // ========== SHOOT PRELOAD (Red Alliance) ==========
//            ShootPreLoad = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(new Pose(80.988, 9.500), new Pose(81.000, 14.949))
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(90), Math.toRadians(66.5))
//                    .build();
//
//            // ========== FIRST ROW (Tag 21 - PPG) - RED ALLIANCE ==========
//            FirstRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(81.000, 14.949),
//                                    new Pose(77.393, 37.088),
//                                    new Pose(126.781, 35.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(66.5), Math.toRadians(0))
//                    .build();
//
//            FirstRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(126.781, 35.000),
//                                    new Pose(77.393, 37.088),
//                                    new Pose(81.000, 14.949)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(66.5))
//                    .build();
//
//            FirstRow_LeaveLaunchZone = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(81.000, 14.949),
//                                    new Pose(77.961, 40.305),
//                                    new Pose(40.305, 35.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(66.5), Math.toRadians(180))
//                    .build();
//
//            // ========== SECOND ROW (Tag 22 - PGP) - RED ALLIANCE ==========
//            SecondRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(81.000, 14.949),
//                                    new Pose(75.311, 64.526),
//                                    new Pose(125.645, 59.795)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(66.5), Math.toRadians(0))
//                    .build();
//
//            SecondRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(125.645, 59.795),
//                                    new Pose(74.744, 75.122)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(44.5))
//                    .build();
//
//            SecondRow_Leave = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(74.744, 75.122),
//                                    new Pose(41.251, 60.000)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(44.5), Math.toRadians(180))
//                    .build();
//
//            // ========== THIRD ROW (Tag 23 - GPP) - RED ALLIANCE ==========
//            ThirdRow_PickUp = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierCurve(
//                                    new Pose(81.000, 9.500),
//                                    new Pose(88.746, 91.206),
//                                    new Pose(65.093, 81.556),
//                                    new Pose(125.834, 84.016)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(66.5), Math.toRadians(0))
//                    .build();
//
//            ThirdRow_ShootBalls = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(125.834, 84.016),
//                                    new Pose(74.744, 75.122)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(44.5))
//                    .build();
//
//            ThirdRow_LeaveLaunchZone = follower
//                    .pathBuilder()
//                    .addPath(
//                            new BezierLine(
//                                    new Pose(74.744, 75.122),
//                                    new Pose(42.000, 83.070)
//                            )
//                    )
//                    .setLinearHeadingInterpolation(Math.toRadians(44.5), Math.toRadians(180))
//                    .build();
//        }
//    }
//
//    // Empty gamepad for autonomous (no input)
//    private static class EmptyGamepad extends com.qualcomm.robotcore.hardware.Gamepad {
//        // All methods return false/0 by default
//    }
//}
//
