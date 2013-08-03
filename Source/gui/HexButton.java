package gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * A six sided toggle button.
 * 
 * This is not guaranteed to be a perfect hexagon, it is just guaranteed to have
 * six sides in the form of a hexagon. To be a perfect hexagon the size of this 
 * component must have a height to width ratio of 1 to 0.866.
 */
public class HexButton extends JButton {

	private Polygon hexagon = new Polygon();
	
	public HexButton() {
		setContentAreaFilled(false);
	}
	
	public HexButton(String string) {
		super(string);
		setContentAreaFilled(false);
	}
	
	private void calculateCoordinates() {
		hexagon.reset();
		int w = getWidth() - 1;
		int h = getHeight() - 1;
		int ratio = (int) (h * 0.25);
		hexagon.addPoint(w / 2, 0);
		hexagon.addPoint(w, ratio);
		hexagon.addPoint(w, h - ratio);
		hexagon.addPoint(w / 2, h);
		hexagon.addPoint(0, h - ratio);
		hexagon.addPoint(0, ratio);
	}
	
	public boolean contains(Point p) {
		return hexagon.contains(p);
	}
	
	public boolean contains(int x, int y) {
		return hexagon.contains(x, y);
	}
	
	protected void paintComponent(Graphics g) {
		if (isSelected()) {
			g.setColor(Color.LIGHT_GRAY);
		} else {
			g.setColor(getBackground());
		}
		g.fillPolygon(hexagon);
		g.setColor(getForeground());
		g.drawPolygon(hexagon);
		FontMetrics fontMetrics = getFontMetrics(getFont());
		Rectangle viewRectangle = getBounds();
		Rectangle iconRectangle = new Rectangle();
		Rectangle textRectangle = new Rectangle();
		SwingUtilities.layoutCompoundLabel(this, fontMetrics, getText(), null, 
				SwingUtilities.CENTER, SwingUtilities.CENTER, 
				SwingUtilities.BOTTOM, SwingUtilities.CENTER, viewRectangle, 
				iconRectangle, textRectangle, 0);
		Point location = getLocation();
		g.drawString(getText(), textRectangle.x - location.x, 
				textRectangle.y - location.y + fontMetrics.getAscent());
	}
	
	protected void paintBorder(Graphics g) {
		// Do not paint a border.
	}
	
	protected void processMouseEvent(MouseEvent e) {
		if (contains(e.getPoint())) {
			super.processMouseEvent(e);
		}
	}
	
	public void setSize(int width, int height) {
		super.setSize(width, height);
		calculateCoordinates();
	}
	
	public void setBounds(int x, int y, int width, int height) {
		super.setBounds(x, y, width, height);
		calculateCoordinates();
	}
	
	public void setBounds(Rectangle r) {
		super.setBounds(r);
		calculateCoordinates();
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("HexButton");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new HexButton("Hexagonal Button"));
		frame.setSize(400, 400);
		frame.setVisible(true);
	}

}
