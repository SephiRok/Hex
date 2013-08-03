package world.ai.td.ann;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Layer {
	
//	private class ThreadPool {
//		private void run() {
//			final Thread[] threads = new Thread[1];
//			for (int t = 0; t < threads.length; ++t) {
//				final int threadID = t;
//				threads[t] = new Thread(new Runnable() {
//					public void run() {
//						for (int i = threadID; i < neurons.length; 
//								i += threads.length) {
//							neurons[i].computeValue();
//						}
//					}
//				});
//				threads[threadID].start();
//			}
//			for (int t = 0; t < threads.length; ++t) {
//				try {
//					threads[t].join();
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	private Layer inputLayer;
	private double learningRate;
	private Neuron[] neurons;
	private Layer outputLayer;
	
	public Layer(int neurons, int biasNeurons) {
		this.neurons = new Neuron[neurons + biasNeurons];
		for (int i = 0; i < neurons; ++i) {
			this.neurons[i] = new Neuron(this);
		}
		for (int i = neurons; i < neurons + biasNeurons; ++i) {
			this.neurons[i] = new BiasNeuron(this);
		}
	}
	
	public void computeValues() {
		for (Neuron neuron : neurons) {
			neuron.computeValue();
		}
	}
	
	public double getLearningRate() {
		return learningRate;
	}
	
	public Neuron[] getNeurons() {
		return neurons;
	}
	
	public void reset(double v) {
		if (inputLayer == null) {
			return;
		}
		v = v / inputLayer.getNeurons().length;
		// From: http://www.willamette.edu/~gorr/classes/cs449/precond.html
		learningRate = Agent.LEARNING_RATE 
				/ (inputLayer.getNeurons().length * Math.sqrt(v));
//		learningRate = Agent.LEARNING_RATE 
//				/ inputLayer.getNeurons().length;
		for (Neuron neuron : neurons) {
			neuron.reset();
		}
		inputLayer.reset(v);
	}
	
	public void resetEligibilityTraces() {
		for (Neuron neuron : neurons) {
			neuron.resetEligibilityTraces();
		}
	}
	
	private void setInput(Layer inputLayer) {
		this.inputLayer = inputLayer;
		for (Neuron neuron : neurons) {
			neuron.setInputLayer(inputLayer);
		}
	}
	
	public void setOutput(Layer outputLayer) {
		this.outputLayer = outputLayer;
		for (Neuron neuron : neurons) {
			neuron.setOutputLayer(outputLayer);
		}
		outputLayer.setInput(this);
	}
	
	public void updateEligibilityTraces0(double outputDerivative) {
		for (Neuron neuron : neurons) {
			neuron.updateEligibilityTraces0(outputDerivative);
		}
	}
	
	public void updateEligibilityTraces1(double outputDerivative) {
		for (int i = 0; i < neurons.length; ++i) {
			neurons[i].updateEligibilityTraces1(outputDerivative,
					outputLayer.getNeurons()[0].getWeights()[i]);
		}
	}
	
	public void updateLearningRate(double runs) {
		learningRate = (Agent.LEARNING_RATE 
				/ inputLayer.getNeurons().length) / (1 + runs / 100000);
//		System.out.println("learningRate: " + learningRate);
	}
	
	public void updateWeights(double error) {
		for (Neuron neuron : neurons) {
			neuron.updateWeights(error);
		}
	}
	
	public void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		for (Neuron neuron : neurons) {
			neuron.readObject(stream);
		}
	}
	
	public void writeObject(ObjectOutputStream stream)
			throws IOException {
		for (Neuron neuron : neurons) {
			neuron.writeObject(stream);
		}
	}

}
