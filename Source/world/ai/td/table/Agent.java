package world.ai.td.table;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import world.ai.AIPlayer;

public class Agent implements world.ai.Agent {

	private final static float DEFAULT_VALUE = 0.0f;
	
	// Only guaranteed to converge to the optimal solution if exploration and step size decrease over time.
	
	/**
	 * From 0.00 (random) to 1.00 (greedy).
	 *
	 */
	// May not discover shortest path with -1.0 field reward without at least 1% randomness?
	// Could keep decreasing randomness.
	private final static float GREEDINESS = 1.0f;
	/**
	 * Gamma.
	 * 
	 * Episodic task.
	 */
	private final static float DISCOUNT_RATE = 1.0f;
	
	/**
	 * Alpha. Learning rate.
	 * 
	 * Only guaranteed to converge (in the mean) to the optimal solution if 
	 * sufficiently small.
	 * 
	 * For any fixed policy �, the TD algorithm described above has been proved to converge to v�, in the mean for a constant step-size parameter if it is sufficiently small, and with probability 1 if the step-size parameter decreases according to the usual stochastic approximation conditions (2.7). 
	 * 
	 * Has to keep decreasing with function approximation to converge (?).
	 */
	private final static float STEP_SIZE = 0.95f;
	
	/**
	 * Lambda.
	 */
	private final static float TRACE_DECAY = 0.6f;

	private TreeMap<Value, ArrayList<Short>> actionValues 
			= new TreeMap<Value, ArrayList<Short>>();
	private float error;
	private ArrayList<State> pathStates = new ArrayList<State>();
	private AIPlayer player;
	private float reward;
	private Random rng = new Random();
	private HashMap<State, State> states = new HashMap<State, State>();
	
	public Agent(AIPlayer player) {
		this.player = player;
	}
	
	private void addWorldState(world.State worldState) {
		State state = states.get(worldState);
		if (state != null) {
			pathStates.add(state);
			return;
		}
		state = new State(worldState);
		state.getValue().set(DEFAULT_VALUE);
		states.put(state, state);
		pathStates.add(state);
	}
	
	public int chooseAction(world.State worldState) {
		synchronized (this) {
			
			// Evaluate available actions.
			actionValues.clear();
			ArrayList<Short> availableActions = worldState.getAvailableActions();
			for (short action : availableActions) {				
				worldState.getFields()[action].setController(player);
				Value value = getStateValueInstance(worldState);
				ArrayList<Short> actions = actionValues.get(value);
				if (actions == null) {
					actions = new ArrayList<Short>();
				}
				actions.add(action);
				actionValues.put(value, actions);
				worldState.getFields()[action].reset();
			}
			
			// Select action.
			short action;
			if (rng.nextInt(100) >= GREEDINESS * 100) {
				action = availableActions.get(rng.nextInt(
						availableActions.size()));
			} else {
				ArrayList<Short> highestValueActions = actionValues.get(
						actionValues.lastKey());
				
				// Faster convergence with .get(0) for 3x3.
//				action = highestValueActions.get(rng.nextInt(
//						highestValueActions.size()));
				action = highestValueActions.get(0);
				
			}

			// Learn (on-policy value prediction).
			// For off-policy, when non-greedy action is picked, learn
			// as if optimal was picked, but set eligibility trace to 0.
			worldState.getFields()[action].setController(player);
			learn((float) getStateValue(worldState));
			addWorldState(worldState);
			worldState.getFields()[action].reset();
			
			return action;
		}
	}
	
	public double getError() {
		synchronized (this) {
			return error;	
		}
	}
	
	private State getState(int stateNumStepsBack) {
		synchronized (this) {
			if (stateNumStepsBack >= pathStates.size()) {
				return null;
			}
			return pathStates.get(pathStates.size() - 1 - stateNumStepsBack);
		}
	}
	
	public double getStateValue(world.State worldState) {
		State aiState = states.get(worldState);
		if (aiState == null) {
			return DEFAULT_VALUE;
		}
		return aiState.getValue().get();
	}
	
	private Value getStateValueInstance(world.State worldState) {
		State aiState = states.get(worldState);
		if (aiState == null) {
			return new Value(DEFAULT_VALUE);
		}
		return aiState.getValue();
	}

	public String getType() {
		return "td.table";
	}
	
	private void learn(float nextStateValue) {
		world.ai.td.table.State currentState = getState(0);
		error = reward + DISCOUNT_RATE * nextStateValue
				- currentState.getValue().get();
		currentState.getEligibilityTrace().set(1);
		for (ListIterator<world.ai.td.table.State> it = pathStates.listIterator(
				pathStates.size()); it.hasPrevious(); ) {
			world.ai.td.table.State state = it.previous();
			if (state.getEligibilityTrace().get() <= 0) {
				break;
			}
			state.getValue().add(STEP_SIZE * error 
					* state.getEligibilityTrace().get());
			state.getEligibilityTrace().set(DISCOUNT_RATE * TRACE_DECAY
					* state.getEligibilityTrace().get());
		}
	}
	
	public void onEpisodeBegin() {
		synchronized (this) {
			for (State state : pathStates) {
				state.getEligibilityTrace().set(0);
			}
			pathStates.clear();
			addWorldState(player.getWorld().getState());
			reward = 0.0f;
		}
	}
	
	public void onEpisodeEnd(boolean goalAchieved) {
		learn(0.0f);
//		if (runs >= 500) {
//			player.getWorld().quit();
//		}
	}
	
	public void readFromFile(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			player.setGamesLost(ois.readLong());
			player.setGamesLostStreak(ois.readLong());
			player.setGamesWon(ois.readLong());
			player.setGamesWonStreak(ois.readLong());
			int statesCount = ois.readInt();
			states = new HashMap<State, State>(statesCount);
			for (int i = 0; i < statesCount; ++i) {
				State state = (State) ois.readObject();
				states.put(state, state);
			}
			ois.close();
			printStats();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFile(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeLong(player.getGamesLost());
			oos.writeLong(player.getGamesLostStreak());
			oos.writeLong(player.getGamesWon());
			oos.writeLong(player.getGamesWonStreak());
			oos.writeInt(states.size());
			for (State state : states.keySet()) {
				oos.writeObject(state);
			}
			oos.close();
			printStats();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printStats() {
		System.out.println(states.size() + " states/values.");
		System.out.println(player.getWL());
	}

	public void reward(world.State worldState, double reward) {
		synchronized (this) {
			this.reward = (float) reward;
		}
	}

}
