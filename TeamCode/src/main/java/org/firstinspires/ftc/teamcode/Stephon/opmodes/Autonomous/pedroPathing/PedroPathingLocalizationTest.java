package org.firstinspires.ftc.teamcode.Stephon.opmodes.Autonomous.pedroPathing;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Pedro Pathing Localization Test", group="Testing")
public class PedroPathingLocalizationTest extends LinearOpMode {

    @Override
    public void runOpMode() {
        // Initialize follower using your Constants
        Follower follower = Constants.createFollower(hardwareMap);

        // Set starting pose at origin
        follower.setStartingPose(new Pose(0, 0, 0));

        telemetry.addLine("Pedro Pathing Localization Test");
        telemetry.addLine("Push robot around and watch position update");
        telemetry.addLine();
        telemetry.addLine("COORDINATE SYSTEM:");
        telemetry.addLine("X+ = Forward");
        telemetry.addLine("Y+ = Left");
        telemetry.addLine("Heading+ = Counter-clockwise");
        telemetry.addLine();
        telemetry.addLine("Press START");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Update localization
            follower.update();

            // Get current pose
            Pose currentPose = follower.getPose();

            telemetry.addLine("=== PEDRO PATHING POSITION ===");
            telemetry.addLine();

            // Access pose coordinates using getter methods
            telemetry.addData("X Position", "%.2f inches", currentPose.getX());
            telemetry.addData("Y Position", "%.2f inches", currentPose.getY());
            telemetry.addData("Heading", "%.2f degrees", Math.toDegrees(currentPose.getHeading()));
            telemetry.addLine();
            telemetry.addLine("TEST:");
            telemetry.addLine("1. Push forward 24\" → X should increase");
            telemetry.addLine("2. Push left 24\" → Y should increase");
            telemetry.addLine("3. Rotate CCW 90° → Heading should increase");

            telemetry.update();
        }
    }
}