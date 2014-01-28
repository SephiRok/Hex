package world.ai.random;


import java.util.ArrayList;
import java.util.Random;

import world.ai.AIPlayer;

public class Agent implements world.ai.Agent {

	private AIPlayer player;
	private Random rng = new Random();
	
	public Agent(AIPlayer player) {
		this.player = player;
	}
	
	public int chooseAction(world.State worldState) {
		ArrayList<Short> availableActions = worldState.getAvailableActions();
		return availableActions.get(rng.nextInt(availableActions.size()));
	}
	
	public double getError() {
		return 0.0;
	}
	
	public double getStateValue() {
		return 0.0;
	}
	
	public String getType() {
		return "random";
	}
	
	public void onEpisodeBegin() {}
	
	public void onEpisodeEnd(boolean goalAchieved) {}
	
	public void readFromFile(String filename) {}
	
	public void writeToFile(String filename) {}
	
	public void printStats() {
		System.out.println(player.getWL());
	}

	public void reward(world.State worldState, double reward) {}

}
