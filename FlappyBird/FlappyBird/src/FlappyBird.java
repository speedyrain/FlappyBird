import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    enum GameState { START_SCREEN, MENU, WAITING_TO_START, PLAYING, GAME_OVER }
    GameState gameState = GameState.START_SCREEN;

    int boardWidth = 360;
    int boardHeight = 640;

    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;

    int pipeHeight = 512;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;
        Bird(Image img) { this.img = img; }
    }

    class Pipe {
        int x = boardWidth;
        int y = 0;
        int width = 64;
        int height = 512;
        Image img;
        boolean passed = false;
        Pipe(Image img) { this.img = img; }
    }

    Bird bird;
    int velocityX = -4;
    int velocityY = 0;
    int gravity = 1;
    ArrayList<Pipe> pipes;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipeTimer;

    boolean gameOver = false;
    double score = 0;
    double highScore = 0;

    int menuIndex = 0;
    String[] menuOptions = { "New Game", "Highest Score", "Exit" };

    Font flappyFont;

    public FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        // Load custom font from file
        try {
            System.out.println("Trying to load custom font...");
            FileInputStream is = new FileInputStream("flappy.ttf");
            flappyFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(48f);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(flappyFont);
            System.out.println("Font loaded successfully.");
        } catch (IOException | FontFormatException e) {
            System.err.println("Failed to load font! Using default.");
            e.printStackTrace();
            flappyFont = new Font("Arial", Font.BOLD, 48);
        }

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipeTimer = new Timer(1500, e -> placePipes());
        gameLoop = new Timer(1000 / 60, this);
    }

    void placePipes() {
        int randomPipeY = (int) (0 - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);
        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        switch (gameState) {
            case START_SCREEN:
                g.setColor(Color.RED);
                g.setFont(flappyFont);
                g.drawString("Flappy Bird", 60, 140);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                g.setColor(Color.white);
                g.drawString("Press any key to Start", 60, boardHeight / 2);
                break;

            case MENU:
                g.setColor(Color.RED);
                g.setFont(flappyFont);
                g.drawString("Flappy Bird", 60, 140);
                g.setFont(new Font("Arial", Font.BOLD, 28));
                g.setColor(Color.WHITE);
                //g.drawString("Menu:", 130, 150);
                for (int i = 0; i < menuOptions.length; i++) {
                    if (i == menuIndex) g.setColor(Color.YELLOW);
                    else g.setColor(Color.WHITE);
                    g.drawString(menuOptions[i], 100, 300 + i * 40);
                }
                break;

                case WAITING_TO_START:
                g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
                for (Pipe pipe : pipes) {
                    g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
                }
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.PLAIN, 24));
                g.drawString("Press SPACE to Start!", 60, boardHeight / 2);
                break;
            


            case PLAYING:
                g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
                for (Pipe pipe : pipes) {
                    g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
                }
                g.setColor(Color.white);
                g.setFont(new Font("Arial", Font.BOLD, 28));
                g.drawString("Score: " + (int) score, 10, 30);
                break;

                case GAME_OVER:
                g.drawImage(birdImg, bird.x, bird.y, bird.width, bird.height, null);
                for (Pipe pipe : pipes) {
                    g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
                }
            
                g.setColor(Color.RED);
                g.setFont(flappyFont);  // Use your custom font here
                g.drawString(" Game Over!", 40, 300);  // Adjust position as needed
            
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.setColor(Color.WHITE);
                g.drawString("  Score: " + (int) score, 100, 340);
                g.drawString("  Press space to Restart", 35, 400);
                break;
            
        }
    }

    public void move() {
        velocityY += gravity;
        bird.y += velocityY;
        bird.y = Math.max(bird.y, 0);

        for (Pipe pipe : pipes) {
            pipe.x += velocityX;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }
            if (collision(bird, pipe)) {
                gameOver = true;
                gameState = GameState.GAME_OVER;
            }
        }

        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);

        if (bird.y > boardHeight) {
            gameOver = true;
            gameState = GameState.GAME_OVER;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            move();
            repaint();
        }
    }

    public void keyPressed(KeyEvent e) {
        switch (gameState) {
            case START_SCREEN:
                gameState = GameState.MENU;
                repaint();
                break;
            case MENU:
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    menuIndex = (menuIndex + menuOptions.length - 1) % menuOptions.length;
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    menuIndex = (menuIndex + 1) % menuOptions.length;
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (menuIndex == 0) startGame();
                    else if (menuIndex == 1) JOptionPane.showMessageDialog(this, "Highest Score: " + (int) highScore);
                    else if (menuIndex == 2) System.exit(0);
                }
                repaint();
                break;
                
                case WAITING_TO_START:
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameLoop.start();
                    gameState = GameState.PLAYING;
                }
                break;
            
                
            case PLAYING:
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    velocityY = -9;
                }
                break;
            case GAME_OVER:
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (score > highScore) highScore = score;
                    resetGame();
                    gameState = GameState.MENU;
                    repaint();
                }
                break;
        }
    }

    void startGame() {
        bird.y = birdY;
        velocityY = 0;
        pipes.clear();
        score = 0;
        gameOver = false;
        gameState = GameState.WAITING_TO_START;
        placePipeTimer.start();
        repaint();
    }
    

    void resetGame() {
        placePipeTimer.stop();
        gameLoop.stop();
        bird.y = birdY;
        pipes.clear();
        score = 0;
        velocityY = 0;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}
