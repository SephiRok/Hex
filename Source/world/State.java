package world;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class State implements Serializable {

	private static final long serialVersionUID = 1;
	
	private ArrayList<Short> availableActions;
	private Field[] fields;
	private byte worldHeight;
	private byte worldWidth;
	
	public State(World world) {
		worldHeight = world.getHeight();
		worldWidth = world.getWidth();
		fields = new Field[worldHeight * worldWidth];
		createFields();
		connectFields();
		availableActions = new ArrayList<Short>();
	}
	
	public State(State state) {
		worldHeight = state.worldHeight;
		worldWidth = state.worldWidth;
		fields = new Field[state.getFields().length];
		for (Field field : state.fields) {
			fields[field.getID()] = new Field(field);
		}
	}
	
	public void connectFields() {
		for (int y = 0; y < worldHeight; ++y) {
			for (int x = 0; x < worldWidth; ++x) {
				int fieldID = getFieldID(x, y);
				Field field = fields[fieldID];
				
				// Left.
				int neighborID = fieldID - 1;
				if ((neighborID >= 0) && (fieldID % worldWidth != 0)) {
					field.addNeighbor(fields[neighborID]);
				}

				// Up right.
				neighborID = fieldID - worldWidth + 1;
				if ((neighborID >= 0) && ((fieldID + 1) % worldWidth != 0)) {
					field.addNeighbor(fields[neighborID]);
				}
				
				// Up left.
				--neighborID;
				if (neighborID >= 0) {
					field.addNeighbor(fields[neighborID]);	
				}
				
				// Right.
				neighborID = fieldID + 1;
				if ((neighborID < fields.length) 
						&& ((fieldID + 1) % worldWidth != 0)) {
					field.addNeighbor(fields[neighborID]);
				}
				
				// Down left.
				neighborID = fieldID + worldWidth - 1;
				if ((neighborID < fields.length) 
						&& (fieldID % worldWidth != 0)) {
					field.addNeighbor(fields[neighborID]);
				}
				
				// Down right.
				++neighborID;
				if (neighborID < fields.length) {
					field.addNeighbor(fields[neighborID]);
				}
			}
		}
	}
	
	private void createFields() {
		for (short y = 0; y < worldHeight; ++y) {
			for (short x = 0; x < worldWidth; ++x) {
				short id = (short) (y * worldWidth + x);
				fields[id] = new Field(id);
			}
		}
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof State)) {
			return false;
		}
		State state = (State) object;
		for (int i = 0; i < fields.length; ++i) {
			if (fields[i].getControllerID()
					!= state.fields[i].getControllerID()) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<Short> getAvailableActions() {
		availableActions.clear();
		for (Field field : fields) {
			if (field.getControllerID() == -1) {
				availableActions.add(field.getID());
			}
		}
		return availableActions;
	}
	
	public Field getField(int x, int y) {
		return fields[getFieldID(x, y)];
	}
	
	public int getFieldID(int x, int y) {
		return y * worldWidth + x;
	}
	
	public Field[] getFields() {
		return fields;
	}
	
	public int hashCode() {
		int hash = 7;
		for (int i = 0; i < fields.length; ++i) {
			hash = 37 * hash + (int) fields[i].getControllerID();
		}
		return hash;
	}
	
	public void reset() {
		for (Field field : fields) {
			field.reset();
		}
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}
	
	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		worldHeight = stream.readByte();
		worldWidth = stream.readByte();
		fields = new Field[worldHeight * worldWidth];
		for (short y = 0; y < worldHeight; ++y) {
			for (short x = 0; x < worldWidth; ++x) {
				short id = (short) (y * worldWidth + x);
				fields[id] = (Field) stream.readObject();
				fields[id].setID(id);
			}
		}
	}
	
	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.writeByte(worldHeight);
		stream.writeByte(worldWidth);
		for (Field field : fields) {
			stream.writeObject(field);
		}
	}
	
}
