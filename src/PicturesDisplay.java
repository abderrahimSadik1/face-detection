import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PicturesDisplay extends JFrame {

    private final Connection connection;

    public PicturesDisplay() {

        connection = DatabaseConnection.connect();


        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle("Image Display");
        setSize(new Dimension(800, 600));


        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane, BorderLayout.CENTER);


        displayImages(panel);

        setVisible(true);
    }


    private void displayImages(JPanel panel) {
        try {
            String selectQuery = "SELECT name, image FROM faces";
            try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String imagePath = resultSet.getString("image");
                    System.out.println(name + ": " + imagePath);
                    displayImage(panel, name, imagePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayImage(JPanel panel, String name, String imagePath) {
        try {
            ImageIcon imageIcon = new ImageIcon(imagePath);
            Image image = imageIcon.getImage();
            Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
            ImageIcon scaledImageIcon = new ImageIcon(scaledImage);

            JLabel nameLabel = new JLabel(name);
            JLabel imageLabel = new JLabel(scaledImageIcon);


            JPanel imagePanel = new JPanel(new BorderLayout());
            imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


            imagePanel.setBorder(BorderFactory.createTitledBorder(name));

            imagePanel.add(imageLabel, BorderLayout.CENTER);

            panel.add(imagePanel);
            GridLayout gridLayout = new GridLayout(0, 4);
            panel.setLayout(gridLayout);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PicturesDisplay());
    }
}
