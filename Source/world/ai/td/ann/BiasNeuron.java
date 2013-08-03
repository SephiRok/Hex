package world.ai.td.ann;

public class BiasNeuron extends Neuron {

	private static final double BIAS = 1.0;
	
	public BiasNeuron(Layer layer) {
		super(layer);
	}
	
	public double computeValue() {
		return BIAS;
	}
	
	public double getValue() {
		return BIAS;
	}

}
