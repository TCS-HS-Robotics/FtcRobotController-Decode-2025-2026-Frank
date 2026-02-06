package org.firstinspires.ftc.teamcode.Frank.opmodes.Autonomous;

/**
 * Centralized timing and shooting constants for all autonomous routines.
 * Adjust these values to tune autonomous performance across all autos.
 */
public class AutoConstants {

    // ==================== SHOOTING TIMING ====================

    /** Time to feed one ball through the tube (ms) */
    public static final int TIME_TO_SHOOT = 900;

    /** Time to wait between shots for flywheel to recover (ms) */
    public static final int WAIT_BETWEEN_SHOTS = 750;

    /** Time to aim shooter at target before spinning up (ms) */
    public static final int AIM_TIME = 500;

    /** Time to run tube intake to position balls before shooting (ms) */
    public static final int POSITION_BALLS_TIME = 750;

    /** Extra time to run intake after reaching pickup position - Back autos (ms) */
    public static final int INTAKE_EXTRA_TIME = 500;

    /** Extra time to run intake after reaching pickup position - Front autos need longer (ms) */
    public static final int INTAKE_EXTRA_TIME_FRONT = 1000;

    // ==================== SPINUP TIMES ====================

    /** Spinup time for preload when shooter has long travel time (Front autos) (ms) */
    public static final int SPINUP_TIME_PRELOAD_SHORT = 500;

    /** Spinup time for preload when shooter has short travel time (Back autos) (ms) */
    public static final int SPINUP_TIME_PRELOAD_LONG = 2000;

    /** Spinup time after pickup - shooter ramps during return path (ms) */
    public static final int SPINUP_TIME_PICKUP = 750;

    // ==================== SHOOTING PARAMETERS ====================

    /** Standard basket height for Into The Deep (inches) */
    public static final double BASKET_HEIGHT = 53.0;

    /** Default shooting distance for Front autos (inches) */
    public static final double SHOOTING_DISTANCE_FRONT = 70.0;

    /** Default shooting distance for Back autos (inches) */
    public static final double SHOOTING_DISTANCE_BACK = 134.0;

    // Private constructor to prevent instantiation
    private AutoConstants() {}
}
