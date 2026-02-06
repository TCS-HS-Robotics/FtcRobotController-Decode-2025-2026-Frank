package org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Frank.Robot;

@Autonomous(name = "SimpleDriveForwardAuto", group = "3")
public class DriveForwardAuto extends LinearOpMode {

    private Robot robot;
    private ElapsedTime runtime = new ElapsedTime();

    @Override
    public void runOpMode() {

        this.robot = new Robot(hardwareMap, telemetry, gamepad1, gamepad2);

        waitForStart();

        this.robot.drivetrain.driveStraight(0.2);
        runtime.reset();
        while (opModeIsActive() && (runtime.seconds() < 3)) {
            telemetry.addLine("Driving forward...");
            telemetry.update();
        }
        this.robot.drivetrain.stop();


    }

}
