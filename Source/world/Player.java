package world;

import java.awt.Color;

import core.Message;
import core.MessageReceiver;

public class Player implements MessageReceiver {

	public enum TYPE {
		NONE,
		HUMAN,
		AI_RANDOM,
		AI_TABLE,
		AI_ANN
	}

	private Color color;
	private byte id;
	private long gamesLost;
	private long gamesLostStreak;
	private long gamesWon;
	private long gamesWonStreak;
	private TYPE type;
	private World world;
	
	public Player(World world, TYPE type, byte id, Color color) {
		this.color = color;
		this.id = id;
		this.type = type;
		this.world = world;
		getWorld().getMessenger().addReceiver(this);
	}
	
	public Color getColor() {
		return color;
	}
	
	public long getGamesLost() {
		return gamesLost;
	}
	
	public long getGamesLostStreak() {
		return gamesLostStreak;
	}

	public long getGamesPlayed() {
		return gamesWon + gamesLost;
	}
	
	public long getGamesWon() {
		return gamesWon;
	}
	
	public long getGamesWonStreak() {
		return gamesWonStreak;
	}
	
	public byte getID() {
		return id;
	}
	
	public String getStats() {
		long winPercent = getGamesPlayed() > 0
				? Math.round((double) getGamesWon() 
						/ (double) getGamesPlayed() * 100.0)
				: 0;
		String streak = getGamesLostStreak() > 0 
				? String.valueOf(getGamesLostStreak()) + " loss streak"
				: String.valueOf(getGamesWonStreak()) + " win streak" ;
		return "W/L: " + getGamesWon() + "/" + getGamesLost() 
				+ " (" + winPercent + "%), " + streak;
	}
	
	public TYPE getType() {
		return type;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void load() {}
	
	public void onMessage(Message message) {
		if (message.getSender() == getWorld().getMessenger()) {
			if (message instanceof World.MessageGameOver) {
				boolean goalAchieved = getWorld().getWinner() == this;
				if (goalAchieved) {
					setGamesWon(getGamesWon() + 1);
					setGamesWonStreak(getGamesWonStreak() + 1);
					setGamesLostStreak(0);
				} else {
					setGamesLost(getGamesLost() + 1);
					setGamesLostStreak(getGamesLostStreak() + 1);
					setGamesWonStreak(0);
				}
			}
		}
	}
	
	public void process() {}
	
	public void reward(double reward) {}
	
	public void save() {}
	
	public void setGamesLost(long gamesLost) {
		this.gamesLost = gamesLost;
	}
	
	public void setGamesLostStreak(long gamesLostStreak) {
		this.gamesLostStreak = gamesLostStreak;
	}
	
	public void setGamesWon(long gamesWon) {
		this.gamesWon = gamesWon;
	}
	
	public void setGamesWonStreak(long gamesWonStreak) {
		this.gamesWonStreak = gamesWonStreak;
	}
	
}
