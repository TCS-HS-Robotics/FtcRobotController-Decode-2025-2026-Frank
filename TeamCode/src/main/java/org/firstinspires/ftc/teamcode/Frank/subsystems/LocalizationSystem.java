package org.firstinspires.ftc.teamcode.Frank.subsystems;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.GoBildaPinpoint.GoBildaPinpointDriver;

public class LocalizationSystem {

    GoBildaPinpointDriver pinpoint;
    private int initialEncoderXDiff = 0;
    private int initialEncoderYDiff = 0;

    // Constants
    private final double X_OFFSET = -5.875;
    private final double Y_OFFSET = -8;
    private final DistanceUnit DISTANCE_UNIT = DistanceUnit.INCH;
    private final AngleUnit ANGLE_UNIT = AngleUnit.DEGREES;
    public static final double BUFFER = 4;
    public static final double ANGLE_BUFFER = 0.4; // ~5.7 degrees in radians
    private static final double TRACK_WIDTH_INCHES = 15.0; // Distance between left and right encoders

    private Telemetry telemetry;
    private HardwareMap hw;

    public LocalizationSystem(HardwareMap hw, Telemetry telemetry) {
        this.hw = hw;
        this.telemetry = telemetry;

        pinpoint = hw.get(GoBildaPinpointDriver.class, "pinpoint");

        // Config
        pinpoint.setOffsets(X_OFFSET, Y_OFFSET, DISTANCE_UNIT);
        pinpoint.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        pinpoint.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.REVERSED, GoBildaPinpointDriver.EncoderDirection.FORWARD);
        pinpoint.recalibrateIMU();
        pinpoint.resetPosAndIMU();

        // Capture initial encoder values (in case reset doesn't work)
        // All future readings will be relative to these initial values
        initialEncoderXDiff = pinpoint.getEncoderX();
        initialEncoderYDiff = pinpoint.getEncoderY();
    }

    public Position getCurrentPos() {
        return new Position(pinpoint.getPosY(DISTANCE_UNIT), pinpoint.getPosX(DISTANCE_UNIT));
    }

    public double getHeading() {
        // Calculate heading from encoder tick difference
        // The difference between X and Y encoders indicates rotation
        int currentEncoderXDiff = pinpoint.getEncoderX() - initialEncoderXDiff;
        int currentEncoderYDiff = pinpoint.getEncoderY() - initialEncoderYDiff;

        // Calibrated from 360° rotation test: -8228 ticks = 360 degrees
        // Therefore: 22.86 ticks per degree
        double TICKS_PER_DEGREE = 22.86;

        // Calculate heading: negative because encoder difference decreases with clockwise rotation
        double headingDegrees = -(currentEncoderXDiff - currentEncoderYDiff) / TICKS_PER_DEGREE;

        return headingDegrees;
    }

    public double getEncoderX() {
        return pinpoint.getEncoderX();
    }

    public double getEncoderY() {
        return pinpoint.getEncoderY();
    }

    public void reset() {
        pinpoint.resetPosAndIMU();
        // Recapture baseline encoder values after reset
        initialEncoderXDiff = pinpoint.getEncoderX();
        initialEncoderYDiff = pinpoint.getEncoderY();
    }

    public void update() {
        pinpoint.update();
    }

    public static int compareX(double x1, double x2) {
        // Return 1 if pos1 is to the right of pos2, -1 if pos1 is to the left
        double xDiff = x1 - x2;
        if (xDiff < -BUFFER) {
            return -1;
        } else if (xDiff > BUFFER) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int compareY(double y1, double y2) {
        // Return 1 if pos1 is above pos2, -1 if pos1 is below
        double yDiff = y1 - y2;
        if (yDiff < -BUFFER) {
            return -1;
        } else if (yDiff > BUFFER) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int compareTheta(double theta1, double theta2) {
        // Return 1 if pos1 is to the right of pos2, -1 if pos1 is to the left
        double thetaDiff = theta1 - theta2;

        // Normalize angle difference to [-π, π] to account for angle wrapping
        while (thetaDiff > Math.PI) thetaDiff -= 2 * Math.PI;
        while (thetaDiff < -Math.PI) thetaDiff += 2 * Math.PI;

        if (thetaDiff < -ANGLE_BUFFER) {
            return -1;
        } else if (thetaDiff > ANGLE_BUFFER) {
            return 1;
        } else {
            return 0;
        }
    }

    public static double getAngleBetweenPos(Position pos1, Position pos2) {
        double xDiff = pos1.x - pos2.x;
        double yDiff = pos1.y - pos2.y;

        return Math.atan2(yDiff, xDiff);
    }

    // --- Sub Classes ---
    public static class Position {
        public double x, y;

        public Position(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

}
