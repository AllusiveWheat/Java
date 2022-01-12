import java.awt.Color;
import java.awt.Graphics;

public class Rectangle implements Drawable {
	// instance variables: attributes a Rectangle object "has"
	private int x;
	private int y;
	private int width;
	private int height;
	private Color color;

	// constructor
	public Rectangle(int x, int y, int width, int height, Color color) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}

	// instance methods: things a Rectangle object can "do"
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x, y, width, height);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, width, height);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Color getColor() {
		return color;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	// *** (1) note there are 2 versions of containsPoint methods
	// what do you think containsPoint from a Rectangle object's perspective is doing?
	public boolean containsPoint(int x, int y) {
		return (x > this.x && x < this.x + width && y > this.y && y < this.y + height);
	}


}
