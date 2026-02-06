package org.firstinspires.ftc.teamcode.Stephon.hardware;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Hardware {

    Telemetry telemetry;
    Gamepad gamepad;

    public Hardware(Telemetry telemetry, Gamepad gamepad) {
        this.telemetry = telemetry;
        this.gamepad = gamepad;
    }



}