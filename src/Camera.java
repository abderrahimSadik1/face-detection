import org.opencv.core.*;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import java.sql.Connection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Camera extends JFrame {
    private final JLabel cameraScreen;
    private final CascadeClassifier faceCascade;
    private final VideoCapture capture;
    private boolean isCameraRunning = false;
    private  Connection connection;

    public Camera() {
        faceCascade = new CascadeClassifier("C:/Program Files/opencv/build/etc/haarcascades/haarcascade_frontalface_alt.xml");

        capture = new VideoCapture(0);


        JPanel p = new JPanel();
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Options");
        JMenuItem startCameraItem = new JMenuItem("Start Camera");
        JMenuItem stopCameraItem = new JMenuItem("Stop Camera");
        JMenuItem captureFaceItem = new JMenuItem("Capture Face");
        JMenuItem changeCascadeItem = new JMenuItem("Change Cascade File");
        JMenuItem exitItem = new JMenuItem("Exit");

        startCameraItem.addActionListener(e -> startCamera());
        stopCameraItem.addActionListener(e -> stopCamera());
        captureFaceItem.addActionListener(e -> captureFace());

        changeCascadeItem.addActionListener(e -> changeCascadeFile());
        exitItem.addActionListener(e -> exitApplication());

        fileMenu.add(startCameraItem);
        fileMenu.add(stopCameraItem);
        fileMenu.addSeparator();
        fileMenu.add(captureFaceItem);
        fileMenu.addSeparator();
        fileMenu.add(changeCascadeItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Add components to the panel
        cameraScreen = new JLabel();
        cameraScreen.setBounds(0, 50, 640, 480);
        p.add(cameraScreen);

        // Set layout and add panel to the frame
        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);

        setSize(new Dimension(640, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApplication();
            }
        });


        setVisible(true);
    }

    private void startCamera() {
        if (!isCameraRunning) {
            isCameraRunning = true;
            new Thread(() -> {
                Mat frame = new Mat();
                while (isCameraRunning) {
                    capture.read(frame);
                    detectAndDrawFaces(frame);

                }
            }).start();
        }
    }

    private void stopCamera() {
        isCameraRunning = false;
    }

    private void detectAndDrawFaces(Mat frame) {
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frame, faces);
        for (Rect rect : faces.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 2);
        }

        updateCameraScreen(frame);
    }

    private void updateCameraScreen(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, buffer);
        ImageIcon image = new ImageIcon(buffer.toArray());
        cameraScreen.setIcon(image);
    }

    private void captureFace() {
        Mat frame = new Mat();
        capture.read(frame);
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frame, faces);

        if (!faces.empty()) {
            for (Rect rect : faces.toArray()) {
                Mat faceR = new Mat(frame, rect);
                String name = JOptionPane.showInputDialog("Enter the name:");
                saveFaceImage(faceR, name);
            }
        }
    }



    private void saveFaceImage(Mat face,String name) {
        connection = DatabaseConnection.connect();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String filename = "C:/Users/sadik/IdeaProjects/Face Recognition/images/" + sdf.format(new Date()) + ".jpg";
        Imgcodecs.imwrite(filename, face);

        String insertQuery = "INSERT INTO faces (name, image) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, filename);
            preparedStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Image saved to database!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving image to database.");
        }

        JOptionPane.showMessageDialog(this, "Face saved successfully!", "Face Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void changeCascadeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Cascade File");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setCurrentDirectory(new File("C:/Program Files/opencv/build/etc/haarcascades/"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            faceCascade.load(selectedFile.getAbsolutePath());
            JOptionPane.showMessageDialog(this, "Cascade file changed successfully!", "Cascade File Changed", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exitApplication() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            stopCamera();
            capture.release();
            setVisible(false);
        }
    }


}
