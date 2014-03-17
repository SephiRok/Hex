package world;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import core.Message;
import core.Messenger;

import world.ai.AIPlayer;

public class World implements Runnable {

	public class MessageFieldTaken extends Message {
		public Field field;
		public MessageFieldTaken(Field field) {
			this.field = field;
		}
	}
	public class MessageGameOver extends Message {
		public Player winner;
		public MessageGameOver(Player winner) {
			this.winner = winner;
		}
	}
	public class MessageNewGame extends Message {}
	public class MessageResized extends Message {}
	
	// 3^(n*n) is the simple upper bound for the size of the state space.
	// More detailed: http://math.stackexchange.com/questions/89128/how-many-states-in-the-game-of-hex
	// Check tic-tac-toe combinatorics for reasoning and additional info.
	
	private int currentPlayerID;
	private int processingDelay = 0;
	private float fieldReward = 0;
	private byte height = 3;
	private float lossReward = -1;
	private Messenger messenger = new Messenger();
	private Player[] players = new Player[2];
	private int restartDelay = 1;
	private AtomicBoolean quit = new AtomicBoolean(false);
	private State state;
	private byte width = 3;
	private Player winner;
	private float winReward = 1;
	
	public World() {
		state = new State(this);
		currentPlayerID = 0;
	}
	
	private void advanceCurrentPlayer() {
		synchronized (this) {
			currentPlayerID = (currentPlayerID + 1) % players.length;
		}
	}
	
	public Player getCurrentPlayer() {
		synchronized (this) {
			return players[currentPlayerID];
		}
	}

	public float getFieldReward() {
		synchronized (this) {
			return fieldReward;
		}
	}
	
	public byte getHeight() {
		return height;
	}
	
	public float getLossReward() {
		synchronized (this) {
			return lossReward;
		}
	}
	
	public Messenger getMessenger() {
		return messenger;
	}
	
	public Player getPlayer(int id) {
		synchronized (this) {
			if ((id < 0) || (id >= players.length)) {
				return null;
			}
			return players[id];
		}
	}

	public int getProcessingDelay() {
		return processingDelay;
	}
	
	public int getRestartDelay() {
		return restartDelay;
	}
	
	public State getState() {
		return state;
	}
	
	public byte getWidth() {
		return width;
	}
	
	public Player getWinner() {
		synchronized (this) {
			return winner;
		}
	}
	
	public float getWinReward() {
		synchronized (this) {
			return winReward;
		}
	}
	
	public boolean hasHumanPlayers() {
		return players[0] != null 
				&& players[0].getType() == Player.TYPE.HUMAN
				|| players[1] != null
				&& players[1].getType() == Player.TYPE.HUMAN;
	}
	
	public boolean hasNoDelay() {
		return processingDelay <= 0 && restartDelay <= 0 && !hasHumanPlayers();
	}
	
	private boolean isWinningMove(Field field) {
		return walk(field, new HashSet<Short>(), new boolean[]{false, false});
	}
	
	public void loadPlayer(int playerID) {
		synchronized (this) {
			Player player = getPlayer(playerID);
			if (player != null) {
				player.load();
				reset();
			}
		}
	}
	
	public void quit() {
		quit.set(true);
	}
	
	private void process() {
		if (getCurrentPlayer() == null) {
			advanceCurrentPlayer();
		} else {
			getCurrentPlayer().process();
		}
	}
	
	public void reset() {
		synchronized (this) {
			winner = null;
			state.reset();
			currentPlayerID = 0;
			messenger.sendMessage(new MessageNewGame());
		}
	}
	
	public void resetPlayer(int id) {
		if (players[id] != null) {
			setPlayer(id, players[id].getType());
		}
	}
	
	public void resetPlayers() {
		for (int i = 0; i < players.length; ++i) {
			resetPlayer(i);
		}
	}
	
	public void run() {
		reset();
		int delay;
		while (!quit.get()) {
			synchronized (this) {
				delay = this.processingDelay;
				if (winner == null) {
					process();
					if (!hasHumanPlayers() && winner != null) {
						delay = this.restartDelay;
					}
				} else if (!hasHumanPlayers()) {
					reset();
				}
			}
			if (delay >= 0) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void savePlayer(int playerID) {
		synchronized (this) {
			Player player = getPlayer(playerID);
			if (player != null) {
				player.save();
				reset();
			}
		}
	}
	
	public void setFieldReward(float fieldReward) {
		synchronized (this) {
			this.fieldReward = fieldReward;
		}
	}
	
	public void setHeight(byte height) {
		synchronized (this) {
			if (this.height == height) {
				return;
			}
			this.height = height;
			state = new State(this);
			messenger.sendMessage(new MessageResized());
			resetPlayers();
			reset();
		}
	}
	
	public void setLossReward(float lossReward) {
		synchronized (this) {
			this.lossReward = lossReward;
		}
	}
	
	public void setPlayer(int id, Player.TYPE type) {
		Color color;
		if (id == 0) {
			color = Color.BLUE;
		} else {
			color = Color.RED;
		}
		synchronized (this) {
			if (type == Player.TYPE.NONE) {
				players[id] = null;
			} else if (type == Player.TYPE.HUMAN) {
				players[id] = new Player(this, type, (byte) id, color);
			} else {
				players[id] = new AIPlayer(this, type, (byte) id, color);
			}
			reset();
		}
	}
	
	public void setProcessingDelay(int delay) {
		synchronized (this) {
			this.processingDelay = delay;
		}
	}
	
	public void setRestartDelay(int delay) {
		synchronized (this) {
			this.restartDelay = delay;
		}
	}
	
	public void setWidth(byte width) {
		synchronized (this) {
			if (this.width == width) {
				return;
			}
			this.width = width;
			state = new State(this);
			messenger.sendMessage(new MessageResized());
			resetPlayers();
			reset();
		}
	}
	
	public void setWinReward(float winReward) {
		synchronized (this) {
			this.winReward = winReward;
		}
	}
	
	public void take(int fieldID) {
		synchronized (this) {
			if (getCurrentPlayer() == null) {
				return;
			}
			Field field = state.getFields()[fieldID];
			if (field.getControllerID() == -1) {
				final Player currentPlayer = getCurrentPlayer();
				field.setController(currentPlayer);
				messenger.sendMessage(new MessageFieldTaken(field));
				if (isWinningMove(field)) {
					winner = currentPlayer;
					winner.reward(winReward);
					for (Player player : players) {
						if (player != null && player != winner) {
							player.reward(lossReward);
						}
					}
					messenger.sendMessage(new MessageGameOver(winner));
					currentPlayerID = 0;
				} else {
					currentPlayer.reward(fieldReward);
					advanceCurrentPlayer();
				}
			}
		}
	}
	
	private boolean walk(Field field, Set<Short> walkedFields, 
			boolean[] connectedEdges) {
		walkedFields.add(field.getID());
		if (field.isOnEdge0(this)) {
			connectedEdges[0] = true;
			if (connectedEdges[1] == true) {
				return true;
			}
		} else if (field.isOnEdge1(this)) {
			connectedEdges[1] = true;
			if (connectedEdges[0] == true) {
				return true;
			}
		}
		for (Field neighbor : field.getNeighbors()) {
			if ((neighbor.getControllerID() != field.getControllerID())
					|| (walkedFields.contains(neighbor.getID()))) {
				continue;
			}
			if (walk(neighbor, walkedFields, connectedEdges)) {
				return true;
			}
		}
		return false;
	}
	
}
