package org.firstinspires.ftc.teamcode.Stephon.util;

import androidx.annotation.Nullable;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Field-aware AprilTag model.
 * Automatically characterizes tags by ID (color/encoded/default).
 */
public class AprilTag {

    // --- TAG TYPES ---
    public enum TagType { DEFAULT, COLOR, ENCODED }

    // --- BASIC FIELDS ---
    public int id;
    public String family;
    public TagType type;

    // --- POSITION DATA ---
    public double x, y, z;
    public double yaw, pitch, roll;

    // --- EXTRA INFO ---
    public String color;
    public String code;

    // --- STATIC MAP OF KNOWN TAGS ---
    private static final Map<Integer, AprilTag> KNOWN_TAGS = new HashMap<>();

    static {
        KNOWN_TAGS.put(20, new AprilTag(20, "tag36h11", "blue"));
        KNOWN_TAGS.put(24, new AprilTag(24, "tag36h11", "red"));
        KNOWN_TAGS.put(21, new AprilTag(21, "tag36h11", new char[]{'G', 'P', 'P'}));
        KNOWN_TAGS.put(22, new AprilTag(22, "tag36h11", new char[]{'P', 'G', 'P'}));
        KNOWN_TAGS.put(23, new AprilTag(23, "tag36h11", new char[]{'P', 'P', 'G'}));
    }

    // --- CONSTRUCTORS ---
    public AprilTag(int id, String family) {
        this.id = id;
        this.family = family;
        this.type = TagType.DEFAULT;
    }

    public AprilTag(int id, String family, String color) {
        this.id = id;
        this.family = family;
        this.type = TagType.COLOR;
        this.color = color;
    }

    public AprilTag(int id, String family, char[] code) {
        this.id = id;
        this.family = family;
        this.type = TagType.ENCODED;
        this.code = new String(code);
    }

    // --- FACTORY METHODS ---

    /**
     * Create an AprilTag from a tag ID.
     * Returns a known tag if ID is in KNOWN_TAGS, otherwise creates a default tag.
     *
     * @param id The AprilTag ID
     * @return AprilTag object (known tag or default)
     */
    public static AprilTag fromId(int id) {
        // Try to fetch a known tag; if not found, make a default one
        return KNOWN_TAGS.containsKey(id)
                ? cloneBase(KNOWN_TAGS.get(id))
                : new AprilTag(id, "tag36h11");
    }

    /**
     * Create an AprilTag from an FTC SDK AprilTagDetection.
     * Preserves backward compatibility with webcam-based vision system.
     *
     * @param detection The AprilTagDetection from FTC SDK
     * @return AprilTag object with position data, or null if detection is null
     */
    public static AprilTag fromDetection(AprilTagDetection detection) {
        if (detection == null) return null;

        // Try to fetch a known tag; if not found, make a default one
        AprilTag tag = KNOWN_TAGS.containsKey(detection.id)
                ? cloneBase(KNOWN_TAGS.get(detection.id))
                : new AprilTag(detection.id, "unknown");

        // Update its positional data
        if (detection.ftcPose != null) {
            tag.x = detection.ftcPose.x;
            tag.y = detection.ftcPose.y;
            tag.z = detection.ftcPose.z;
            tag.yaw = detection.ftcPose.yaw;
            tag.pitch = detection.ftcPose.pitch;
            tag.roll = detection.ftcPose.roll;
        }

        return tag;
    }

    // --- INTERNAL CLONER (to avoid shared references) ---
    private static AprilTag cloneBase(AprilTag base) {
        AprilTag copy;
        switch (base.type) {
            case COLOR:
                copy = new AprilTag(base.id, base.family, base.color);
                break;
            case ENCODED:
                copy = new AprilTag(base.id, base.family, base.code.toCharArray());
                break;
            default:
                copy = new AprilTag(base.id, base.family);
        }
        return copy;
    }

    // --- DISTANCE HELPERS ---
    public static double getTagDistance(AprilTagDetection detection) {
        if (detection == null || detection.ftcPose == null) return -1;
        double x = detection.ftcPose.x;
        double z = detection.ftcPose.z;
        return Math.sqrt(x * x + z * z);
    }

    public static double getGoalDistance(AprilTagDetection detection) {
        if (detection == null || detection.ftcPose == null) return -1;
        double x = detection.ftcPose.x;
        double y = detection.ftcPose.y;
        double z = detection.ftcPose.z;
        double verticalOffset = 11.75 * 0.0254; // inches → meters
        return Math.sqrt(x * x + z * z + Math.pow(y + verticalOffset, 2));
    }

    // --- Identification Helpers ---
    public static boolean isColorTag(AprilTagDetection detection) {
        return KNOWN_TAGS.containsKey(detection.id) && KNOWN_TAGS.get(detection.id).type == TagType.COLOR;
    }

    public static boolean isEncoderTag(AprilTagDetection detection) {
        return KNOWN_TAGS.containsKey(detection.id) && KNOWN_TAGS.get(detection.id).type == TagType.ENCODED;
    }

    public static String getTagTypeFromDetection(AprilTagDetection detection) {
        if (isColorTag(detection)) return "Color";
        else if (isEncoderTag(detection)) return "Encoder";
        else return "Unknown";
    }

    // --- TELEMETRY PRINTING ---
    public void printTelemetry(Telemetry telemetry) {
        telemetry.addLine("=== AprilTag ===");
        telemetry.addData("ID", id);
        telemetry.addData("Type", type);
        if (color != null) telemetry.addData("Color", color);
        if (code != null) telemetry.addData("Code", code);
        telemetry.addData("Position", String.format("(%.2f, %.2f, %.2f)", x, y, z));
        telemetry.addData("Orientation", String.format("Yaw: %.1f  Pitch: %.1f  Roll: %.1f", yaw, pitch, roll));
    }

    // --- QUICK DISPLAY FOR FIRST DETECTED TAG ---
    public static void showFirstDetectedTag(AprilTagProcessor processor, Telemetry telemetry) {
        if (processor.getDetections().isEmpty()) {
            telemetry.addLine("No AprilTags detected.");
            return;
        }

        AprilTagDetection det = processor.getDetections().get(0);
        AprilTag tag = AprilTag.fromDetection(det);
        tag.printTelemetry(telemetry);
    }
}
