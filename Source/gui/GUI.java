package gui;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.UIManager;

public class GUI extends JFrame implements WindowListener {
	
	private GUIWorker worker = new GUIWorker();

	public GUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setSize(900, 800);
		setLocation(getToolkit().getScreenSize().width / 2 - getWidth() / 2,
				getToolkit().getScreenSize().height / 2 - getHeight() / 2);
		setTitle("Hex");
		addWindowListener(this);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().add(new Parameters(worker), BorderLayout.NORTH);
		getContentPane().add(new Board(worker), BorderLayout.CENTER);
		setVisible(true);
		worker.execute();
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		worker.quit();
		try {
			worker.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

}
