package world.ai.td.ann;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import world.ai.td.ann.Agent;

public class Neuron {
	
	private double[] eligibilityTraces;
	private Layer inputLayer;
	private Layer layer;
	private Layer outputLayer;
	private double value;
	private double[] weights;
	
	public Neuron(Layer layer) {
		this.layer = layer;
	}
	
	public double computeValue() {
		double weightedSum = 0.0;
		for (int i = 0; i < weights.length; ++i) {
			weightedSum += inputLayer.getNeurons()[i].getValue()
					* weights[i];
		}
		value = value(weightedSum);
		return value;
	}
	
	public double[] getEligibilityTraces() {
		return eligibilityTraces;
	}
	
	public double getValue() {
		return value;
	}
	
	public double[] getWeights() {
		return weights;
	}
	
	public void reset() {
		double r = 1 / Math.sqrt(inputLayer.getNeurons().length);
		for (int i = 0; i < weights.length; ++i) {
			weights[i] = Math.pow(-1.0, Agent.getRNG().nextInt(2)) 
					* Agent.getRNG().nextDouble() * r;
		}		
	}
	
	public void resetEligibilityTraces() {
		for (int i = 0; i < eligibilityTraces.length; ++i) {
			eligibilityTraces[i] = 0;
		}
	}
	
	public void setInputLayer(Layer inputLayer) {
		this.inputLayer = inputLayer;
		eligibilityTraces = new double[inputLayer.getNeurons().length];
		weights = new double[inputLayer.getNeurons().length];
	}
	
	public void setOutputLayer(Layer outputLayer) {
		this.outputLayer = outputLayer;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	// From output to hidden:
	public void updateEligibilityTraces0(double outputDerivative) {
		for (int i = 0; i < eligibilityTraces.length; ++i) {
			eligibilityTraces[i] = Agent.TRACE_DECAY 
					* eligibilityTraces[i] + outputDerivative
					* inputLayer.getNeurons()[i].getValue();
		}
	}
	
	// From hidden to input:
	// outputDerivative is the derivative of the final nn output
	// outputWeight is the weight of the connection to the parent
	public void updateEligibilityTraces1(double outputDerivative,
			double outputWeight) {
		for (int i = 0; i < eligibilityTraces.length; ++i) {
			eligibilityTraces[i] = Agent.TRACE_DECAY 
					* eligibilityTraces[i] + outputDerivative 
					* outputWeight * valueDerivative(value) 
					* inputLayer.getNeurons()[i].getValue();
		}
	}
	
	public void updateWeights(double error) {
		for (int i = 0; i < weights.length; ++i) {
			double weightDelta = layer.getLearningRate() * error 
					* eligibilityTraces[i];
			weightDelta += Agent.MOMENTUM * weights[i];
			weights[i] += weightDelta;
		}
	}
	
	// Activation function.
	static public double value(double x) {
		
		// sigmoid
//		return 1.0 / (1.0 + Math.pow(Math.E, -x));
		
		// tanh
		return Math.tanh(x);
		
	}
	
	// Derivative of activation function.
	static public double valueDerivative(double x) {
		
		// sigmoid
//		return value(x) * (1.0 - value(x));
//		return x * (1.0 - x);
		
		// tanh
		return 1.0 - x * x;
	
	}
	
	public void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		eligibilityTraces = (double[]) stream.readObject();
		weights = (double[]) stream.readObject();
	}
	
	public void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.writeObject(eligibilityTraces);
		stream.writeObject(weights);
	}

}
