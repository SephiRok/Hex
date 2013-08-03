package world.ai.td.table;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class State extends world.State {

	private static final long serialVersionUID = 1;

	private Value eligibilityTrace = new Value(0);
	private Value value = new Value(); // afterstateValue
	
	public State(world.State state) {
		super(state);
	}
	
	public Value getEligibilityTrace() {
		return eligibilityTrace;
	}

	public Value getValue() {
		return value;
	}
	
	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		eligibilityTrace = new Value(0);
		value = (Value) stream.readObject();
	}
	
	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.writeObject(value);
	}

}
