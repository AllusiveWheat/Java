import java.awt.*;
import javax.swing.*;

public class DrawingPanel extends JPanel {
	int numOfRects = 0;
	Rectangle[] rectangles = new Rectangle[2000];
	// instance variables represent state.
	int counter = 0;

	@Override
	public void paintComponent(Graphics g) {
		System.out.println(numOfRects);
		super.paintComponent(g);
		counter++;
		g.setColor(Color.PINK);
		g.drawRect(0, 0, getWidth(), getHeight());

		for (int i = 0; i < numOfRects; i++) {
			rectangles[i].draw(g);
		}
	}

	public void addRectangle(int x, int y, int width, int height, Color color) {
		rectangles[numOfRects] = new Rectangle(x, y, width, height, color);
		numOfRects++;
	}
}
