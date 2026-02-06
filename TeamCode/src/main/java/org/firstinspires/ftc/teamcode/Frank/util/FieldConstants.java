package org.firstinspires.ftc.teamcode.Frank.util;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

/**
 * Field Constants for DECODE presented by RTX 2025-2026 Season
 *
 * Stores fixed field coordinates for goals, baskets, and boundaries.
 *
 * ✅ EXACT COORDINATES from official DECODE field CAD files
 * AprilTag positions (IDs 20 and 24) extracted from field transform matrices.
 * Source: https://www.chiefdelphi.com/t/decode-field-map/506135
 *
 * COORDINATE SYSTEM (FTC Standard):
 * - Origin (0,0) at field center
 * - X-axis: positive = right (red alliance side), negative = left (blue alliance side)
 * - Y-axis: positive = forward (audience side), negative = back (driver station)
 * - Heading: 0° = facing audience (+Y), 90° = facing red (+X), -90° = facing blue (-X)
 *
 * NOTE: Both basket AprilTags have the same X coordinate (-58.35"), positioned on the
 * left/blue side of the field. They are differentiated by Y coordinate (±55.63").
 * This may indicate baskets are both on the blue alliance side but opposite ends,
 * or the coordinate system may differ from typical FTC layout. Verify on actual field.
 */
public class FieldConstants {

    // ===== FIELD DIMENSIONS (FTC Standard) =====
    public static final double FIELD_WIDTH = 141.0;   // inches (12 tiles × 23.75" ≈ 144")
    public static final double FIELD_HEIGHT = 141.0;  // inches

    // ===== BASKET POSITIONS (DECODE presented by RTX 2025-2026) =====
    // ✅ EXACT COORDINATES from official DECODE field CAD files
    // Source: FTC DECODE Field Map transform matrices (tags 20 and 24)

    // Blue Alliance Basket AprilTag (Tag ID 20)
    // Note: These coordinates are for the AprilTag center, NOT the basket rim
    public static final Position BLUE_BASKET_POSITION = new Position(
        DistanceUnit.INCH,
        -58.35,  // X: -1.482m converted to inches
        -55.63,  // Y: -1.413m converted to inches (driver station side)
        29.48,   // Z: 0.749m converted to inches (AprilTag center height)
        0        // Time (not used, required by constructor)
    );

    // Red Alliance Basket AprilTag (Tag ID 24)
    public static final Position RED_BASKET_POSITION = new Position(
        DistanceUnit.INCH,
        -58.35,  // X: -1.482m converted to inches
        55.63,   // Y: 1.413m converted to inches (audience side)
        29.48,   // Z: 0.749m converted to inches (AprilTag center height)
        0        // Time (not used)
    );

    // ===== BASKET HEIGHTS =====
    // High basket rim (where ball must enter)
    public static final double BASKET_RIM_HEIGHT = 53.0;  // inches (estimated - verify on field)

    // Basket AprilTag center height (EXACT from field CAD)
    public static final double BASKET_TAG_HEIGHT = 29.48;  // inches (0.749m from transform matrix)

    // ===== SHOOTING TARGET OFFSET =====
    // Aim point adjustment (may want to aim slightly behind basket for arc)
    // This mimics the +10" offset used in tag-based aiming
    public static final double BASKET_DEPTH_OFFSET = 10.0;  // inches behind tag

    // ===== WALL POSITIONS (for safety/boundary checks) =====
    public static final double WALL_LEFT = -70.5;    // -141/2
    public static final double WALL_RIGHT = 70.5;    // +141/2
    public static final double WALL_FAR = 70.5;      // Audience side (+Y)
    public static final double WALL_NEAR = -70.5;    // Driver station side (-Y)

    // ===== HELPER METHODS =====

    /**
     * Get basket position for alliance.
     *
     * @param alliance "blue" or "red" (case-insensitive)
     * @return Position of basket on field
     */
    public static Position getBasketPosition(String alliance) {
        return alliance.equalsIgnoreCase("blue")
            ? BLUE_BASKET_POSITION
            : RED_BASKET_POSITION;
    }

    /**
     * Calculate 2D distance from robot position to basket.
     * Uses horizontal distance only (ignores Z height).
     *
     * @param robotPose Robot's current field position from Limelight
     * @param alliance "blue" or "red"
     * @return Distance to basket in inches
     */
    public static double getDistanceToBasket(Pose3D robotPose, String alliance) {
        Position basket = getBasketPosition(alliance);

        double dx = basket.x - robotPose.getPosition().x;
        double dy = basket.y - robotPose.getPosition().y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate heading (in degrees) from robot to basket.
     * Returns angle in field coordinates for alignment.
     *
     * @param robotPose Robot's current field position
     * @param alliance "blue" or "red"
     * @return Heading in degrees (0° = audience, 90° = red side, -90° = blue side)
     */
    public static double getHeadingToBasket(Pose3D robotPose, String alliance) {
        Position basket = getBasketPosition(alliance);

        double dx = basket.x - robotPose.getPosition().x;
        double dy = basket.y - robotPose.getPosition().y;

        // atan2(dx, dy) gives heading where 0° = +Y (audience), 90° = +X (red)
        return Math.toDegrees(Math.atan2(dx, dy));
    }

    /**
     * Check if robot is within field boundaries.
     * Used to validate localization (reject out-of-bounds readings).
     *
     * @param robotPose Robot's current field position
     * @return true if position is within field, false if out of bounds
     */
    public static boolean isInBounds(Pose3D robotPose) {
        double x = robotPose.getPosition().x;
        double y = robotPose.getPosition().y;

        return x > WALL_LEFT && x < WALL_RIGHT &&
               y > WALL_NEAR && y < WALL_FAR;
    }

    /**
     * Get a descriptive string of robot's field position (for telemetry).
     *
     * @param robotPose Robot's current field position
     * @return String like "Position: (12.3, 45.6) @ 90.0°"
     */
    public static String formatPosition(Pose3D robotPose) {
        return String.format("Position: (%.1f, %.1f) @ %.1f°",
            robotPose.getPosition().x,
            robotPose.getPosition().y,
            robotPose.getOrientation().getYaw());
    }
}