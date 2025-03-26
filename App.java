import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        // Use SwingUtilities to ensure thread safety for Swing components
        SwingUtilities.invokeLater(() -> {
            // Create game dimensions
            int boardWidth = 360;
            int boardHeight = 640;

            // Create the frame with more configuration options
            JFrame frame = new JFrame("Flappy Bird");
            frame.setPreferredSize(new Dimension(boardWidth, boardHeight));
            frame.setMaximumSize(new Dimension(boardWidth, boardHeight));
            frame.setMinimumSize(new Dimension(boardWidth, boardHeight));

            // Center the frame on the screen
            frame.setLocationRelativeTo(null);
            
            // Prevent resizing to maintain game layout
            frame.setResizable(false);
            
            // More robust close operation
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create instance of FlappyBird
            FlappyBird flappyBird = new FlappyBird();
            frame.add(flappyBird);
            
            // Adjust frame to preferred size
            frame.pack();
            
            // Ensure the game panel has focus for keyboard input
            flappyBird.requestFocusInWindow();
            
            // Make the frame visible
            frame.setVisible(true);
        });
    }
}