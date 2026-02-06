# 🤖 FTC Robot Context v1.0
**Team:** 8421  
**Season:** DECODE (2025–2026)  
**Robot Name:** Stephon

---

## 🧭 Overview
Stephon is an FTC competition robot designed to **intake and outtake game elements using the same mechanism**, with the goal of **scoring from any shooting area automatically**. The design emphasizes **versatility, aiming precision, and eventual autonomous scoring** using odometry and computer vision.

---

## ⚙️ Mechanical Systems

**Drivetrain:**
- 4-wheel *Mecanum drive*
- Powered by **four 435 RPM GoBilda Yellow Jacket motors**
- Fully capable of strafing and omnidirectional movement
- Uses a **GoBilda Pinpoint Odometry Computer** with **two swing-arm odometry pods** for precise localization

**Primary Mechanism (Intake/Outtake Combination):**
- **Two 6000 RPM GoBilda Yellow Jacket motors** drive opposing wheels spaced to grip 5-inch balls
- The wheels spin at the same speed to **shoot or launch balls** directly
- A **tilt bar** controlled by **two servos** adjusts the shooting angle for aiming
- Planned addition: a **rotating tube magazine** capable of holding three balls, with servo-controlled gates and color sensors for ball management and identification

**Other Mechanisms:**
- None currently implemented

**Sensors:**
- **Camera mounted on the right side** for AprilTag detection and positional alignment
- Planned addition: color sensors integrated in the tube loader

---

## ⚡️ Electronics & Control

**Control System:**
- REV Control Hub + REV Expansion Hub

**Motor/Servo Management:**
- All devices connected through REV hubs
- Current count:
    - 6 DC motors (4 drivetrain + 2 shooter/intake)
    - 2 servos (tilt control)
    - More servos to be added for the ball tube and gates

**Programming:**
- Language: **Java**
- Framework: **FTC SDK**
- Control structure:
    - Functional **TeleOp mode** implemented
    - No Autonomous routine yet, but planning integration
    - Not yet using PID or motion profiling, but aiming to integrate **Pinpoint-based localization and auto-aim** features in the near future

---

## 🧩 Performance & Goals

**Current Strengths:**
- Core mechanical systems functional
- Mecanum drive and odometry setup ready for precise autonomous development

**Current Limitations:**
- Autonomous and aiming systems not yet implemented
- Tube loader and color-sensing system not yet built

**Dimensions / Constraints:**
- TBD (within FTC’s standard 18×18×18 inch limits)

**Planned Upgrades:**
1. Add tube loader with servo gates and color sensors
2. Implement automatic aiming system
3. Create autonomous routines using the GoBilda Pinpoint odometry

---

## 🧱 Version History
- **v1.0 (Current):** Base Mecanum drive and shooter mechanism operational. Preparing for auto and loader integration.
