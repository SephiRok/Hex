package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JFrame;

public class HexLayout implements LayoutManager {

	private int columns;
	private int gap;
	private boolean proportionalSize;
	private int rows;
	
	public HexLayout() {
		this(1, 0, -1);
	}
	
	public HexLayout(int rows, int columns) {
		this(rows, columns, -1);
	}
	
	public HexLayout(int rows, int columns, int gap) {
		this(rows, columns, gap, true);
	}
	
	public HexLayout(int rows, int columns, int gap, boolean proportionalSize) {
		this.rows = rows;
		this.columns = columns;
		this.gap = gap;
		this.proportionalSize = proportionalSize;
	}
	
	public int getRows() {
		return rows;
	}
	
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public void setColumns(int columns) {
		this.columns = columns;
	}
	
	public int getGap() {
		return gap;
	}
	
	public void setGap(int gap) {
		this.gap = gap;
	}
	
	public void addLayoutComponent(String name, Component component) {}
	
	public void removeLayoutComponent(Component component) {}
	
	public Dimension preferredLayoutSize(Container parent) {
		return minimumLayoutSize(parent);
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		synchronized (parent.getTreeLock()) {
			// TODO: Fix for proportionalSize false.
			Insets insets = parent.getInsets();
			int components = parent.getComponentCount();
			int rows = this.rows;
			int columns = this.columns;
			if (rows > 0) {
				columns = (components + rows - 1) / rows;
			} else {
				rows = (components + columns - 1) / columns;
			}
			int componentsWidth = 0;
			int componentsHeight = 0;
			for (int i = 0; i < components; ++i) {
				Component component = parent.getComponent(i);
				Dimension dimension = component.getPreferredSize(); // getMinimumSize();
				componentsWidth = Math.max(componentsWidth, dimension.width);
				componentsHeight = Math.max(componentsHeight, dimension.height);
			}
			int width = insets.left + insets.right + columns * componentsWidth
					+ (columns - 1) * gap;
			int height = insets.top + insets.bottom + rows * componentsHeight
					+ (rows - 1) * gap;
			if (rows > 1) {
				width += (int) (componentsWidth * 0.5);
				height /= rows;
				height += (int) (height * (rows - 1) * 0.75);
			}
			return new Dimension(width, height);
		}
	}
	
	public void layoutContainer(Container parent) {
		synchronized (parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int components = parent.getComponentCount();
			int rows = this.rows;
			int columns = this.columns;
			if (components == 0) {
				return;
			}
			if (rows > 0) {
				columns = (components + rows - 1) / rows;
			} else {
				rows = (components + columns - 1) / columns;
			}
			int width = parent.getWidth() - (insets.left + insets.right);
			int height = parent.getHeight() - (insets.top + insets.bottom);
			if (proportionalSize) {
				width = (int) ((width - (columns - 1) * gap) 
						/ (columns + (rows > 1 ? 0.5 : 0.0)));
			} else {
				width = (int) ((width - (columns - 1) * gap) 
						/ (columns + ((rows - 1) * 0.5)));
			}
			double effectiveRows = 1 + ((rows - 1) * 0.75);
			height = (int) ((height - (rows - 1) * gap) / effectiveRows);
			int xOffset = (width + gap) / 2;
			int yOffset = (int) (height * 0.75);
			boolean staggeredRow = false;
			for (int row = 0, y = insets.top; row < rows; 
					++row, y += yOffset + gap) {
				int offset = 0;
				if (proportionalSize) {
					if (staggeredRow) {
						offset = xOffset;
					}
				} else {
					offset = xOffset * row;
				}
				for (int column = 0, x = insets.left; column < columns; 
						++column, x += width + gap) {
					int i = row * columns + column;
					if (i < components) {
						parent.getComponent(i).setBounds(x + offset, y, width, 
								height);
					}
				}
				staggeredRow = !staggeredRow;
			}
		}
	}
	
	public void setProportionalSize(boolean proportionalSize) {
		this.proportionalSize = proportionalSize;
	}
	
	public String toString() {
		return getClass().getName() + "[gap=" + gap + ",rows=" + rows 
				+ ",columns=" + columns + "]";
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("HexLayout");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new HexLayout(3, 0, -1, false));
		for (int i = 0; i < 9; ++i) {
			HexButton button = new HexButton(String.valueOf(i));
			button.setForeground(Color.black);
			button.setBackground(Color.green);
			frame.add(button);
        }
		frame.pack();
		frame.setSize(700, 400);
		frame.setVisible(true);
    }

}
