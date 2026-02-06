package org.firstinspires.ftc.teamcode.Frank.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.Frank.util.FieldConstants;

public class Shooter extends Hardware {

    // Constants
    private final double WHEEL_RADIUS = 1.875;
    private final double MAX_RPM = 6000;
    private final double NOMINAL_VOLTAGE = 13.88; // Voltage at which MAX_RPM is rated (fully charged battery)
    private final double LEFT_TILT_AT_90_DEG = 0.72;
    private final double SHOOTER_HEIGHT_OFF_GROUND = 2.5; // in inches
    public static double TRANSFER_EFFICIENCY_CLOSE = 0.71;
    public static double TRANSFER_EFFICIENCY_FAR = 0.72; // start 0.55, lower if ball is slower than expected

    private double DISTANCE_THRESHOLD = 80; // inches: above this uses FAR, below uses CLOSE
    private final double MAX_SHOOTER_POWER = 0.5;    // user cap
    private final double TICKS_PER_REV = 537.7;  // goBILDA Yellow Jacket motor encoder ticks

    // Hardware references
    private HardwareMap hardwareMap;

    // Shooting Config
    private DcMotor shootLeft, shootRight;
    private double outTakePower = 0.425;
    private double inTakePower = -0.4;  // Increased from -0.25

    // Encoder tracking for ready-state detection
    private double currentMotorPosition = 0;
    private int timeBetweenChecks = 10; // in milliseconds
    private ElapsedTime runtime = new ElapsedTime();

    // Servo Rotators
    private Servo leftTilt, rightTilt;

    // Presets and other Constants
    private final CalibrationCoeffs CAL_COEFFS = new CalibrationCoeffs(-0.0321039824, 0.7489592892, -0.0689888076);
    public final ServoPos MIN = new ServoPos(0.2, 0.11);
    public final ServoPos MAX = new ServoPos(0.598, 0.391);

    // Position handling
    private CurrentPos currentPos = new CurrentPos(MIN);

    // init
    public Shooter(HardwareMap hw, Telemetry telemetry, Gamepad gamepad) {

        super(telemetry, gamepad);

        this.hardwareMap = hw;  // Store for voltage sensing

        this.shootLeft = hw.get(DcMotor.class, "shootLeft");
        this.shootRight = hw.get(DcMotor.class, "shootRight");

        this.leftTilt = hw.get(Servo.class, "leftTilt");
        this.rightTilt = hw.get(Servo.class, "rightTilt");

        this.shootRight.setDirection(DcMotorSimple.Direction.REVERSE);
        this.rightTilt.setDirection(Servo.Direction.REVERSE);

        this.currentPos.goTo(MIN);

    }

    public void presets() {

        // Flat Preset
        if (gamepad.a) {
            this.currentPos.goTo(MIN);
        }

        // Position for Loading by Human Player
        if (gamepad.b) {
            this.currentPos.goTo(MAX);
        }

        // Small Triangle Tip Shot
        if (gamepad.dpad_down) {
            this.currentPos.goTo(new ServoPos(0.53, 0.35)); // Temp
            this.outTakePower = 0.3894; // Temp
        }

        // Big Triangle Tip Shot
        if (gamepad.dpad_up) {
            this.currentPos.goTo(new ServoPos(0.56, 0.37)); // Temp
            this.outTakePower = 0.3506; // Temp
        }

        // Big Triangle Mid Shot
        if (gamepad.dpad_left) {
            this.currentPos.goTo(new ServoPos(0.608, 0.398)); // Temp
            this.outTakePower = 0.3; // Temp
        }

        // Up Against Goal
        if (gamepad.dpad_right) {
            this.currentPos.goTo(MAX); // Temp
            this.outTakePower = 0.3; // Temp
        }

//        telemetry.addLine("Servo Pos: " + this.currentPos);
//        telemetry.addLine("OuttakePower: " + this.outTakePower);

    }

    public void angleControl(boolean telemetryActive) {

        this.presets();

        if (-gamepad.left_stick_y > 0.5) {
            this.currentPos.left += 0.002;
            this.currentPos.right = _calcRightTiltPos(this.currentPos.left, CAL_COEFFS);
        }
        if (-gamepad.left_stick_y < -0.5) {
            this.currentPos.left -= 0.002;
            this.currentPos.right = _calcRightTiltPos(this.currentPos.left, CAL_COEFFS);
        }

        this.currentPos = _clampPos(this.currentPos);

        this.moveServos();

        if (telemetryActive) {
            telemetry.addLine("Current Pos: " + this.currentPos);
        }

    }

    public void moveServos() {
        this.leftTilt.setPosition(this.currentPos.left);
        this.rightTilt.setPosition(this.currentPos.right);
    }

    public void shooterControl() {
        telemetry.addLine("Right Trigger: " + gamepad.right_trigger);
        telemetry.addLine("Left Trigger: " + gamepad.left_trigger);

        if (gamepad.right_trigger > 0.5) {  // Outtake
            this.shootLeft.setPower(this.outTakePower);
            this.shootRight.setPower(this.outTakePower);
        } else if (gamepad.left_trigger > 0.5) {  // Intake
            this.shootLeft.setPower(this.inTakePower);
            this.shootRight.setPower(this.inTakePower);
            telemetry.addLine("INTAKE ACTIVE - Power: " + this.inTakePower);
        } else {
            this.shootLeft.setPower(0.0);
            this.shootRight.setPower(0.0);
            telemetry.addLine("MOTORS STOPPED");
        }

        double leftPower = this.shootLeft.getPower();
        double rightPower = this.shootRight.getPower();
        telemetry.addLine("Left Power: " + leftPower + " Right Power: " + rightPower);
    }


    public void goToOptimalShootingAngleAndSpeed(double distance, double target_height) {
        ServoPos pos = _calcOptimalShootingAngleAndSpeed(distance, target_height);

        this.currentPos.goTo(pos);
        this.moveServos();
    }

    /**
     * Pre-calculate and set motor power for a given distance WITHOUT moving servos.
     * Use this to start ramping up the shooter while driving to the shooting position.
     *
     * @param distance Expected shooting distance in inches
     * @param target_height Height of target basket
     */
    public void rampUpForDistance(double distance, double target_height) {
        // Calculate optimal power (this also sets this.outTakePower internally)
        _calcOptimalShootingAngleAndSpeed(distance, target_height);
        // Start motors at calculated power
        outTake();
    }

    public void handleIntakeOuttake() {
        if (this.gamepad.right_trigger > 0.5) {
            this.outTake();
        } else if (this.gamepad.left_trigger > 0.5) {
            this.inTake();
        } else {
            this.stopShooter();
        }
    }

    public void inTake() {
        this.shootLeft.setPower(this.inTakePower);
        this.shootRight.setPower(this.inTakePower);
    }

    public void outTake() {
        this.shootLeft.setPower(this.outTakePower);
        this.shootRight.setPower(this.outTakePower);
    }

    public boolean readyToOutTake() {
        if (runtime.milliseconds() > timeBetweenChecks) {
            // Apply voltage compensation to expected motor speed
            double voltageCompensation = _getVoltageCompensationFactor();
            double effectiveMaxRPM = MAX_RPM * voltageCompensation;

            // Calculate expected motor speed based on outTakePower
            double expectedRPM = effectiveMaxRPM * outTakePower;
            double expectedTicksPerMs = (expectedRPM / 60.0) * (TICKS_PER_REV / 1000.0);
            double expectedChangeInTime = expectedTicksPerMs * timeBetweenChecks;

            // Check if actual change matches expected (with 80% threshold for tolerance)
            double actualChange = Math.abs(currentMotorPosition - this.shootLeft.getCurrentPosition());
            boolean isReady = actualChange > expectedChangeInTime * 0.8;

            currentMotorPosition = this.shootLeft.getCurrentPosition();
            runtime.reset();

            return isReady;
        }

        return false;
    }

    public void stopShooter() {
        this.shootLeft.setPower(0.0);
        this.shootRight.setPower(0.0);
    }

    public double getOutTakePower() {
        return this.outTakePower;
    }

    public void setOutTakePower(double power) {
        this.outTakePower = power;
    }

    public void setTiltAngle(double left, double right) {
        this.currentPos.goTo(left, right);
    }

    public void setTiltAngle(double left) {
        double right = _calcRightTiltPos(left, this.CAL_COEFFS);
        this.currentPos.goTo(left, right);
    }

    public void setTiltAngle(ServoPos pos) {
        this.currentPos.goTo(pos);
        this.moveServos();
    }

    public void setTransferEfficiency(double far, double close) {
        this.TRANSFER_EFFICIENCY_FAR = far;
        this.TRANSFER_EFFICIENCY_CLOSE = close;
    }

    public void setDistanceThreshold(double threshold) {
        this.DISTANCE_THRESHOLD = threshold;
    }

    public double getTransferEfficiencyFar() {
        return this.TRANSFER_EFFICIENCY_FAR;
    }

    public double getTransferEfficiencyClose() {
        return this.TRANSFER_EFFICIENCY_CLOSE;
    }

    public double getDistanceThreshold() {
        return this.DISTANCE_THRESHOLD;
    }


    // --- Helper Methods ---
    private double _calcRightTiltPos(double leftPos, CalibrationCoeffs coeffs) {
        return coeffs.a + coeffs.b * leftPos + coeffs.c * leftPos * leftPos;
    }

    private double _clampRight(double val) {
        return Math.max(this.MIN.right, Math.min(this.MAX.right, val));
    }

    private double _clampLeft(double val) {
        return Math.max(MIN.left, Math.min(MAX.left, val));
    }

    private ServoPos _clampPos(ServoPos pos) {
        return new ServoPos(_clampLeft(pos.left), _clampRight(pos.right));
    }

    private CurrentPos _clampPos(CurrentPos pos) {
        return new CurrentPos(_clampLeft(pos.left), _clampRight(pos.right));
    }

    private double _currentTiltPosToDeg() {
        return (currentPos.left - MIN.left) * (90 / (LEFT_TILT_AT_90_DEG - MIN.left));
    }

    private double _tiltPosToDeg(ServoPos pos) {
        return (pos.left - MIN.left) * (90 / (LEFT_TILT_AT_90_DEG - MIN.left));
    }

    private double _degToTiltPos(double degrees) {
        double rangeInDegrees = _tiltPosToDeg(MAX);
        return MIN.left + (degrees / rangeInDegrees) * (MAX.left - MIN.left);
    }

    /*
    The distance displayed from the AprilTag was different from the physical distance to it. This corrects for that
     */
    private double _correctDistance(double distance) {
        // Constants calculated by discrepancies found
        double a = 1.13;
        double b = 1.0;

        // Correction
        return a * distance - b;
    }

    /**
     * Gets the voltage compensation factor to adjust for battery drain
     * Motor RPM scales linearly with voltage, so we compensate by scaling MAX_RPM
     * @return Factor to multiply MAX_RPM by (e.g., 0.92 at 12V when nominal is 13V)
     */
    private double _getVoltageCompensationFactor() {
        try {
            // Try to get voltage from the Control Hub or Expansion Hub
            double currentVoltage = hardwareMap.voltageSensor.iterator().next().getVoltage();

            // Scale factor = current_voltage / nominal_voltage
            // If battery is at 12V and nominal is 13V, factor = 0.923
            return currentVoltage / NOMINAL_VOLTAGE;
        } catch (Exception e) {
            // If voltage sensor unavailable, assume nominal voltage (no compensation)
            return 1.0;
        }
    }

    public ServoPos _calcOptimalShootingAngleAndSpeed(double distance, double target_height) {
        double g = 386.09; // in/s²
        double delta_y = target_height - SHOOTER_HEIGHT_OFF_GROUND;
        double transfer_efficiency = distance > DISTANCE_THRESHOLD ? TRANSFER_EFFICIENCY_FAR : TRANSFER_EFFICIENCY_CLOSE;

        // Battery voltage compensation
        double voltageCompensation = _getVoltageCompensationFactor();
        double effectiveMaxRPM = MAX_RPM * voltageCompensation;

        // Speed constraints
        double maxPower = MAX_SHOOTER_POWER;
        double maxWheelRPM = effectiveMaxRPM * maxPower;
        double maxWheelSpeed = (maxWheelRPM / 60.0) * (2 * Math.PI * WHEEL_RADIUS);
        double maxProjectileSpeed = maxWheelSpeed * 0.65;

        // Angle limits
        double theta_min = Math.atan(delta_y / distance);
        double theta_max = Math.toRadians(_tiltPosToDeg(MAX));

        // ✅ START: Find angle that requires MINIMUM velocity
        double bestTheta = theta_min;
        double bestV = Double.MAX_VALUE;
        boolean foundValid = false;

        for (double theta = theta_min; theta <= theta_max; theta += Math.toRadians(0.5)) {
            double denom = distance * Math.tan(theta) - delta_y;
            if (denom <= 0) continue;

            double v = Math.sqrt((g * distance * distance) /
                    (2 * Math.pow(Math.cos(theta), 2) * denom));

            // ✅ CHANGED: Track the angle with LOWEST velocity required
            if (v < bestV) {  // Find minimum, not maximum
                bestTheta = theta;
                bestV = v;
                foundValid = true;
            }
        }

        // If minimum-velocity solution exceeds speed cap, fall back to minimum angle
        if (!foundValid || bestV > maxProjectileSpeed) {
            bestTheta = theta_min;
            double denom = distance * Math.tan(theta_min) - delta_y;
            bestV = Math.sqrt((g * distance * distance) /
                    (2 * Math.pow(Math.cos(theta_min), 2) * denom));
        }

        // ✅ FIXED: Account for efficiency when going backwards
        double requiredWheelSpeed = bestV / transfer_efficiency;  // Reverse the efficiency loss
        double wheel_rpm = (requiredWheelSpeed / (2 * Math.PI * WHEEL_RADIUS)) * 60;
        this.outTakePower = Math.min(maxPower, (wheel_rpm / effectiveMaxRPM));

        // Servo conversion (using your corrected method)
        double leftTiltPos = _degToTiltPos(Math.toDegrees(bestTheta));
        double rightTiltPos = _calcRightTiltPos(leftTiltPos, CAL_COEFFS);

        ServoPos bestServoPos = new ServoPos(leftTiltPos, rightTiltPos);

        // Telemetry
        telemetry.addLine();
        telemetry.addLine();
        telemetry.addLine("=== OPTIMAL SHOOTING CALC ===");
        telemetry.addData("   Battery Voltage", "%.2fV (%.1f%%)",
            voltageCompensation * NOMINAL_VOLTAGE, voltageCompensation * 100);
        telemetry.addData("   Distance", distance);
//        telemetry.addData("   Theta Min (deg)", Math.toDegrees(theta_min));
//        telemetry.addData("   Theta Max (deg)", Math.toDegrees(theta_max));
        telemetry.addData("   Best Theta (deg)", Math.toDegrees(bestTheta));
        telemetry.addData("   Optimal Servo Pos: ", bestServoPos);
//        telemetry.addData("   Required Exit Velocity (in/s)", bestV);
        telemetry.addData("   Required Wheel Speed (in/s)", requiredWheelSpeed);
        telemetry.addData("   Motor Power", outTakePower);
        telemetry.addLine();
        telemetry.addLine();
        telemetry.update();

        return bestServoPos;
    }

    // ========== EXPERIMENTAL: FIELD-CENTRIC AIMING METHODS ==========

    /**
     * FIELD-CENTRIC AUTO-AIM (EXPERIMENTAL)
     * Aim at basket using robot's field position from Limelight MegaTag2 localization.
     * This allows aiming from anywhere on the field, not just when goal tag is visible.
     *
     * @param robotPose Robot's position on field (from vision.getRobotPose())
     * @param alliance "blue" or "red"
     * @param basketHeight Height of basket rim (53" for Into The Deep)
     */
    public void aimAtBasketFromPose(Pose3D robotPose, String alliance, double basketHeight) {
        // Calculate distance from robot position to basket
        double distance = FieldConstants.getDistanceToBasket(robotPose, alliance);

        // Call existing ballistics method (unchanged!)
        // This reuses ALL the proven physics, servo calibration, voltage compensation
        goToOptimalShootingAngleAndSpeed(distance, basketHeight);

        // Telemetry
        telemetry.addLine();
        telemetry.addLine("=== FIELD-CENTRIC AIMING (EXPERIMENTAL) ===");
        telemetry.addData("Robot Position", "(%.1f, %.1f)",
            robotPose.getPosition().x, robotPose.getPosition().y);
        telemetry.addData("Alliance", alliance);
        telemetry.addData("Distance to Basket", "%.1f inches", distance);
        telemetry.addLine();
    }

    /**
     * TAG-RELATIVE AUTO-AIM (LEGACY FALLBACK)
     * Aim using AprilTag detection - original proven behavior.
     * This is a wrapper around existing logic for compatibility.
     *
     * @param tag AprilTag detection
     * @param basketHeight Height of basket rim
     */
    public void aimAtBasketFromTag(org.firstinspires.ftc.teamcode.Frank.util.AprilTag tag, double basketHeight) {
        // Original logic: aim at ridge behind tag
        double distance = tag.y + 10;  // +10" offset for ridge targeting

        // Call existing ballistics method
        goToOptimalShootingAngleAndSpeed(distance, basketHeight);

        // Telemetry
        telemetry.addLine();
        telemetry.addLine("=== TAG-BASED AIMING (PROVEN) ===");
        telemetry.addData("Tag ID", tag.id);
        telemetry.addData("Tag Distance", "%.1f inches", tag.y);
        telemetry.addLine();
    }

    /**
     * Get required heading to face basket (for field-centric alignment).
     *
     * @param robotPose Robot's current field position
     * @param alliance "blue" or "red"
     * @return Heading in degrees (field coordinates: 0° = audience, 90° = red side)
     */
    public double getRequiredHeadingToBasket(Pose3D robotPose, String alliance) {
        return FieldConstants.getHeadingToBasket(robotPose, alliance);
    }







    // --- Helper Classes ---
    public static class ServoPos {
        public double left, right;
        public ServoPos(double left, double right) {
            this.left = left;
            this.right = right;
        }

        public ServoPos(ServoPos pos) {
            this.left = pos.left;
            this.right = pos.right;
        }

        public void setPos(double left, double right) {
            this.left = left;
            this.right = right;
        }

        public void setPos(ServoPos pos) {
            this.left = pos.left;
            this.right = pos.right;
        }

        public String toString() {
            return "(" + this.left + ", " + this.right + ")";
        }

    }

    public class CurrentPos extends ServoPos {
        public CurrentPos(double left, double right) {
            super(left, right);
        }

        public CurrentPos(ServoPos pos) {
            super(pos);
        }

        public void goTo(ServoPos pos) {
            super.setPos(pos);
        }

        public void goTo(double left, double right) {
            super.setPos(left, right);
        }



    }

    public class CalibrationCoeffs {
        public double a, b, c;

        public CalibrationCoeffs(double a, double b, double c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

    }

}