import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    public Main() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Main Menu");
        setSize(new Dimension(300, 200));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));


        JButton cameraButton = new JButton("Open Camera Frame");
        JButton displayButton = new JButton("Open Display Frame");

        cameraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
                SwingUtilities.invokeLater(() -> new Camera());
            }
        });

        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> new PicturesDisplay());
            }
        });

        panel.add(cameraButton);
        panel.add(displayButton);

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
