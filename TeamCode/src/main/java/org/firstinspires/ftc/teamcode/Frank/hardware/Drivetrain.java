package org.firstinspires.ftc.teamcode.Frank.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Frank.subsystems.LocalizationSystem;

public class Drivetrain extends Hardware {

    private DcMotor leftFront, rightFront, leftBack, rightBack;
    private double speed = 0.5;

    // - Localization -
    public LocalizationSystem localizer;
    private LocalizationSystem.Position currentPos = new LocalizationSystem.Position(0, 0);
    private LocalizationSystem.Position targetPos = null;

    public Drivetrain(HardwareMap hw, Telemetry telemetry, Gamepad gamepad) {

        super(telemetry, gamepad);

        this.leftFront = hw.get(DcMotor.class, "leftFront");
        this.rightFront = hw.get(DcMotor.class, "rightFront");
        this.leftBack = hw.get(DcMotor.class, "leftBack");
        this.rightBack = hw.get(DcMotor.class, "rightBack");

        this.leftFront.setDirection(DcMotor.Direction.REVERSE);
        this.leftBack.setDirection(DcMotor.Direction.REVERSE);

        this.localizer = new LocalizationSystem(hw, telemetry);
    }

    public void driveControl() {
        this.changeSpeedMode();

        // D-Pad overrides everything
        if (gamepad.dpad_up) {
            this.leftFront.setPower(speed);
            this.leftBack.setPower(speed);
            this.rightFront.setPower(speed);
            this.rightBack.setPower(speed);
        } else if (gamepad.dpad_down) {
            this.leftFront.setPower(-speed);
            this.leftBack.setPower(-speed);
            this.rightFront.setPower(-speed);
            this.rightBack.setPower(-speed);
        } else if (gamepad.dpad_left) {
            this._strafeLeft();
        } else if (gamepad.dpad_right) {
            this._strafeRight();
        }
        // Triggers override joysticks (but not D-pad)
        else if (gamepad.right_trigger > 0.5) {
            this._strafeRight();
        } else if (gamepad.left_trigger > 0.5) {
            this._strafeLeft();
        }
        // Joystick tank drive - BOTH sides handled independently with constant speed
        else {
            double leftPower = 0;
            double rightPower = 0;

            // Left joystick controls left side at constant speed
            if (-gamepad.left_stick_y > 0.5) {
                leftPower = speed;
            } else if (-gamepad.left_stick_y < -0.5) {
                leftPower = -speed;
            }

            // Right joystick controls right side at constant speed
            if (-gamepad.right_stick_y > 0.5) {
                rightPower = speed;
            } else if (-gamepad.right_stick_y < -0.5) {
                rightPower = -speed;
            }

            this.leftFront.setPower(leftPower);
            this.leftBack.setPower(leftPower);
            this.rightFront.setPower(rightPower);
            this.rightBack.setPower(rightPower);
        }
    }

    public void changeSpeedMode() {
        if (gamepad.right_bumper) { // Activate Fast Mode
            speed = 0.9;
        } else if (gamepad.left_bumper) { // Activate Slow Mode
            speed = 0.5;
        }
    }

    public void driveStraight(double speed) {
        this.leftFront.setPower(speed);
        this.leftBack.setPower(speed);
        this.rightFront.setPower(speed);
        this.rightBack.setPower(speed);
    }

    public void driveBackward(double speed) {
        this.leftFront.setPower(-speed);
        this.leftBack.setPower(-speed);
        this.rightFront.setPower(-speed);
        this.rightBack.setPower(-speed);
    }

    public void turnRight(double speed) {
        this.leftFront.setPower(speed);
        this.leftBack.setPower(speed);
        this.rightFront.setPower(-speed);
        this.rightBack.setPower(-speed);
    }

    public void turnLeft(double speed) {
        this.leftFront.setPower(-speed);
        this.leftBack.setPower(-speed);
        this.rightFront.setPower(speed);
        this.rightBack.setPower(speed);
    }

    public void stop() {
        this.leftFront.setPower(0);
        this.leftBack.setPower(0);
        this.rightFront.setPower(0);
        this.rightBack.setPower(0);
    }


    // --- Localization ---
    public boolean goTo(LocalizationSystem.Position pos, double speed) {
        // Initialize target if this is a new goTo call
        if (targetPos == null || (targetPos.x != pos.x || targetPos.y != pos.y)) {
            targetPos = pos;
        }

        currentPos = localizer.getCurrentPos();

        // Check if reached target position
        int xDir = LocalizationSystem.compareX(currentPos.x, targetPos.x);
        int yDir = LocalizationSystem.compareY(currentPos.y, targetPos.y);

        if (xDir == 0 && yDir == 0) {
            // Reached target position
            stop();
            targetPos = null;
            return true;
        }

        // Drive toward target position
        driveStraight(speed);
        return false;
    }


    // --- Helper methods ---

    public void _strafeRight() {
        this.leftFront.setPower(speed);
        this.leftBack.setPower(-speed);
        this.rightFront.setPower(-speed);
        this.rightBack.setPower(speed);
    }

    public void _strafeLeft() {
        this.leftFront.setPower(-speed);
        this.leftBack.setPower(speed);
        this.rightFront.setPower(speed);
        this.rightBack.setPower(-speed);
    }

    public void _strafeRight(double speed) {
        this.leftFront.setPower(speed);
        this.leftBack.setPower(-speed);
        this.rightFront.setPower(-speed);
        this.rightBack.setPower(speed);
    }

    public void _strafeLeft(double speed) {
        this.leftFront.setPower(-speed);
        this.leftBack.setPower(speed);
        this.rightFront.setPower(speed);
        this.rightBack.setPower(-speed);
    }




}