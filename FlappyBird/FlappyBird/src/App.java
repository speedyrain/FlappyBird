// Updated App.java to launch FlappyBird with menu system
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird Game");
        FlappyBird game = new FlappyBird();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
