package SimplePaintRefactor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class SimplePaintPanel extends JPanel {

    int currentColor = 0;
    ArrayList<Integer> colorList = new ArrayList<Integer>();
    Line lastLine;

    SimplePaintPanel() {

        this.setBackground(Color.BLACK);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // 3 Pixel border using BordorFactory
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        for (Line line : lines) {
            g2.setColor(new Color(line.getColorCode()));
            g2.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
            lastLine = line;
        }
    }

    ArrayList<Line> lines = new ArrayList<Line>();

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }



    public void removeLastLine() {
        lines.remove(lastLine);
    }


}
