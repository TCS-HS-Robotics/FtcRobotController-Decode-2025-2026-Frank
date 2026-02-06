package org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Frank.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.Frank.subsystems.LocalizationSystem;

@Autonomous(name = "Localization Testing", group = "Frank")
public class LocalizationTesting extends LinearOpMode {

    Drivetrain drivetrain;

    LocalizationSystem.Position targetPos = new LocalizationSystem.Position(0, 15);

    @Override
    public void runOpMode() {

        drivetrain = new Drivetrain(hardwareMap, telemetry, gamepad1);
        telemetry.addLine("Drivetrain initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            drivetrain.localizer.update();

            boolean reached = drivetrain.goTo(targetPos, 0.2);

            telemetry.addData("Current Pos", drivetrain.localizer.getCurrentPos());
            telemetry.addData("Target Pos", targetPos);
            telemetry.addData("Reached Target", reached);
            telemetry.update();

            if (reached) {
                break;
            }
        }

    }

}
