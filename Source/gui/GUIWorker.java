package gui;

import java.util.List;

import javax.swing.SwingWorker;

import core.Message;
import core.MessageReceiver;
import core.Messenger;

import world.World;

public class GUIWorker extends SwingWorker<Void, Message> 
		implements MessageReceiver {

	private Messenger messenger = new Messenger();
	private World world = new World();
	private Thread worldThread = new Thread(world);

	@Override protected Void doInBackground() {
		world.getMessenger().addReceiver(this);
		worldThread.start();
		try {
			worldThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Messenger getMessenger() {
		return messenger;
	}
	
	public World getWorld() {
		return world;
	}
	
	public void join() throws InterruptedException {
		worldThread.join();
	}
	
	public void onMessage(Message message) {
		publish(message);
	}
	
	protected void process(List<Message> messages) {
		for (Message message : messages) {
			messenger.sendMessage(message);
		}
	}
	
	public void quit() {
		world.quit();
	}
	
}
