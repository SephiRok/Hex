package world.ai;

import java.awt.Color;

import core.Message;

import world.Player;
import world.World;

/**
 * An AI-controlled player.
 */
public class AIPlayer extends Player {

	private Agent agent;
	private String filename;
	
	/**
	 * Constructor.
	 * 
	 * @param world Associated world.
	 * @param type Type of AI player.
	 * @param id Player ID.
	 * @param color Player color.
	 */
	public AIPlayer(World world, Player.TYPE type, byte id, Color color) {
		super(world, type, id, color);
		if (type == Player.TYPE.AI_ANN) {
			setAgent(new world.ai.td.ann.Agent(this));
		} else if (type == Player.TYPE.AI_RANDOM) {
			setAgent(new world.ai.random.Agent(this));
		} else if (type == Player.TYPE.AI_TABLE) {
			setAgent(new world.ai.td.table.Agent(this));
		}
	}
	
	public Agent getAgent() {
		return agent;
	}
	
	public void load() {
		System.out.println("Loading policy for AI player " + getID() 
				+ " from file " + filename + " ...");
		agent.readFromFile(filename);
	}
	
	public void onMessage(Message message) {
		super.onMessage(message);
		if (message.getSender() == getWorld().getMessenger()) {
			if (message instanceof World.MessageNewGame) {
				agent.onEpisodeBegin();
			} else if (message instanceof World.MessageGameOver) {
				boolean goalAchieved = getWorld().getWinner() == this;
				agent.onEpisodeEnd(goalAchieved);
			}
		}
	}

	public void process() {
		getWorld().take(agent.chooseAction(getWorld().getState()));
	}
	
	public void reward(double reward) {
		agent.reward(getWorld().getState(), reward);
	}
	
	public void save() {
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		String date = dateFormat.format(new Date());
		
		System.out.println("Saving policy for AI player " + getID() 
				+ " to file " + filename + " ...");
		agent.writeToFile(filename);
	}
	
	public void setAgent(Agent agent) {
		this.agent = agent;
		filename = getWorld().getWidth() + "x" + getWorld().getHeight() + "." 
				+ getID() + "." + agent.getType() + ".ai.bin";
	}
	
}
