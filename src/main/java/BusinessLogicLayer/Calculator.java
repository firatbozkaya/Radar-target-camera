package BusinessLogicLayer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Calculator {

    public Calculator(){

    }

    public ImageIcon rotateImage(ImageIcon icon, double angle) {
        Image img = icon.getImage();
        BufferedImage bufferedImg =
                new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImg.createGraphics();
        AffineTransform tx = new AffineTransform();
        tx.rotate(Math.toRadians(-angle), img.getWidth(null) / 2, img.getHeight(null) / 2);
        g2d.setTransform(tx);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(bufferedImg);
    }

    public double getCalculateDistance(int x1, int y1, int x2, int y2){
        // Euclidean distance
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public double getAngleDeg(int x1, int y1, int x2, int y2) {
        double calculateX = x2 - x1;
        double calculateY = y2 - y1;

        double radianAngle = Math.atan2(calculateY, calculateX);
        double northUpAngle = Math.toDegrees(radianAngle);

        if (northUpAngle < 0) {
            northUpAngle += 360;
        }

        return Math.round(northUpAngle);
    }

    public double calculateAngleAndDistance(int x1, int y1, int x2, int y2, double angle, double distance) {
        double targetX = x1 + distance * Math.cos(Math.toRadians(angle));
        double targetY = y1 + distance * Math.sin(Math.toRadians(angle));

        double vectorX = targetX - x2;
        double vectorY = targetY - y2;

        double angleToTarget = Math.toDegrees(Math.atan2(vectorY, vectorX));

        if (angleToTarget < 0) {
            angleToTarget += 360;
        }

        return angleToTarget;
    }
}
