package gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class Line extends JPanel {

	private Color color;
	
	public Line(Color color) {
		this.color = color;
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(color);
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	public void setColor(Color color) {
		this.color = color;
		repaint();
	}
	
}
