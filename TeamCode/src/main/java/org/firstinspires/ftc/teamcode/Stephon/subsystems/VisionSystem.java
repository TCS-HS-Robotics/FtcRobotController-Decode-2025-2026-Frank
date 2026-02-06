package org.firstinspires.ftc.teamcode.Stephon.subsystems;

import androidx.annotation.Nullable;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.Stephon.util.AprilTag;

import java.util.ArrayList;
import java.util.List;

/**
 * LIMELIGHT 3A SETUP INSTRUCTIONS:
 *
 * 1. Hardware: Connect Limelight 3A to Control Hub via USB-C
 *
 * 2. Robot Configuration:
 *    - In Driver Station: Configure → Limelight3A → name: "limelight"
 *
 * 3. Web Interface Access:
 *    - Connect to robot WiFi
 *    - Navigate to: http://172.29.0.31:5801
 *    - Configure pipeline 0 for AprilTag detection:
 *      * Family: AprilTag Classic 36h11 (apriltag3_36h11_classic)
 *      * Size: 165.1mm (DECODE presented by RTX 2025-2026 tags)
 *      * Enable MegaTag2 for robot localization
 *
 * 4. IMU Integration (for MegaTag2):
 *    - Call updateRobotOrientation(yawDegrees) in mainLoop with IMU heading
 *    - Example: vision.updateRobotOrientation(imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES))
 *
 * BENEFITS:
 *    - 90 FPS processing (vs ~30 FPS with webcam)
 *    - Hardware-accelerated AprilTag detection
 *    - Offloads Control Hub CPU for better overall performance
 *    - MegaTag2 multi-tag fusion for improved robot localization
 *    - Web-based pipeline configuration without code changes
 */
public class VisionSystem {

    // --- Known Tags in the Environment ---
    private final List<AprilTag> knownTags = new ArrayList<>();

    // --- Limelight Hardware ---
    private final Limelight3A limelight;

    // --- Cached Latest Result ---
    private LLResult latestResult;

    // --- Telemetry ---
    Telemetry telemetry;

    // --- Constructor ---
    public VisionSystem(HardwareMap hw, Telemetry telemetry) {
        this.telemetry = telemetry;

        // Initialize Limelight 3A
        limelight = hw.get(Limelight3A.class, "limelight");

        // Configure Limelight settings
        limelight.setPollRateHz(100); // Set to 100 Hz for responsive updates
        limelight.pipelineSwitch(0);   // Start with pipeline 0 (AprilTag detection)

        // Start the Limelight
        limelight.start();

        // Wait for Limelight to fully boot - LL3A needs 3-5 seconds after power-on
        // This prevents the "no data but LEDs flashing" issue
        try {
            Thread.sleep(1500); // Give Limelight time to fully initialize
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify connection by checking status
        LLStatus status = limelight.getStatus();
        if (status == null) {
            telemetry.addLine("WARNING: Limelight not responding - try power cycling robot");
            telemetry.update();
        }
    }

    // --- Periodic Update (called every loop) ---
    public void mainLoop(@Nullable Gamepad gamepad) {
        // Update cached result with latest data from Limelight
        latestResult = limelight.getLatestResult();

        // You can later use the gamepad to switch pipelines or modes
        // Example: if (gamepad != null && gamepad.dpad_up) setPipeline(1);
    }

    // --- Get all detections as custom AprilTag objects ---
    public ArrayList<AprilTag> getDetections() {
        ArrayList<AprilTag> tags = new ArrayList<>();

        if (latestResult == null || !latestResult.isValid()) {
            return tags;
        }

        // Get fiducial (AprilTag) results from Limelight
        List<LLResultTypes.FiducialResult> fiducials = latestResult.getFiducialResults();
        if (fiducials == null) {
            return tags;
        }

        // Convert each Limelight fiducial result to our custom AprilTag format
        for (LLResultTypes.FiducialResult fiducial : fiducials) {
            AprilTag tag = convertFiducialToAprilTag(fiducial);
            if (tag != null) {
                tags.add(tag);
            }
        }

        return tags;
    }

    // --- Get the closest tag by Euclidean distance ---
    public @Nullable AprilTag getClosestTag() {
        ArrayList<AprilTag> detections = getDetections();
        if (detections.isEmpty()) return null;

        AprilTag closest = detections.get(0);
        double minDist = getTagDistance(closest);

        for (AprilTag tag : detections) {
            double dist = getTagDistance(tag);
            if (dist < minDist) {
                closest = tag;
                minDist = dist;
            }
        }
        return closest;
    }

    // --- Get the first target tag (color tag) ---
    public @Nullable AprilTag getFirstTargetTag() {
        ArrayList<AprilTag> detections = getDetections();
        if (detections.isEmpty()) return null;

        for (AprilTag tag : detections) {
            if (tag.type.equals(AprilTag.TagType.COLOR)) {
                return tag;
            }
        }
        return null;
    }

    // --- Get the data from closest tag ---
    public AprilTag getTagFromDetection() {
        return getClosestTag();
    }

    // --- Lookup known tag by ID ---
    public @Nullable AprilTag getTagById(int id) {
        for (AprilTag tag : knownTags) {
            if (tag.id == id) return tag;
        }
        return null;
    }

    // --- Display all tag info to telemetry ---
    public void telemetryTags() {
        ArrayList<AprilTag> detections = getDetections();

        if (detections.isEmpty()) {
            // Optionally show "No tags detected" message
            return;
        }

        telemetry.addLine();
        telemetry.addLine("---" + " Detected Tag Info " + "---");

        for (AprilTag tag : detections) {
            telemetry.addLine("Tag: " + tag.id);
            telemetry.addLine("   Type: " + tag.type);
            if (tag.type == AprilTag.TagType.COLOR) {
                telemetry.addLine("   Color: " + tag.color);
            } else if (tag.type == AprilTag.TagType.ENCODED) {
                telemetry.addLine("   Code: " + tag.code);
            }
            telemetry.addLine("   Position: (" + tag.x + ", " + tag.y + ", " + tag.z + ")");
            telemetry.addLine();
        }

        telemetry.addLine();
        telemetry.addLine();
        telemetry.addLine();
    }

    // --- Check if an alliance tag is found ---
    public boolean allianceTagIsFound(String allianceColor) {
        ArrayList<AprilTag> detections = getDetections();
        for (AprilTag tag : detections) {
            if (tag.type.equals(AprilTag.TagType.COLOR) && tag.color.equals(allianceColor)) {
                return true;
            }
        }
        return false;
    }

    // --- Get the alliance color tag (ignoring code tags) ---
    public @Nullable AprilTag getAllianceColorTag(String allianceColor) {
        ArrayList<AprilTag> detections = getDetections();
        for (AprilTag tag : detections) {
            if (tag.type.equals(AprilTag.TagType.COLOR) && tag.color.equals(allianceColor)) {
                return tag;
            }
        }
        return null;
    }

    // ========== NEW LIMELIGHT-SPECIFIC METHODS ==========

    /**
     * Get robot's field position using MegaTag2 multi-tag fusion.
     * Requires MegaTag2 to be enabled in pipeline settings and IMU data via updateRobotOrientation().
     *
     * @return Pose3D with robot's field position (x, y, z, orientation) or null if unavailable
     */
    public @Nullable Pose3D getRobotPose() {
        if (latestResult == null || !latestResult.isValid()) {
            return null;
        }

        // Get MegaTag2 robot pose (uses multiple tags + IMU fusion for accuracy)
        return latestResult.getBotpose_MT2();
    }

    /**
     * Get horizontal offset to target in degrees.
     * Useful for alignment - positive means target is to the right.
     *
     * @return Horizontal offset in degrees, or 0.0 if no target
     */
    public double getTargetXOffset() {
        if (latestResult == null || !latestResult.isValid()) {
            return 0.0;
        }

        return latestResult.getTx(); // Horizontal offset in degrees
    }

    /**
     * Switch to a different vision pipeline.
     * Configure pipelines in Limelight web interface (http://172.29.0.1:5801)
     *
     * @param index Pipeline index (0-9)
     */
    public void setPipeline(int index) {
        if (index >= 0 && index <= 9) {
            limelight.pipelineSwitch(index);
        }
    }

    /**
     * Update robot's orientation for MegaTag2 fusion.
     * Call this in mainLoop() with IMU heading for improved accuracy.
     *
     * @param yawDegrees Robot's heading in degrees from IMU
     */
    public void updateRobotOrientation(double yawDegrees) {
        limelight.updateRobotOrientation(yawDegrees);
    }

    /**
     * Get Limelight status information.
     *
     * @return LLStatus object with pipeline, temperature, and capture info
     */
    public @Nullable LLStatus getStatus() {
        return limelight.getStatus();
    }

    /**
     * Check if Limelight is connected and responding.
     * Use this to detect the "LEDs flashing but no data" issue.
     *
     * @return true if Limelight is responding, false if connection lost
     */
    public boolean isConnected() {
        LLStatus status = limelight.getStatus();
        return status != null;
    }

    /**
     * Get detailed diagnostic string for troubleshooting.
     * Shows exactly where the data pipeline is failing.
     */
    public String getDiagnostics() {
        StringBuilder diag = new StringBuilder();

        // Check 1: Status
        LLStatus status = limelight.getStatus();
        if (status == null) {
            return "FAIL: No status (USB disconnected?)";
        }
        diag.append("Status: OK | ");

        // Check 2: Latest result
        LLResult result = limelight.getLatestResult();
        if (result == null) {
            return diag + "FAIL: No result data";
        }
        diag.append("Result: OK | ");

        // Check 3: Result validity
        if (!result.isValid()) {
            return diag + "FAIL: Result not valid (pipeline issue?)";
        }
        diag.append("Valid: OK | ");

        // Check 4: Fiducial results
        List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
        if (fiducials == null) {
            return diag + "FAIL: Fiducials null";
        }
        diag.append("Tags seen: ").append(fiducials.size());

        return diag.toString();
    }

    /**
     * Restart Limelight connection. Call this if isConnected() returns false.
     * This can recover from USB enumeration failures without power cycling.
     */
    public void restartConnection() {
        telemetry.addLine("Restarting Limelight connection...");
        telemetry.update();

        // Stop and restart the Limelight
        limelight.stop();

        try {
            Thread.sleep(1000); // Wait for clean shutdown
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        limelight.start();

        try {
            Thread.sleep(3000); // Wait for reboot
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Re-apply settings
        limelight.setPollRateHz(100);

        // Force pipeline reset by switching away and back
        limelight.pipelineSwitch(1);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        limelight.pipelineSwitch(0);

        try {
            Thread.sleep(1000); // Wait for pipeline to initialize
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if restart worked
        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            telemetry.addLine("Limelight FIXED - pipeline working!");
        } else if (isConnected()) {
            telemetry.addLine("Still invalid - try power cycle or check web interface");
        } else {
            telemetry.addLine("Limelight not responding - power cycle robot");
        }
        telemetry.update();
    }

    // ========== INTERNAL HELPER METHODS ==========

    /**
     * Convert Limelight FiducialResult to custom AprilTag object.
     * Uses robotPoseTargetSpace for relative positioning.
     */
    private @Nullable AprilTag convertFiducialToAprilTag(LLResultTypes.FiducialResult fiducial) {
        if (fiducial == null) {
            return null;
        }

        int tagId = (int) fiducial.getFiducialId();

        // Create AprilTag from known tags or default
        AprilTag tag = AprilTag.fromId(tagId);

        // Get tag position relative to camera (what we need for aiming!)
        Pose3D targetPose = fiducial.getTargetPoseCameraSpace();
        if (targetPose != null) {
            // Map Limelight camera space to shooter coordinate system
            // Limelight: X=horizontal, Y=vertical, Z=forward distance (meters)
            // Shooter expects: x=horizontal, y=DISTANCE, z=vertical (inches)
            tag.x = targetPose.getPosition().x * 39.3701;      // horizontal (meters to inches)
            tag.y = targetPose.getPosition().z * 39.3701;      // DISTANCE (Z in camera space!)
            tag.z = targetPose.getPosition().y * 39.3701;      // vertical (meters to inches)

            // Update orientation data
            tag.yaw = targetPose.getOrientation().getYaw();
            tag.pitch = targetPose.getOrientation().getPitch();
            tag.roll = targetPose.getOrientation().getRoll();
        }

        return tag;
    }

    /**
     * Calculate Euclidean distance to a tag.
     */
    private double getTagDistance(AprilTag tag) {
        if (tag == null) return -1;
        double x = tag.x;
        double z = tag.z;
        return Math.sqrt(x * x + z * z);
    }
}
