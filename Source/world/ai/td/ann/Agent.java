package world.ai.td.ann;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import world.ai.AIPlayer;

public class Agent implements world.ai.Agent {
	
	public final static double DISCOUNTING_RATE = 1.0;
	public final static double GREEDINESS = 0.90;
	public final static double LEARNING_RATE = 0.001;
	public final static double MOMENTUM = 0.0;

	// has to be 0 or decrease towards 0 to converge to local optimum ... how can it get to optimal if always 0???
	// only for linear function approximation or any func approx???
	public final static double TRACE_DECAY = 0.0;
	
	public final static double WEIGHT_DECAY = 0.5;
	
	/* interesting values:
	 * size: learning rate, hidden layer size, trace decay
	 * 3x3: 0.5, 32, 0.6
	 * 3x3: 0.005, 24, 0.25
	 * 3x3: 0.005, 2, 0.25
	 */
	
	private static Random rng = new Random();

	private TreeMap<Double, ArrayList<Short>> actionValues 
			= new TreeMap<Double, ArrayList<Short>>();
	private double error;
	private double greediness;
	private Layer hiddenLayer;
	private Layer inputLayer;
	private double output; // value
	private Layer outputLayer = new Layer(1, 0);
	private AIPlayer player;
	private double previousOutput;
	private double reward;
	
	public Agent(AIPlayer player) {
		this.player = player;
		inputLayer = new Layer((player.getWorld().getWidth() 
				* player.getWorld().getHeight()) * 2, 1);
		hiddenLayer = new Layer((int) ((inputLayer.getNeurons().length - 1) 
				* 1.5), 1);
		inputLayer.setOutput(hiddenLayer);
		hiddenLayer.setOutput(outputLayer);
		reset();
	}
	
	public int chooseAction(world.State worldState) {
		
		// Evaluate available actions.
		actionValues.clear();
		ArrayList<Short> availableActions = worldState.getAvailableActions();
		for (short action : availableActions) {
			worldState.getFields()[action].setController(player);
			double value = evaluate(worldState);
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
		if (rng.nextInt(100) >= greediness * 100) {
//			System.out.println("Non-greedy move!");
			action = availableActions.get(rng.nextInt(
					availableActions.size()));
		} else {
			ArrayList<Short> highestValueActions = actionValues.get(
					actionValues.lastKey());
			
			// Faster convergence with .get(0) for 3x3.
			action = highestValueActions.get(rng.nextInt(
					highestValueActions.size()));
//			action = highestValueActions.get(0);
			
		}
		
		// Learn.
		worldState.getFields()[action].setController(player);
		previousOutput = output;
		output = evaluate(worldState); // forward pass -- compute activities
		learn(output);
		output = evaluate(worldState); // forward pass must be done twice to form TD errors
		updateEligibilityTraces();
		worldState.getFields()[action].reset();
		
		return action;
	}
	
	private double computeOutput() {
		hiddenLayer.computeValues();
		outputLayer.computeValues();
		return getOutputNeuron().getValue();
	}
	
	// Forward propagation.
	private double evaluate(world.State worldState) {
		setInputs(worldState);
		return computeOutput();
	}
	
	public double getError() {
		return error;
	}
	
	public static Random getRNG() {
		return rng;
	}
	
	private Neuron getOutputNeuron() {
		return outputLayer.getNeurons()[0];
	}
	
	public double getStateValue(world.State worldState) {
		return output;
	}
	
	public String getType() {
		return "td.ann";
	}
	
	public void learn(double output) {
		error = reward + DISCOUNTING_RATE * output - previousOutput; // form error
//		updateEligibilityTraces();
		updateWeights(); // backward pass (backward propagation) -- learn
	}
	
	public void onEpisodeBegin() {
		outputLayer.resetEligibilityTraces();
		hiddenLayer.resetEligibilityTraces();
		output = 0.0;
		reward = 0.0;
//		greediness = 1.0 - 1.0 / (player.getGamesPlayed() + 1.0);
		outputLayer.updateLearningRate(1.0, player.getGamesPlayed());
//		output = evaluate(player.getWorld().getState());
//		updateEligibilityTraces();
	}
	
	public void onEpisodeEnd(boolean goalAchieved) {
		learn(0.0);		
		
//		if (!goalAchieved) {
//			if (player.getID() == 1 && player.getGamesLostStreak() >= 10
//					|| player.getGamesLostStreak() >= 100000) {
//				lossStreak = 0;
//				runs = 0;
//				wins = 0;
//				reset();
//				System.out.println("RESET!");
//			}
//		}
		
//		if (player.getGamesPlayed() == 5000) {
//			printStats();
//			if (runs - wins <= 20) {
//				player.getWorld().quit();
//			} else {
//				runs = 0;
//				wins = 0;
//				winStreak = 0;
//				lossStreak = 0;
//				reset();
//			}
//		}
	}
	
	public void printStats() {
		System.out.println(outputLayer.getNeurons().length
				+ " output neurons, " + hiddenLayer.getNeurons().length 
				+ " hidden neurons, " + inputLayer.getNeurons().length 
				+ " input neurons.");
		System.out.println(player.getWL());
	}
	
	public void readFromFile(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			player.setGamesLost(ois.readLong());
			player.setGamesLostStreak(ois.readLong());
			player.setGamesWon(ois.readLong());
			player.setGamesWonStreak(ois.readLong());
			outputLayer.readObject(ois);
			hiddenLayer.readObject(ois);
			ois.close();
			printStats();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void reset() {
		greediness = GREEDINESS;
		outputLayer.reset();
	}
	
	public void reward(world.State worldState, double reward) {
		this.reward = reward;
	}
	
	private void updateEligibilityTraces() {
		double outputDerivative = Neuron.transferDerivative(output);
		outputLayer.updateEligibilityTraces0(outputDerivative);
		hiddenLayer.updateEligibilityTraces1(outputDerivative);
	}
	
	private void updateWeights() {
		outputLayer.updateWeights(error);
		hiddenLayer.updateWeights(error);
	}
	
	private void setInputs(world.State state) {
		/*
		 * Raw boolean input mapping:
		 * WIDTH * HEIGHT * 3 inputs.
		 * Set 3 inputs based on the controller of a field.
		 * 100 == no controller
		 * 010 == controller 0
		 * 001 == controller 1
		 */
//		for (world.Field field : state.getFields()) {
//			inputLayer.getNeurons()[field.getID() * 3 + 0].setValue(
//					field.getControllerID() == -1 ? 1 : 0);
//			inputLayer.getNeurons()[field.getID() * 3 + 1].setValue(
//					field.getControllerID() == 0 ? 1 : 0);
//			inputLayer.getNeurons()[field.getID() * 3 + 2].setValue(
//					field.getControllerID() == 1 ? 1 : 0);
//		}

		/*
		 * Reduced boolean input mapping:
		 * WIDTH * HEIGHT * 2 inputs.
		 * Set 2 inputs based on the controller of a field.
		 * 00 == no controller
		 * 10 == controller 0
		 * 01 == controller 1
		 * 11 is not possible
		 */
		for (world.Field field : state.getFields()) {
			inputLayer.getNeurons()[field.getID() * 2 + 0].setValue(
					field.getControllerID() == 0 ? 1 : 0);
			inputLayer.getNeurons()[field.getID() * 2 + 1].setValue(
					field.getControllerID() == 1 ? 1 : 0);
		}
		
		/*
		 * Line input mapping:
		 * (WIDTH + HEIGHT) * 2 inputs.
		 * Set input 0 to the number of rows with 1 field controlled by 0 and none by 1
		 * Set input 1 to the number of rows with 2 fields controlled by 0 and none by 1
		 * ...
		 * Set input HEIGHT-1 to the number of rows with WIDTH fields controlled by 0 and none by 1
		 * Repeat for controller 1 and then columns.
		 */
//		for (Neuron neuron : inputLayer.getNeurons()) {
//			neuron.setValue(0.0);
//		}
//		int[] controller0FieldsInRow = new int[World.HEIGHT];
//		int[] controller1FieldsInRow = new int[World.HEIGHT];
//		int[] controller0FieldsInColumn = new int[World.WIDTH];
//		int[] controller1FieldsInColumn = new int[World.WIDTH];
//		for (world.Field field : state.getFields()) {
//			if (field.getControllerID() == 0) {
//				++controller0FieldsInRow[field.getID() / World.WIDTH];
//				++controller0FieldsInColumn[field.getID() % World.WIDTH];
//			} else if (field.getControllerID() == 1) {
//				++controller1FieldsInRow[field.getID() / World.WIDTH];
//				++controller1FieldsInColumn[field.getID() % World.WIDTH];	
//			}
//		}
//		Neuron[] inputNeurons = inputLayer.getNeurons();
//		for (int i = 0; i < controller0FieldsInRow.length; ++i) {
//			int fields = controller0FieldsInRow[i];
//			if (fields > 0 && controller1FieldsInRow[i] == 0) {
//				Neuron inputNeuron = inputNeurons[fields];
//				inputNeuron.setValue(inputNeuron.getValue() + 1);
//			}
//		}
//		int offset = World.WIDTH;
//		for (int i = 0; i < controller1FieldsInRow.length; ++i) {
//			int fields = controller1FieldsInRow[i];
//			if (fields > 0 && controller0FieldsInRow[i] == 0) {
//				Neuron inputNeuron = inputNeurons[offset + fields];
//				inputNeuron.setValue(inputNeuron.getValue() + 1);
//			}
//		}
//		offset += World.WIDTH;
//		for (int i = 0; i < controller0FieldsInColumn.length; ++i) {
//			int fields = controller0FieldsInColumn[i];
//			if (fields > 0 && controller1FieldsInColumn[i] == 0) {
//				Neuron inputNeuron = inputNeurons[offset + fields];
//				inputNeuron.setValue(inputNeuron.getValue() + 1);
//			}
//		}
//		offset += World.HEIGHT;
//		for (int i = 0; i < controller1FieldsInColumn.length; ++i) {
//			int fields = controller1FieldsInColumn[i];
//			if (fields > 0 && controller0FieldsInColumn[i] == 0) {
//				Neuron inputNeuron = inputNeurons[offset + fields];
//				inputNeuron.setValue(inputNeuron.getValue() + 1);
//			}
//		}
	}
	
	public void writeToFile(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeLong(player.getGamesLost());
			oos.writeLong(player.getGamesLostStreak());
			oos.writeLong(player.getGamesWon());
			oos.writeLong(player.getGamesWonStreak());
			outputLayer.writeObject(oos);
			hiddenLayer.writeObject(oos);
			oos.close();
			printStats();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
