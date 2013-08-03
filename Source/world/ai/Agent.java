package world.ai;

public interface Agent {

	public int chooseAction(world.State worldState);

	public double getError();
	
	public double getStateValue();
	
	public String getType();
	
	public void onEpisodeBegin();
	
	public void onEpisodeEnd(boolean goalAchieved);
	
	public void readFromFile(String filename);

	public void reward(world.State worldState, double reward);
	
	public void writeToFile(String filename);

}
