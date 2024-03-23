package Entities;

public class Positions {
    private static Positions instance;
    private Positions() {}
    public static Positions getInstance(){
        if (instance == null) {
            instance = new Positions();
        }
        return instance;
    }
    private int planeXPosition = 150;
    private int planeYPosition = 200;
    private int radarXPosition = 175;
    private int radarYPosition = 385;
    private int cameraXPosition = 385;
    private int cameraYPosition = 385;
    private double cameraAngle = 0;
    private double cameraLosStatusDegree = 0;

    public void setCameraLosStatusDegree(double cameraLosStatusDegree) {
        this.cameraLosStatusDegree = cameraLosStatusDegree;
    }

    public double getCameraLosStatusDegree() {
        return cameraLosStatusDegree;
    }

    public void setCameraAngle(double cameraAngle) {
        this.cameraAngle = cameraAngle;
    }

    public double getCameraAngle() {
        return cameraAngle;
    }

    public void setPlaneXPosition(int planeXPosition) {
        this.planeXPosition = planeXPosition;
    }

    public void setPlaneYPosition(int planeYPosition) {
        this.planeYPosition = planeYPosition;
    }

    public void setRadarXPosition(int radarXPosition) {
        this.radarXPosition = radarXPosition;
    }

    public void setRadarYPosition(int radarYPosition) {
        this.radarYPosition = radarYPosition;
    }

    public void setCameraXPosition(int cameraXPosition) {
        this.cameraXPosition = cameraXPosition;
    }

    public void setCameraYPosition(int cameraYPosition) {
        this.cameraYPosition = cameraYPosition;
    }

    public int getPlaneXPosition() {
        return planeXPosition;
    }

    public int getPlaneYPosition() {
        return planeYPosition;
    }

    public int getRadarXPosition() {
        return radarXPosition;
    }

    public int getRadarYPosition() {
        return radarYPosition;
    }

    public int getCameraXPosition() {
        return cameraXPosition;
    }

    public int getCameraYPosition() {
        return cameraYPosition;
    }
}
