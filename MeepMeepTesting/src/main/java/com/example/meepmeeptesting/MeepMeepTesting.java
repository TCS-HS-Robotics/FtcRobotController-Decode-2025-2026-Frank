package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import org.rowlandhall.meepmeep.MeepMeep;
import org.rowlandhall.meepmeep.roadrunner.DefaultBotBuilder;
import org.rowlandhall.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        Pose2d startPose = new Pose2d(-24, -63, Math.toRadians(90));

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Your robot constraints (adjust based on your Pedro tuning)
                .setConstraints(50, 40, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive -> drive.trajectorySequenceBuilder(startPose)
                        // Turn slightly right to line up with blue goal
                        .turn(Math.toRadians(-15))  // Turn 15° clockwise to aim at goal

                        // Drive forward toward the blue goal
                        .forward(30)  // Drive 30 inches forward

                        .build());

        // Load custom DECODE field image
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/Users/sam/StudioProjects/FtcRobotController2025-2026/MeepMeepTesting/src/main/java/com/example/meepmeeptesting/decode-field-image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        meepMeep.setBackground(img)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}