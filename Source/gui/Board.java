package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import core.Message;
import core.MessageReceiver;

import world.Field;
import world.Player;
import world.World;

public class Board extends JPanel implements ActionListener, MessageReceiver {

	private Line[] borders = new Line[4];
	private HexButton[] buttons;
	private JPanel fields;
	private World world;
	private GUIWorker worker;
	
	public Board(GUIWorker worker) {
		this.worker = worker;
		this.world = worker.getWorld();
		setLayout(new BorderLayout());
		add(borders[0] = new Line(Color.GRAY), BorderLayout.NORTH);
		add(borders[1] = new Line(Color.GRAY), BorderLayout.SOUTH);
		add(borders[2] = new Line(Color.GRAY), BorderLayout.WEST);
		add(borders[3] = new Line(Color.GRAY), BorderLayout.EAST);
		add(fields = new JPanel(), BorderLayout.CENTER);
		reset();
		this.worker.getMessenger().addReceiver(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		for (int y = 0; y < world.getHeight(); ++y) {
			for (int x = 0; x < world.getWidth(); ++x) {
				int index = y * world.getWidth() + x;
				if (e.getSource() == buttons[index]) {
					if (world.getWinner() == null) {
						if (world.getCurrentPlayer().getType() 
								== Player.TYPE.HUMAN) {
							world.take(index);
						}
					} else {
						world.reset();
					}
				}
			}
		}
	}
	
	public void onMessage(Message message) {
		if (message.getSender() == worker.getMessenger()) {
			if (world.hasNoDelay()) {
				return;
			}
			if (message instanceof World.MessageFieldTaken) {
				Field field = ((World.MessageFieldTaken) message).field;
				Player controller = world.getPlayer(field.getControllerID());
				if (controller != null) {
					buttons[field.getID()].setBackground(controller.getColor());	
				}
			} else if (message instanceof World.MessageGameOver) {
				Player winner = ((World.MessageGameOver) message).winner;
				for (Field field : world.getState().getFields()) {
					if (field.getControllerID() == winner.getID()) {
						buttons[field.getID()].setText("Winner!");
					} else if (field.getControllerID() != -1) {
						buttons[field.getID()].setText("Loser!");
					}
					//buttons[field.getID()].setEnabled(false);
				}
			} else if (message instanceof World.MessageNewGame) {
				for (Field field : world.getState().getFields()) {
					buttons[field.getID()].setBackground(null);
					buttons[field.getID()].setText(String.valueOf(
							field.getID()));
				}
				setBorders();
			} else if (message instanceof World.MessageResized) {
				reset();
			}
		}
	}
	
	private void reset() {
		fields.removeAll();
		fields.setLayout(new HexLayout(world.getHeight(), 0, -1, false));
		buttons = new HexButton[world.getWidth() * world.getHeight()];
		for (int y = 0; y < world.getHeight(); ++y) {
			for (int x = 0; x < world.getWidth(); ++x) {
				int index = y * world.getWidth() + x;
				HexButton button = new HexButton(String.valueOf(index));
				buttons[index] = button;
				fields.add(button);
				button.addActionListener(this);
			}
		}
		fields.validate();
		fields.repaint();
	}
	
	private void setBorders() {
		Color color = Color.GRAY;
		Player player0 = world.getPlayer(0);
		if (player0 != null) {
			color = player0.getColor();
		}
		borders[0].setColor(color);
		borders[1].setColor(color);
		color = Color.GRAY;
		Player player1 = world.getPlayer(1);
		if (player1 != null) {
			color = player1.getColor();
		}
		borders[2].setColor(color);
		borders[3].setColor(color);
	}

}
