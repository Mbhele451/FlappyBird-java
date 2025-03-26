import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.*;

@SuppressWarnings("serial")
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    
    private static final int BOARD_WIDTH = 360;
    private static final int BOARD_HEIGHT = 640;
    private static final String HIGH_SCORE_FILE = "flappy_high_score.txt";
    
    // Images
    private Image backgroundImg;
    private Image birdImg;
    private Image topPipeImg;
    private Image bottomPipeImg;
    
    // Bird
    private static final int BIRD_X = BOARD_WIDTH / 8;
    private static final int BIRD_Y = BOARD_HEIGHT / 2;
    private static final int BIRD_WIDTH = 34;
    private static final int BIRD_HEIGHT = 24;
    
    // Pipes
    private static final int PIPE_X = BOARD_WIDTH;
    private static final int PIPE_WIDTH = 64;
    private static final int PIPE_HEIGHT = 512;
    
    // Game settings
    private static final int GAME_UPDATE_RATE = 1000 / 60;
    private static final int PIPE_SPAWN_INTERVAL = 1500;
    private static final int JUMP_VELOCITY = -9;
    private static final int GRAVITY = 1;
    private static final int PIPE_VELOCITY = -4;
    
    class Bird {
        int x = BIRD_X;
        int y = BIRD_Y;
        int width = BIRD_WIDTH;
        int height = BIRD_HEIGHT;
        Image img;
        
        Bird(Image img) {
            this.img = img;
        }
    }
    
    class Pipe {
        int x = PIPE_X;
        int y = 0;
        int width = PIPE_WIDTH;
        int height = PIPE_HEIGHT;
        Image img;
        boolean passed = false;
        
        Pipe(Image img) {
            this.img = img;
        }
    }
    
    // Game Logic 
    private Bird bird;
    private int velocityX = PIPE_VELOCITY;
    private int velocityY = 0;
    private int gravity = GRAVITY;
    
    private ArrayList<Pipe> pipes;
    public Random random = new Random();
    
    private Timer gameLoop;
    private Timer placePipesTimer;
    private boolean gameOver = false;
    private double score = 0;
    private int highScore = 0;
    
    // Constructor
    public FlappyBird() {
        // Load high score
        loadHighScore();
        
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        
        // Load images with error handling
        try {
            backgroundImg = loadImage("./flappybirdbg.png");
            birdImg = loadImage("./flappybird.png");
            topPipeImg = loadImage("./toppipe.png");
            bottomPipeImg = loadImage("./bottompipe.png");
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Failed to load game resources", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        // Bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<>();
        
        // Place pipe timer
        placePipesTimer = new Timer(PIPE_SPAWN_INTERVAL, e -> placePipes());
        placePipesTimer.start();
        
        // Game Timer
        gameLoop = new Timer(GAME_UPDATE_RATE, this);
        gameLoop.start();
    }
    
    // Helper method to load images with error handling
    private Image loadImage(String path) {
        ImageIcon icon = new ImageIcon(getClass().getResource(path));
        if (icon.getImage() == null) {
            throw new RuntimeException("Image not found: " + path);
        }
        return icon.getImage();
    }
    
    // Load high score from file
    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            // If file doesn't exist or is invalid, high score remains 0
            highScore = 0;
        }
    }
    
    // Save high score to file
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("Error saving high score: " + e.getMessage());
        }
    }
    
    public void placePipes() {
        int randomPipeY = (int) (0 - PIPE_HEIGHT/4 - Math.random() * (PIPE_HEIGHT/2));
        int openingSpace = BOARD_HEIGHT / 4;
        
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + PIPE_HEIGHT + openingSpace;
        pipes.add(bottomPipe);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    
    public void draw(Graphics g) {
        // Background
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);
        
        // Bird
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);
        
        // Pipes
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        
        // Score
        g.setColor(Color.black);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        // Current Score
        g.drawString("Score: " + (int) score, 10, 30);
        
        // High Score
        g.drawString("High Score: " + highScore, 10, 60);
        
        // Game Over
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.setColor(Color.RED);
            g.drawString("Game Over", BOARD_WIDTH / 2 - 100, BOARD_HEIGHT / 2);
        }
    }
    
    public void move() {
        // Bird
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);
        
        // Pipes
        for (int i = pipes.size() - 1; i >= 0; i--) {
            Pipe pipe = pipes.get(i);
            pipe.x += velocityX;
            
            // Remove pipes that are off-screen
            if (pipe.x + pipe.width < 0) {
                pipes.remove(i);
                continue;
            }
            
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }
            
            if (collision(bird, pipe)) {
                gameOver = true;
            }
        }
        
        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
        } 
    }
    
    public boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
            
            // Update high score
            if ((int) score > highScore) {
                highScore = (int) score;
                saveHighScore();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameOver) {
                velocityY = JUMP_VELOCITY;
            } else {
                // Restart game
                resetGame();
            }
        }
    }
    
    private void resetGame() {
        bird.y = BIRD_Y;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameLoop.start();
        placePipesTimer.start();
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
}