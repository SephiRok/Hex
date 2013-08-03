package world.ai.td.table;
//package World.AI;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
import java.io.Serializable;
//import java.util.Arrays;
//
public class LinedState implements Serializable {
//
//	private static final long serialVersionUID = 1;
//
//	private byte[] columns0 = new byte[World.World.WIDTH];
//	private byte[] columns1 = new byte[World.World.WIDTH];
//	private Value eligibilityTrace = new Value(0);
//	private byte[] rows0 = new byte[World.World.HEIGHT];
//	private byte[] rows1 = new byte[World.World.HEIGHT];
//	private Value value = new Value();
//	
//	public LinedState(World.State state) {
//		for (World.Field field : state.getFields()) {
//			rows0[(int) Math.floor(field.getID() / World.World.WIDTH)] += booleanToByte(field.getControllerID() == 0);
//			rows1[(int) Math.floor(field.getID() / World.World.WIDTH)] += booleanToByte(field.getControllerID() == 1);
//			columns0[field.getID() % World.World.WIDTH] += booleanToByte(field.getControllerID() == 0);
//			columns1[field.getID() % World.World.WIDTH] += booleanToByte(field.getControllerID() == 1);
//		}
//	}
//	
//	public boolean equals(Object object) {
//		if (this == object) {
//			return true;
//		}
//		if (!(object instanceof LinedState)) {
//			return false;
//		}
//		LinedState state = (LinedState) object;
//		return Arrays.equals(columns0, state.columns0)
//				&& Arrays.equals(columns1, state.columns1)
//				&& Arrays.equals(rows0, state.rows0)
//				&& Arrays.equals(rows1, state.rows1);
//	}
//	
//	public Value getEligibilityTrace() {
//		return eligibilityTrace;
//	}
//	
//	public byte booleanToByte(boolean b) {
//		return (byte) (b ? 1 : 0);
//	}
//
//	public Value getValue() {
//		return value;
//	}
//	
//	public int hashCode() {
//		int hash = 7;
//		for (int i = 0; i < columns0.length; ++i) {
//			hash = 37 * hash + (int) columns0[i];
//		}
//		for (int i = 0; i < columns1.length; ++i) {
//			hash = 37 * hash + (int) columns1[i];
//		}
//		for (int i = 0; i < rows0.length; ++i) {
//			hash = 37 * hash + (int) rows0[i];
//		}
//		for (int i = 0; i < rows1.length; ++i) {
//			hash = 37 * hash + (int) rows1[i];
//		}
//		return hash;
//	}
//	
//	private void readObject(ObjectInputStream stream)
//			throws IOException, ClassNotFoundException {
//		columns0 = (byte[]) stream.readObject();
//		columns1 = (byte[]) stream.readObject();
//		rows0 = (byte[]) stream.readObject();
//		rows1 = (byte[]) stream.readObject();
//		eligibilityTrace = new Value(0);
//		value = (Value) stream.readObject();
//	}
//	
//	private void writeObject(ObjectOutputStream stream)
//			throws IOException {
//		stream.writeObject(columns0);
//		stream.writeObject(columns1);
//		stream.writeObject(rows0);
//		stream.writeObject(rows1);
//		stream.writeObject(value);
//	}
//
}
