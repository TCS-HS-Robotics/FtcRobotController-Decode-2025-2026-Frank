package org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous.PP_Autos;

import com.bylazar.field.FieldManager;
import com.bylazar.field.PanelsField;
import com.bylazar.field.Style;
import com.pedropathing.math.Vector;
import com.pedropathing.util.PoseHistory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

import org.firstinspires.ftc.teamcode.Frank.Robot;
import org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous.AutoConstants;
import org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous.pedroPathing.Constants;

@Autonomous(name = "🔵 6 Shot FRONT", group = "(1) PP Autonomous (Blue)")
public class SixShotFrontBlueAuto_PP extends OpMode {

    private Follower follower;
    private Robot robot;
    private ElapsedTime timer;

    // Constants
    private static final String ALLIANCE_COLOR = "blue";
    private static final double SHOOTING_ANGLE = 134;

    // Positions (from JSON path configuration)
    private final Pose START_POSE = new Pose(19, 119, Math.toRadians(143));
    private final Pose SCORING_POS = new Pose(55, 88);
    private final Pose INTAKE_CONTROL = new Pose(56, 82);
    private final Pose INTAKE_POS = new Pose(16, 83);
    private final Pose PARK_POS = new Pose(55, 120);

    // Pre-built paths
    private PathChain path1, path2, path3, path4;

    // Drawing
    private static final double ROBOT_RADIUS = 9;
    private FieldManager panelsField;
    private Style robotStyle, pathStyle, historyStyle, plannedPathStyle;

    // State machine
    private enum AutoState {
        // Path 1: To scoring position
        PATH_1,

        // Preload shooting sequence
        AIM_PRELOAD,
        SPINUP_PRELOAD,
        POSITION_BALLS_PRELOAD,
        SHOOT_PRELOAD_1,
        WAIT_PRELOAD_1,
        SHOOT_PRELOAD_2,
        WAIT_PRELOAD_2,
        SHOOT_PRELOAD_3,

        // Drive to intake position
        START_INTAKE,
        PATH_2,
        EXTRA_INTAKE,
        STOP_INTAKE,

        // Return to scoring position
        PATH_3,

        // Second shooting sequence
        AIM_1,
        SPINUP_1,
        POSITION_BALLS_1,
        SHOOT_1_BALL_1,
        WAIT_1_BALL_1,
        SHOOT_1_BALL_2,
        WAIT_1_BALL_2,
        SHOOT_1_BALL_3,

        // Park sequence (leave launch area)
        START_PARK,
        PATH_4,

        IDLE
    }

    private AutoState currentState = AutoState.PATH_1;

    @Override
    public void init() {
        // Initialize follower
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(START_POSE);

        // Initialize robot for mechanisms ONLY (skip drivetrain - Pedro Pathing controls motors)
        robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2, false);
        timer = new ElapsedTime();

        // Initialize Panels
        panelsField = PanelsField.INSTANCE.getField();
        panelsField.setOffsets(PanelsField.INSTANCE.getPresets().getPEDRO_PATHING());
        robotStyle = new Style("", "#4CAF50", 0.0);
        pathStyle = new Style("", "#2196F3", 0.0);
        historyStyle = new Style("", "#FF9800", 0.0);
        plannedPathStyle = new Style("", "#9E9E9E", 0.0);

        // Build all paths
        // Path 1: Start to scoring position
        path1 = follower.pathBuilder()
                .addPath(new BezierLine(START_POSE, SCORING_POS))
                .setLinearHeadingInterpolation(Math.toRadians(143), Math.toRadians(SHOOTING_ANGLE))
                .build();

        // Path 2: Scoring to intake position (with control point for curved path)
        path2 = follower.pathBuilder()
                .addPath(new BezierCurve(
                        SCORING_POS,
                        INTAKE_CONTROL,
                        INTAKE_POS))
                .setTangentHeadingInterpolation()
                .build();

        // Path 3: Intake back to scoring position
        path3 = follower.pathBuilder()
                .addPath(new BezierLine(INTAKE_POS, SCORING_POS))
                .setLinearHeadingInterpolation(Math.toRadians(180), Math.toRadians(SHOOTING_ANGLE))
                .build();

        // Path 4: Scoring to park (leave launch area)
        path4 = follower.pathBuilder()
                .addPath(new BezierLine(SCORING_POS, PARK_POS))
                .setLinearHeadingInterpolation(Math.toRadians(SHOOTING_ANGLE), Math.toRadians(270))
                .build();

        // Lower shooter to starting position
        robot.shooter.setTiltAngle(robot.shooter.MIN);

        telemetry.addLine("Ready - 6 Shot Front Blue (Non-Blocking)");
        telemetry.update();
    }

    @Override
    public void start() {
        timer.reset();
        robot.shooter.rampUpForDistance(AutoConstants.SHOOTING_DISTANCE_FRONT, AutoConstants.BASKET_HEIGHT);
        follower.followPath(path1);
        currentState = AutoState.PATH_1;
    }

    @Override
    public void loop() {
        // CRITICAL: Update follower EVERY loop iteration
        follower.update();

        // State machine
        switch (currentState) {

            // ==================== PATH 1: To scoring position ====================
            case PATH_1:
                if (!follower.isBusy()) {
                    timer.reset();
                    currentState = AutoState.AIM_PRELOAD;
                }
                break;

            // ==================== PRELOAD SHOOTING SEQUENCE ====================
            case AIM_PRELOAD:
                if (!robot.aimShooterOnly(AutoConstants.BASKET_HEIGHT, ALLIANCE_COLOR)) {
                    robot.aimShooterAtDistance(AutoConstants.SHOOTING_DISTANCE_FRONT, AutoConstants.BASKET_HEIGHT);
                }
                if (timer.milliseconds() >= AutoConstants.AIM_TIME) {
                    robot.shooter.outTake();
                    robot.tube.inTake();
                    timer.reset();
                    currentState = AutoState.POSITION_BALLS_PRELOAD;
                }
                break;

            case POSITION_BALLS_PRELOAD:
                if (timer.milliseconds() >= AutoConstants.POSITION_BALLS_TIME) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.SPINUP_PRELOAD;
                }
                break;

            case SPINUP_PRELOAD:
                if (timer.milliseconds() >= AutoConstants.SPINUP_TIME_PRELOAD_SHORT) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_PRELOAD_1;
                }
                break;

            case SHOOT_PRELOAD_1:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.WAIT_PRELOAD_1;
                }
                break;

            case WAIT_PRELOAD_1:
                if (timer.milliseconds() >= AutoConstants.WAIT_BETWEEN_SHOTS) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_PRELOAD_2;
                }
                break;

            case SHOOT_PRELOAD_2:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.WAIT_PRELOAD_2;
                }
                break;

            case WAIT_PRELOAD_2:
                if (timer.milliseconds() >= AutoConstants.WAIT_BETWEEN_SHOTS) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_PRELOAD_3;
                }
                break;

            case SHOOT_PRELOAD_3:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    robot.shooter.stopShooter();
                    robot.shooter.setTiltAngle(robot.shooter.MIN);
                    currentState = AutoState.START_INTAKE;
                }
                break;

            // ==================== INTAKE SEQUENCE ====================
            case START_INTAKE:
                robot.shooter.inTake();
                robot.tube.inTake();
                follower.setMaxPower(0.35);  // Slow down for intake
                follower.followPath(path2);
                currentState = AutoState.PATH_2;
                break;

            case PATH_2:
                if (!follower.isBusy()) {
                    timer.reset();
                    currentState = AutoState.EXTRA_INTAKE;
                }
                break;

            case EXTRA_INTAKE:
                // Keep both intakes running
                robot.shooter.inTake();
                robot.tube.inTake();
                if (timer.milliseconds() >= AutoConstants.INTAKE_EXTRA_TIME_FRONT) {
                    currentState = AutoState.STOP_INTAKE;
                }
                break;

            case STOP_INTAKE:
                robot.tube.stop();
                robot.shooter.rampUpForDistance(AutoConstants.SHOOTING_DISTANCE_FRONT, AutoConstants.BASKET_HEIGHT);
                follower.setMaxPower(1.0);  // Back to full speed
                follower.followPath(path3);
                currentState = AutoState.PATH_3;
                break;

            // ==================== RETURN TO SCORING ====================
            case PATH_3:
                if (!follower.isBusy()) {
                    timer.reset();
                    currentState = AutoState.AIM_1;
                }
                break;

            // ==================== SECOND SHOOTING SEQUENCE ====================
            case AIM_1:
                if (!robot.aimShooterOnly(AutoConstants.BASKET_HEIGHT, ALLIANCE_COLOR)) {
                    robot.aimShooterAtDistance(AutoConstants.SHOOTING_DISTANCE_FRONT, AutoConstants.BASKET_HEIGHT);
                }
                if (timer.milliseconds() >= AutoConstants.AIM_TIME) {
                    robot.shooter.outTake();
                    robot.tube.inTake();
                    timer.reset();
                    currentState = AutoState.POSITION_BALLS_1;
                }
                break;

            case POSITION_BALLS_1:
                if (timer.milliseconds() >= AutoConstants.POSITION_BALLS_TIME) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.SPINUP_1;
                }
                break;

            case SPINUP_1:
                if (timer.milliseconds() >= AutoConstants.SPINUP_TIME_PICKUP) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_1_BALL_1;
                }
                break;

            case SHOOT_1_BALL_1:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.WAIT_1_BALL_1;
                }
                break;

            case WAIT_1_BALL_1:
                if (timer.milliseconds() >= AutoConstants.WAIT_BETWEEN_SHOTS) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_1_BALL_2;
                }
                break;

            case SHOOT_1_BALL_2:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    timer.reset();
                    currentState = AutoState.WAIT_1_BALL_2;
                }
                break;

            case WAIT_1_BALL_2:
                if (timer.milliseconds() >= AutoConstants.WAIT_BETWEEN_SHOTS) {
                    robot.tube.outTake();
                    timer.reset();
                    currentState = AutoState.SHOOT_1_BALL_3;
                }
                break;

            case SHOOT_1_BALL_3:
                if (timer.milliseconds() >= AutoConstants.TIME_TO_SHOOT) {
                    robot.tube.stop();
                    robot.shooter.stopShooter();
                    robot.shooter.setTiltAngle(robot.shooter.MIN);
                    currentState = AutoState.START_PARK;
                }
                break;

            // ==================== PARK SEQUENCE (leave launch area) ====================
            case START_PARK:
                follower.setMaxPower(0.75);
                follower.followPath(path4);
                currentState = AutoState.PATH_4;
                break;

            case PATH_4:
                if (!follower.isBusy()) {
                    follower.setMaxPower(1.0);
                    currentState = AutoState.IDLE;
                }
                break;

            // ==================== DONE ====================
            case IDLE:
                robot.shooter.stopShooter();
                robot.tube.stop();
                break;
        }

        // Telemetry
        Pose currentPose = follower.getPose();
        telemetry.addData("State", currentState);
        telemetry.addData("Busy", follower.isBusy());
        telemetry.addData("X", "%.2f", currentPose.getX());
        telemetry.addData("Y", "%.2f", currentPose.getY());
        telemetry.addData("Heading", "%.1f\u00b0", Math.toDegrees(currentPose.getHeading()));
        telemetry.addData("Timer", "%.0f ms", timer.milliseconds());
        telemetry.update();

        // Update drawing
        updateDrawing();
    }

    // ==================== DRAWING ====================

    private void updateDrawing() {
        Pose currentPose = follower.getPose();

        drawPathChain(path1, plannedPathStyle);
        drawPathChain(path2, plannedPathStyle);
        drawPathChain(path3, plannedPathStyle);
        drawPathChain(path4, plannedPathStyle);

        if (follower.getCurrentPath() != null) {
            drawPath(follower.getCurrentPath(), pathStyle);
        }

        drawPoseHistory(follower.getPoseHistory());
        drawRobot(currentPose);
        panelsField.update();
    }

    private void drawRobot(Pose pose) {
        if (pose == null || Double.isNaN(pose.getX()) || Double.isNaN(pose.getY())) return;
        panelsField.setStyle(robotStyle);
        panelsField.moveCursor(pose.getX(), pose.getY());
        panelsField.circle(ROBOT_RADIUS);
        Vector v = pose.getHeadingAsUnitVector();
        v.setMagnitude(v.getMagnitude() * ROBOT_RADIUS);
        panelsField.moveCursor(pose.getX(), pose.getY());
        panelsField.line(pose.getX() + v.getXComponent(), pose.getY() + v.getYComponent());
    }

    private void drawPath(com.pedropathing.paths.Path path, Style style) {
        double[][] points = path.getPanelsDrawingPoints();
        panelsField.setStyle(style);
        panelsField.moveCursor(points[0][0], points[0][1]);
        panelsField.line(points[1][0], points[1][1]);
    }

    private void drawPathChain(PathChain pathChain, Style style) {
        for (int i = 0; i < pathChain.size(); i++) {
            drawPath(pathChain.getPath(i), style);
        }
    }

    private void drawPoseHistory(PoseHistory poseHistory) {
        panelsField.setStyle(historyStyle);
        double[] x = poseHistory.getXPositionsArray();
        double[] y = poseHistory.getYPositionsArray();
        for (int i = 0; i < x.length - 1; i++) {
            panelsField.moveCursor(x[i], y[i]);
            panelsField.line(x[i + 1], y[i + 1]);
        }
    }
}