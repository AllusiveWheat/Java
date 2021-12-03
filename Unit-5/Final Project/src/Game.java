import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;

// Simple JPanel
public class Game extends JPanel implements ActionListener {

    // Constructor
    public Game() {
        // Add mouse listener
        add(button);
    }

    public static void main(String[] args) {

        /*
         * NOTE: The string in the following statement goes in the title bar of the window.
         */
        JFrame window = new JFrame("Simple Animation");

        /*
         * NOTE: If you change the name of this class, you must change the name of the class in the
         * next line to match!
         */
        Game drawingArea = new Game();

        drawingArea.setBackground(Color.WHITE);
        window.setContentPane(drawingArea);

        /*
         * NOTE: In the next line, the numbers 600 and 450 give the initial width and height of the
         * drawing array. You can change these numbers to get a different size.
         */
        drawingArea.setPreferredSize(new Dimension(600, 450));

        window.pack();
        window.setLocation(100, 50);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        /*
         * Note: In the following line, you can change true to false. This will prevent the user
         * from resizing the window, so you can be sure that the size of the drawing area will not
         * change. It can be easier to draw the frames if you know the size.
         */
        window.setResizable(true);

        /*
         * NOTE: In the next line, the number 20 gives the time between calls to drawFrame(). The
         * time is given in milliseconds, where one second equals 1000 milliseconds. You can
         * increase this number to get a slower animation. You can decrease it somewhat to get a
         * faster animation, but the speed is limited by the time it takes for the computer to draw
         * each frame.
         */
        Timer frameTimer = new Timer(10, drawingArea);

        window.setVisible(true);
        frameTimer.start();

    } // end main

    // Paint method
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawFrame(g, frameNum, getWidth(), getHeight());
    }

    JButton button = new JButton("Click Me!");
    private int score = 0;

    // Draws the frame
    public void drawFrame(Graphics g, int frameNumber, int width, int height) {
        g.drawString("Frame number " + frameNumber, 40, 50);
        Point mousePosition = getMousePosition();
        int mouseX = mousePosition.x;
        int mouseY = mousePosition.y;
        g.drawString("Mouse position: " + mouseX + ", " + mouseY, 40, 70);
        g.drawString("Width: " + width + ", Height: " + height, 40, 90);
        int randX = (int) (Math.random() * width);
        int randY = (int) (Math.random() * height);
        // Randomly Spawn a button every 100 frames
        if (frameNumber % 100 == 0) {
            button.setBounds(randX, randY, 100, 100);
            add(button);
        }
        g.drawString("Score: " + score, width / 2, height / 2);
        button.setText("");
        button.setBorderPainted(false);
        button.setOpaque(true);
        Icon icon = new ImageIcon("./icon.png");
        button.setIcon(icon);
        button.addActionListener(this);
        button.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Remove the button
                remove(button);
                // Add to the score if the button is clicked

                score = score + 1;
            }
        });
    }

    public Color getPixelColor(int x, int y) throws AWTException {
        // Get the color of the pixel at the mouse position
        Color pixelColor = new Robot().getPixelColor(x, y);
        return pixelColor;
    }

    public Point getMousePosition() {
        return MouseInfo.getPointerInfo().getLocation();
    }


    private int frameNum;

    public void actionPerformed(ActionEvent evt) {
        frameNum++;
        repaint();
    }

}
