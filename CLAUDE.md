# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an FTC (FIRST Tech Challenge) Robot Controller codebase for the 2025-2026 season. The project uses the FTC SDK v11.0.0 and includes custom team code in the `TeamCode` module.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install debug APK to connected device
./gradlew installDebug
```

## Project Structure

- **FtcRobotController/**: Official FTC SDK library module (rarely modified)
- **TeamCode/**: Custom team robot code (primary development area)
  - **Stephon/**: Main robot architecture (production framework)
  - **SuckySucky/**: Alternative simpler implementation for testing
  - **GoBildaPinpoint/**: goBILDA Pinpoint Odometry driver
  - **UtilClasses/**: Shared utilities

## Key Architecture: Stephon Framework

The primary robot architecture follows a **component-based, object-oriented pattern**:

### Central Robot Controller
- **Robot.java**: Central controller that aggregates all subsystems
- Each subsystem implements `HardwareInterface` and extends `Hardware`
- Unified `mainLoop()` coordinates all subsystem updates
- Gamepad assignments: gamepad1 → drivetrain, gamepad2 → shooter

### Hardware Components
Located in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Stephon/hardware/`:

- **Drivetrain.java**: 4-wheel mecanum drive (435 RPM motors)
  - Tank drive via joysticks, strafing via triggers
  - Variable speed modes, D-pad override controls

- **Shooter.java**: Dual 6000 RPM motors with servo-controlled tilt
  - **Physics-based ballistics calculation** for optimal shooting angles
  - Quadratic servo calibration system for dual-servo alignment
  - Preset positions and manual adjustment modes

- **Tube.java**: Ball storage system (in development)

### Subsystems
Located in `TeamCode/src/main/java/org/firstinspires/ftc/teamcode/Stephon/subsystems/`:

- **VisionSystem.java**: AprilTag detection using FTC Vision SDK
  - Webcam integration (640×480 resolution)
  - Closest tag detection and filtering
  - Integration with shooter for auto-aiming

### OpModes

**TeleOp Pattern** (extends `LinearOpMode`):
```java
@TeleOp(name="OpModeName", group="GroupName")
public class MyTeleOp extends LinearOpMode {
    private Robot robot;

    @Override
    public void runOpMode() {
        robot = new Robot(hardwareMap, telemetry);
        waitForStart();
        while (opModeIsActive()) {
            robot.mainLoop(gamepad1, gamepad2);
        }
    }
}
```

**Autonomous Pattern** (extends `OpMode`):
```java
@Autonomous(name="Auto Name", group="GroupName")
public class MyAuto extends OpMode {
    private Follower follower;
    private int pathState;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        // Build paths
    }

    @Override
    public void loop() {
        follower.update();
        // State machine logic
    }
}
```

## Pedro Pathing (Autonomous Framework)

The codebase uses **Pedro Pathing v2.0.3** for autonomous navigation:

- **Constants.java**: Configure follower, drive constants, path constraints
- **Follower**: Path-following controller with mecanum drivetrain support
- **Pinpoint Localizer**: Odometry integration with goBILDA Pinpoint Computer
- **Path Building**: BezierLine, BezierCurve, and PathChain support

Example autonomous structure:
1. Use `pathBuilder()` to create paths with Bezier curves/lines
2. Implement state machine with `pathState` variable
3. Check `follower.isBusy()` for path completion
4. Use `follower.followPath()` or `follower.holdPoint()` for actions

## GoBilda Pinpoint Odometry

- **Driver**: `GoBildaPinpointDriver.java` (custom I2C device)
- Provides sensor fusion between 2-wheel odometry and IMU
- Returns `Pose2D` (x, y, heading) and velocity data
- Configure with `setOffsets()`, `setEncoderResolution()`, `setEncoderDirections()`
- Supports goBILDA Swingarm Pod and 4-Bar Pod types

## Hardware Mapping Conventions

```java
// Motors
leftFront = hardwareMap.get(DcMotor.class, "leftfront");
shootLeft = hardwareMap.get(DcMotor.class, "shootleft");

// Servos
leftTilt = hardwareMap.get(Servo.class, "lefttilt");

// Sensors
pinpoint = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

// Camera
camera = hardwareMap.get(WebcamName.class, "Webcam 1");
```

## Adding New Hardware Components

1. Create a new class extending `Hardware` in `Stephon/hardware/`
2. Implement `mainLoop(Gamepad gamepad)` method
3. Add hardware initialization in constructor
4. Add instance to `Robot.java` and initialize in Robot constructor
5. Call component's `mainLoop()` in Robot's `mainLoop()`

## Key Dependencies

- **FTC SDK**: 11.0.0
- **Pedro Pathing**: 2.0.3 (autonomous navigation)
- **FTC Dashboard**: 0.4.15 (web-based telemetry)
- **Panels**: 1.0.7 (enhanced telemetry organization)
- **Apache Commons Math**: 3.6.1 (mathematical utilities)

## Development Environment

- **Android Studio**: Ladybug 2024.2 or later
- **Android Gradle Plugin**: 8.7.0
- **compileSdk**: 34 (Android 14)
- **minSdkVersion**: 24 (Android 7.0)
- **Java Version**: Java 8
- **NDK Version**: 21.3.6528147

## Special Features

### Servo Calibration System
- Uses quadratic polynomial regression for dual-servo alignment
- Coefficients stored in `CalibrationCoeffs.txt`
- Utilities in `ServoHelp/GetServoCalibration.java`

### Ballistics Calculation
- Physics-based projectile motion calculation in `Shooter.java`
- Calculates optimal angle and motor power based on distance and target height
- Accounts for transfer efficiency between wheel and ball
- Iterates through angles to find minimum required velocity

### Vision-Based Auto-Aiming
- AprilTag detection integrated with shooter mechanism
- Automatic distance calculation and angle adjustment
- Real-time telemetry output for debugging

## Git Workflow

- **Main Branch**: `master` (use for pull requests)
- **Current Development Branch**: `sam-code`
- Robot code is actively developed with servo implementation, driving improvements, and odometry integration

## Alternative Implementation: SuckySucky

A simpler, monolithic implementation in `SuckySucky/SuckySuckyTeleOp.java`:
- All logic in single file (no abstraction layers)
- Direct hardware access
- Use for quick testing and prototyping
- Same hardware as Stephon configuration

## Robot Configuration

**Robot Name**: "Stephon" (Team 8421, DECODE 2025-2026 Season)

**Current Status**:
- TeleOp fully functional with mecanum drive
- Shooter mechanism operational with auto-aiming
- Vision system integrated
- Autonomous routines in development
- Tube loader system planned

## Important Notes

- Always test on actual hardware before competitions
- Use FTC Dashboard (http://192.168.43.1:8080/dash) for real-time configuration
- Servo calibration must be performed before precise shooting
- OpModes must be annotated with `@TeleOp` or `@Autonomous` to appear in Driver Station
- Hardware device names in code must match Robot Controller configuration
