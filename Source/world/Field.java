package world;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Field implements Serializable {

	private static final long serialVersionUID = 1;
	
	private byte controllerID = -1;
	private short id;
	private ArrayList<Field> neighbors;

	public Field(short id) {
		this.id = id;
		neighbors = new ArrayList<Field>();
	}
	
	public Field(Field field) {
		controllerID = field.controllerID;
		id = field.id;
//		neighbors = field.neighbors; // Don't need neighbors in AI state.
	}
	
	public void addNeighbor(Field field) {
		neighbors.add(field);
	}
	
	public int countNeighbors(int controllerID) {
		int count = 0;
		for (Field field : neighbors) {
			if (field.getControllerID() == controllerID) {
				++count;
			}
		}
		return count;
	}
	
	public byte getControllerID() {
		return controllerID;
	}
	
	public short getID() {
		return id;
	}
	
	public ArrayList<Field> getNeighbors() {
		return neighbors;
	}
	
	public boolean isOnEdge0(World world) {
		if (controllerID == -1) {
			return false;
		}
		if (controllerID == 0) {
			if (id < world.getWidth()) {
				return true;
			}
		} else {
			if (id % world.getWidth() == 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isOnEdge1(World world) {
		if (controllerID == -1) {
			return false;
		}
		if (controllerID == 0) {
			if (id >= (world.getHeight() - 1) * world.getWidth()) {
				return true;
			}
		} else {
			if ((id + 1) % world.getWidth() == 0) {
				return true;
			}
		}
		return false;
	}
	
	public void setController(Player player) {
		controllerID = player.getID();
	}
	
	public void setID(short id) {
		this.id = id;
	}
	
	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		controllerID = stream.readByte();
	}
	
	public void reset() {
		controllerID = -1;
	}
	
	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.writeByte(controllerID);
	}
	
}
