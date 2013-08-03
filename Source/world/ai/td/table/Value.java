package world.ai.td.table;

import java.io.Serializable;

public class Value implements Comparable<Value>, Serializable {
	
	private static final long serialVersionUID = 1;
	
	private float value;
	
	public Value() {}
	
	public Value(float value) {
		this.value = value;
	}
	
	public void add(float value) {
		this.value += value;
	}
	
	public int compareTo(Value that) {
		if (this.value < that.value) {
			return -1;
		}
		if (this.value > that.value) {
			return 1;
		}
		return 0;
	}
	
	public float get() {
		return value;
	}
	
	public void set(float value) {
		this.value = value;
	}
	
}
