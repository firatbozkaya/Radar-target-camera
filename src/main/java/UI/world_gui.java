package UI;

import Entities.*;
import BusinessLogicLayer.radar_control;
import BusinessLogicLayer.camera_control;
import BusinessLogicLayer.Calculator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class world_gui extends JFrame implements ActionListener {
   // Variables
    private world_simulator worldSimulator;
    private Positions positions;
    private Config config;
    private radar_control radarControl;
    private camera_control cameraControl;
    private Calculator calculator;
    private boolean startButtonTrigger = true;
    private Thread appThread;
    // Variables

    //Components
    private Image plane;
    private ImageIcon cameraIcon;
    private ImageIcon radarIcon;
    private JLabel radarLabel;
    private JLabel cameraLabel;
    //Components

    public world_gui() {
        initParameters();
        initialComponents();
    }

    private void initParameters(){
        positions = Positions.getInstance();
        config = Config.getInstance();
        worldSimulator = new world_simulator();
        radarControl = new radar_control();
        cameraControl = new camera_control();
        calculator = new Calculator();

        appThread = new Thread(getAppThread);

        Timer my_timer = new Timer(100, this);
        my_timer.restart();

        plane = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/plane_icon.png"))).getImage();
        cameraIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/camera_icon.png")));
        radarIcon = new ImageIcon(
                Objects.requireNonNull(getClass().getResource("/icons/radar_icon.png")));

        radarLabel = new JLabel("Radar Label");
        cameraLabel = new JLabel("Camera Label");
    }

    void initialComponents(){
        setTitle("Radar Camera Task");
        setSize(600, 600);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphic_2D = (Graphics2D) g;

                // Set the color to light blue
                Color lightBlue = new Color(89, 154, 213);
                graphic_2D.setColor(lightBlue);

                // Set the stroke thickness
                graphic_2D.setStroke(new BasicStroke(2));

                //  x-axis
                graphic_2D.drawLine(80, 440, 500, 440);
                // x-axis left arrow
                graphic_2D.fillPolygon(new int[]{80, 75, 80}, new int[]{435, 440, 445}, 3);
                // x-axis right arrow
                graphic_2D.fillPolygon(new int[]{500, 505, 500}, new int[]{435, 440, 445}, 3);

                // y-axis
                graphic_2D.drawLine(140, 100, 140, 500);
                // y-axis up arrow
                graphic_2D.fillPolygon(new int[]{135, 140, 145}, new int[]{100, 95, 100}, 3);
                // y-axis down arrow
                graphic_2D.fillPolygon(new int[]{135, 140, 145}, new int[]{500, 505, 500}, 3);

                // Set color to black for labels
                graphic_2D.setColor(Color.BLACK);
                // label for x-axis
                graphic_2D.drawString("X", 510, 460);
                // label for y-axis
                graphic_2D.drawString("Y", 120, 100);

                graphic_2D.drawImage(plane, positions.getPlaneXPosition(), positions.getPlaneYPosition(), null);

                if(positions.getCameraLosStatusDegree() != 0 && positions.getCameraLosStatusDegree() != 180){
                    double angleInRadians = Math.toRadians(-positions.getCameraLosStatusDegree());
                    // Detection distance of the camera
                    int detectionDistance = 350;
                    int x1 = positions.getCameraXPosition();
                    int y1 = positions.getCameraYPosition();
                    int x2 = x1 + (int) (detectionDistance * Math.cos(angleInRadians));
                    int y2 = y1 + (int) (detectionDistance * Math.sin(angleInRadians));

                    // Draw two lines for the range from the camera to the target
                    graphic_2D.drawLine(x1 + plane.getWidth(null) / 2, y1 + plane.getWidth(null) / 2, x2 + (int)(plane.getWidth(null) / 1.75), y2 + plane.getHeight(null) / 2);
                    graphic_2D.drawLine(x1 + plane.getWidth(null) / 2, y1 + plane.getWidth(null) / 2, x2 + (int)(plane.getWidth(null) * 2.25), y2 + plane.getHeight(null) / 2);
                }
            }
        };
        // Creating GUI buttons
        JButton startButton = new JButton("Start");
        startButton.setBounds(175, 450, 100, 30);

        JButton stopButton = new JButton("Stop");
        stopButton.setBounds(285, 450, 100, 30);

        radarLabel.setBounds(positions.getRadarXPosition(), positions.getRadarYPosition(), 48, 48);
        radarLabel.setText("");
        radarLabel.setIcon(radarIcon);

        cameraLabel.setBounds(positions.getCameraXPosition(), positions.getRadarYPosition(), 48, 48);
        cameraLabel.setText("");
        cameraLabel.setIcon(cameraIcon);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(startButtonTrigger){
                    worldSimulator.startProducer();
                    radarControl.startProducer();
                    cameraControl.startProducer();

                    appThread.start();
                    startButtonTrigger=!startButtonTrigger;
                }
                else{
                    worldSimulator.resumeProducer();
                    radarControl.resumeProducer();
                    cameraControl.resumeProducer();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                worldSimulator.stopProducer();
                radarControl.stopProducer();
                cameraControl.stopProducer();

                startButton.setText("Continue");
            }
        });

        panel.setLayout(null);

        panel.add(startButton);
        panel.add(stopButton);
        panel.add(cameraLabel);
        panel.add(radarLabel);

        add(panel);
    }

    private Runnable getAppThread = new Runnable() {
        @Override
        public void run() {
            while (true) {
                worldSimulator.startListenConsumerCameraLosStatus();
                radarControl.startListenTargetPointPositionAndTowerPosition();
                cameraControl.startListenTargetBearingPosition();
            }
        }
    };

    @Override
    public void actionPerformed(ActionEvent arg0){
        cameraLabel.setIcon(calculator.rotateImage(cameraIcon, positions.getCameraAngle()));
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new world_gui().setVisible(true);
            }
        });
    }
}
