package org.firstinspires.ftc.teamcode.Frank.hardware;

import androidx.annotation.Nullable;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;


import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Tube extends Hardware {

//    private CRServo bottomLeft, topLeft, topRight, bottomRight;
//    private CRServo tubeLeft, tubeRight;

    private DcMotor wheel;

    private Servo stopper;
    private final double STOPPER_OPEN_POS = 100;
    private final double STOPPER_CLOSE_POS = 0;

    private Servo frontPusher;
    private boolean frontPusherIsOpen = true;


    public Tube(HardwareMap hw, Telemetry telemetry, Gamepad gamepad) {

        super(telemetry, gamepad);

        this.wheel = hw.get(DcMotor.class, "tubeWheel");

//        this.tubeLeft = hw.get(CRServo.class, "tubeLeft");
//        this.tubeRight = hw.get(CRServo.class, "tubeRight");
//        this.frontPusher = hw.get(Servo.class, "frontPusher");

//        this.tubeRight.setDirection(DcMotorSimple.Direction.REVERSE);

//        this.bottomLeft = hw.get(CRServo.class, "bottomleft");
//        this.topLeft = hw.get(CRServo.class, "topleft");
//        this.bottomRight = hw.get(CRServo.class, "bottomright");
//        this.topRight = hw.get(CRServo.class, "topright");
        this.stopper = hw.get(Servo.class, "stopper");

        this.closeStopper();

//        this.bottomLeft.setDirection(CRServo.Direction.REVERSE);
//        this.topLeft.setDirection(CRServo.Direction.REVERSE);

        //this.bottomRight.setDirection(DcMotorSimple.Direction.REVERSE);

    }

    public void handleIntakeOuttake() {
        if (gamepad.right_bumper) {
            outTake();
        } else if (gamepad.left_bumper || gamepad.left_trigger > 0.5) {
            inTake();
        } else {
            stop();
        }
    }

    public void inTake() {
//        tubeLeft.setPower(1.0);
//        tubeRight.setPower(1.0);
//        bottomLeft.setPower(1.0);
//        topLeft.setPower(1.0);
//        bottomRight.setPower(1.0);
//        topRight.setPower(1.0);

        wheel.setPower(0.75);
    }

    public void outTake() {
//        tubeLeft.setPower(-1.0);
//        tubeRight.setPower(-1.0);
//        bottomLeft.setPower(-1.0);
//        topLeft.setPower(-1.0);
//        bottomRight.setPower(-1.0);
//        topRight.setPower(-1.0);
        wheel.setPower(-0.75);
    }

    public void stop() {
//        tubeLeft.setPower(0.0);
//        tubeRight.setPower(0.0);
//        bottomLeft.setPower(0.0);
//        topLeft.setPower(0.0);
//        bottomRight.setPower(0.0);
//        topRight.setPower(0.0);
        wheel.setPower(0.0);
    }

    public void handleStopper() {
        if (gamepad.b) {
            openStopper();
        } else if (gamepad.a || gamepad.y || gamepad.left_stick_y > 0.5 || gamepad.left_stick_y < -0.5 || gamepad.right_stick_y > 0.5) {
            closeStopper();
        }
    }

    public void openStopper() {
        stopper.setPosition(STOPPER_OPEN_POS);
    }

    public void closeStopper() {
        stopper.setPosition(STOPPER_CLOSE_POS);
    }

    public void handleIntakeAssist() {
        if (gamepad.x) {
            if (frontPusher.getPosition() > 0.2) {
                frontPusher.setPosition(0);
                //frontPusherIsOpen = false;
            } else {
                frontPusher.setPosition(10);
                //frontPusherIsOpen = true;
            }
        }
    }

    public double frontPusherCurrentPos() {
        return frontPusher.getPosition();
    }

}
