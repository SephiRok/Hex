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
	
	/**
	 * From 0.00 (random) to 1.00 (greedy).
	 */
	private final static float GREEDINESS = 1.0f; // May not discover shortest path with -1.0 field reward without at least 1% randomness?
	
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
	private Random rng = new Random();
	private HashMap<State, State> states = new HashMap<State, State>();
	
	public Agent(AIPlayer player) {
		this.player = player;
	}
	
	private void addWorldState(world.State worldState) {
		State state = states.get(worldState);
		if (state != null) {
//			System.out.println("Found past experience for current state.");
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
//				System.out.println(getStateValueInstance(nextState).get());
			}
			if (rng.nextInt(100) >= GREEDINESS * 100) {
				short action = availableActions.get(rng.nextInt(
						availableActions.size()));
//				System.out.println("Picked action: " + action);
				return action;
			}
			ArrayList<Short> highestValueActions = actionValues.get(
					actionValues.lastKey());
//			System.out.println(highestValueActions.size());

			// Less losses until learned with .get(0) for 3x3.
			short action = highestValueActions.get(rng.nextInt(
					highestValueActions.size()));
//			short action = highestValueActions.get(0);

//			System.out.println("Picked action: " + action);
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
	
	public double getStateValue() {
		synchronized (this) {
			world.ai.td.table.State state = getState(0);
			if (state == null) {
				return DEFAULT_VALUE;
			}
			return state.getValue().get();
		}
	}
	
	public double getStateValue(world.State worldState) {
		world.ai.td.table.State aiState = states.get(worldState);
		if (aiState == null) {
			return DEFAULT_VALUE;
		}
		return aiState.getValue().get();
	}
	
	private Value getStateValueInstance(world.State worldState) {
		world.ai.td.table.State aiState = states.get(worldState);
		if (aiState == null) {
			return new Value(DEFAULT_VALUE);
		}
		return aiState.getValue();
	}

	public String getType() {
		return "td.table";
	}
	
	public void onEpisodeBegin() {
		synchronized (this) {
			for (world.ai.td.table.State state : pathStates) {
				state.getEligibilityTrace().set(0);
			}
			pathStates.clear();
			addWorldState(player.getWorld().getState());
		}
	}
	
	public void onEpisodeEnd(boolean goalAchieved) {
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
			addWorldState(worldState);
			world.ai.td.table.State currentState = getState(0);
			world.ai.td.table.State previousState = getState(1);
			error = (float) reward 
					+ DISCOUNT_RATE * currentState.getValue().get() 
					- previousState.getValue().get();
			currentState.getEligibilityTrace().set(1);
			for (ListIterator<world.ai.td.table.State> it = pathStates.listIterator(
					pathStates.size() - 1); it.hasPrevious(); ) {
				// XXXXX??? Why do we set the value of the previous state and not the current one?
				// We don't know if the end state is good like this .........
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
	}

}
