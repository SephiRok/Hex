package core;

import java.util.ArrayList;

public class Messenger {
	
	private ArrayList<MessageReceiver> receivers 
			= new ArrayList<MessageReceiver>();
	
	public void addReceiver(MessageReceiver receiver) {
		receivers.add(receiver);
	}
	
	public void sendMessage(Message message) {
		message.setSender(this);
		for (MessageReceiver receiver : receivers) {
			receiver.onMessage(message);
		}
	}
	
}
