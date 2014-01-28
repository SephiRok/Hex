package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import core.Message;
import core.MessageReceiver;

import world.Field;
import world.Player;
import world.World;
import world.ai.AIPlayer;

public class Parameters extends JPanel implements ActionListener, MessageReceiver {
	
	public class TextFieldParameter extends JPanel {

		public JLabel label;
		public JTextField textField;
		
		public TextFieldParameter(String caption, String defaultValue) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			label = new JLabel(caption);
			textField = new JTextField(defaultValue, 3);
			textField.setMaximumSize(new Dimension(40, 30));
			label.setLabelFor(textField);
			add(label);
			add(Box.createHorizontalGlue());
			add(textField);
		}
		
	}

	private JLabel[] aiError = new JLabel[2];
	private JButton[] aiLoadButton = new JButton[2];
	private JPanel[] aiParameters = new JPanel[2];
	private JButton[] aiResetButton = new JButton[2];
	private JButton[] aiSaveButton = new JButton[2];
	private JLabel[] aiStateValue = new JLabel[2];
	private TextFieldParameter fieldRewardParameter;
	private TextFieldParameter heightParameter;
	private TextFieldParameter lossRewardParameter;
	private JPanel[] playerParameters = new JPanel[2];
	private JLabel[] playerStreak = new JLabel[2];
	private JComboBox[] playerType = new JComboBox[2];
	private JLabel[] playerWL = new JLabel[2];
	private TextFieldParameter processingDelayParameter;
	private JButton resetPlayersButton;
	private JButton resetWorldButton;
	private TextFieldParameter restartDelayParameter;
	private TextFieldParameter widthParameter;
	private TextFieldParameter winRewardParameter;
	private World world;
	private JPanel worldParameters;
	private GUIWorker worker;
	
	public Parameters(GUIWorker worker) {
		this.worker = worker;
		this.world = worker.getWorld();
		setLayout(new FlowLayout());
		createWorldParameters();
		createPlayerParameters(0, "Blue Player");
		createPlayerParameters(1, "Red Player");
		this.worker.getMessenger().addReceiver(this);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == fieldRewardParameter.textField) {
			world.setFieldReward(Float.valueOf(fieldRewardParameter.textField.getText()));
		} else if (e.getSource() == heightParameter.textField) {
			world.setHeight(Byte.valueOf(heightParameter.textField.getText()));
			for (int i = 0; i < 2; ++i) {
				refreshAIParameters(i);
				refreshStats(i);
			}
		} else if (e.getSource() == lossRewardParameter.textField) {
			world.setLossReward(Float.valueOf(lossRewardParameter.textField.getText()));
		} else if (e.getSource() == processingDelayParameter.textField) {
			world.setProcessingDelay(Integer.valueOf(processingDelayParameter.textField.getText()));
		} else if (e.getSource() == resetPlayersButton) {
			for (int i = 0; i < 2; ++i) {
				world.resetPlayer(i);
				refreshAIParameters(i);
				refreshStats(i);
			}
		} else if (e.getSource() == resetWorldButton) {
			world.reset();
		} else if (e.getSource() == restartDelayParameter.textField) {
			world.setRestartDelay(Integer.valueOf(restartDelayParameter.textField.getText()));
		} else if (e.getSource() == widthParameter.textField) {
			world.setWidth(Byte.valueOf(widthParameter.textField.getText()));
			for (int i = 0; i < 2; ++i) {
				refreshAIParameters(i);
				refreshStats(i);
			}
		} else if (e.getSource() == winRewardParameter.textField) {
			world.setWinReward(Float.valueOf(winRewardParameter.textField.getText()));
		} else {
			for (int i = 0; i < 2; ++i) {
				if (e.getSource() == aiLoadButton[i]) {
					world.loadPlayer(i);
					refreshAIParameters(i);
					refreshStats(i);
					break;
				} else if (e.getSource() == aiResetButton[i]) {
					world.resetPlayer(i);
					refreshAIParameters(i);
					refreshStats(i);
					break;
				} else if (e.getSource() == aiSaveButton[i]) {
					world.savePlayer(i);
					break;
				} else if (e.getSource() == playerType[i]) {
					Player.TYPE type = Player.TYPE.values()[playerType[i].getSelectedIndex()];
					world.setPlayer(i, type);
					refreshAIParameters(i);
					refreshStats(i);
					break;
				}
			}
		}
	}
	
	private void createWorldParameters() {
		worldParameters = new JPanel();
		worldParameters.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("World"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		worldParameters.setLayout(new BoxLayout(worldParameters, 
				BoxLayout.Y_AXIS));
		worldParameters.add(widthParameter = new TextFieldParameter(
				"Width: ",
				String.valueOf(world.getWidth())));
		worldParameters.add(heightParameter = new TextFieldParameter(
				"Height: ",
				String.valueOf(world.getHeight())));
		worldParameters.add(processingDelayParameter = new TextFieldParameter(
				"Processing delay: ",
				String.valueOf(world.getProcessingDelay())));
		worldParameters.add(restartDelayParameter = new TextFieldParameter(
				"Restart delay: ",
				String.valueOf(world.getRestartDelay())));
		worldParameters.add(fieldRewardParameter = new TextFieldParameter(
				"Field reward: ",
				String.valueOf(world.getFieldReward())));
		worldParameters.add(winRewardParameter = new TextFieldParameter(
				"Win reward: ",
				String.valueOf(world.getWinReward())));
		worldParameters.add(lossRewardParameter = new TextFieldParameter(
				"Loss reward: ",
				String.valueOf(world.getLossReward())));
		worldParameters.add(resetWorldButton = new JButton("Reset World"));
		worldParameters.add(resetPlayersButton = new JButton("Reset Players"));
		resetWorldButton.setAlignmentX(CENTER_ALIGNMENT);
		resetPlayersButton.setAlignmentX(CENTER_ALIGNMENT);
		add(worldParameters);
		widthParameter.textField.addActionListener(this);
		heightParameter.textField.addActionListener(this);
		processingDelayParameter.textField.addActionListener(this);
		restartDelayParameter.textField.addActionListener(this);
		fieldRewardParameter.textField.addActionListener(this);
		winRewardParameter.textField.addActionListener(this);
		lossRewardParameter.textField.addActionListener(this);
		resetWorldButton.addActionListener(this);
		resetPlayersButton.addActionListener(this);
	}
	
	private void createPlayerParameters(int playerID, String playerName) {
		playerParameters[playerID] = new JPanel();
		playerParameters[playerID].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(playerName),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		playerParameters[playerID].setLayout(new BoxLayout(
				playerParameters[playerID], BoxLayout.Y_AXIS));
		String[] playerTypes = {"None", 
				"Human",
				"[AI] Random",
				"[AI] Table TD(λ)",
				"[AI] ANN TD(λ)"};
		playerParameters[playerID].add(playerType[playerID] = new JComboBox(
				playerTypes));
		playerParameters[playerID].add(playerWL[playerID] = new JLabel());
		playerParameters[playerID].add(playerStreak[playerID] = new JLabel());
		aiParameters[playerID] = new JPanel();
		aiParameters[playerID].setLayout(new BoxLayout(aiParameters[playerID], 
				BoxLayout.Y_AXIS));
		aiParameters[playerID].add(aiStateValue[playerID] = new JLabel());
		aiParameters[playerID].add(aiError[playerID] = new JLabel());
		JPanel aiButtons = new JPanel(new FlowLayout());
		aiButtons.add(aiResetButton[playerID] = new JButton("Reset"));
		aiButtons.add(aiSaveButton[playerID] = new JButton("Save"));
		aiButtons.add(aiLoadButton[playerID] = new JButton("Load"));
		aiParameters[playerID].add(aiButtons);
		playerParameters[playerID].add(aiParameters[playerID]);
		add(playerParameters[playerID]);

		playerType[playerID].setAlignmentX(LEFT_ALIGNMENT);
		playerWL[playerID].setAlignmentX(LEFT_ALIGNMENT);
		playerStreak[playerID].setAlignmentX(LEFT_ALIGNMENT);
		aiStateValue[playerID].setAlignmentX(LEFT_ALIGNMENT);
		aiError[playerID].setAlignmentX(LEFT_ALIGNMENT);
		aiButtons.setAlignmentX(LEFT_ALIGNMENT);
		aiParameters[playerID].setAlignmentX(LEFT_ALIGNMENT);
		
		aiParameters[playerID].setVisible(false);
		playerType[playerID].addActionListener(this);
		aiResetButton[playerID].addActionListener(this);
		aiSaveButton[playerID].addActionListener(this);
		aiLoadButton[playerID].addActionListener(this);
	}
	
	public void onMessage(Message message) {
		if (message.getSender() == worker.getMessenger()) {
			if (message instanceof World.MessageFieldTaken) {
				if (world.hasNoDelay()) {
					return;
				}
				Field field = ((World.MessageFieldTaken) message).field;
				refreshAIParameters(field.getControllerID());
			} else if (message instanceof World.MessageGameOver) {
				for (int i = 0; i < 2; ++i) {
					if (world.getPlayer(i) != null) {
						playerWL[i].setText(world.getPlayer(i).getWL());
						playerStreak[i].setText(world.getPlayer(i).getStreak());
					}
				}
			}
		}
	}
	
	private void refreshAIParameters(int playerID) {
		Player player = world.getPlayer(playerID);
		if (player == null || !(player instanceof AIPlayer)) {
			if (playerID >= 0 && playerID < aiParameters.length) {
				aiParameters[playerID].setVisible(false);
			}
			return;
		}
		AIPlayer ai = (AIPlayer) player;
		aiStateValue[playerID].setText("State value: "
				+ String.valueOf(ai.getAgent().getStateValue()));
		aiError[playerID].setText("Error: "
				+ String.valueOf(ai.getAgent().getError()));
		aiParameters[playerID].setVisible(true);
	}	
	
	private void refreshStats(int playerID) {
		Player player = world.getPlayer(playerID);
		if (player == null) {
			playerWL[playerID].setVisible(false);
			playerStreak[playerID].setVisible(false);
			return;
		}
		playerWL[playerID].setText(player.getWL());
		playerWL[playerID].setVisible(true);
		playerStreak[playerID].setText(player.getStreak());
		playerStreak[playerID].setVisible(true);
	}

}
