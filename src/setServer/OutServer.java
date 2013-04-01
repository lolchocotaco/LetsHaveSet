package setServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class OutServer extends Thread {

	private ConcurrentMap<Object, Socket> socketMap = null;
	private BlockingQueue<Message> outMessages = null;
	
	public OutServer(ConcurrentMap<Object, Socket> socketMap, BlockingQueue<Message> inMessages) {
		super("SubServer");
		this.socketMap = socketMap;
		this.outMessages = inMessages;
	}
	
	public void run() {
		
		while(true) // Always looking for outgoing messages
		{
			try {
				Message outM = outMessages.take();
				if(outM.clientID == -1) { // Broadcast
					
					Iterator<Entry<Object, Socket>> it = socketMap.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<Object, Socket> entry = (Map.Entry<Object, Socket>) it.next();
						Socket outSocket = (Socket) entry.getValue();
						try {
							DataOutputStream out = new DataOutputStream(outSocket.getOutputStream());
							out.writeBytes(outM.message + '\n');
						} catch (IOException e) {
							System.err.println("Problem with Server Output");
						}
					}
				} else { // Single client
					
					Socket outSocket = socketMap.get(outM.clientID);
					try {
						DataOutputStream out = new DataOutputStream(outSocket.getOutputStream());
						out.writeBytes(outM.message + '\n');
					} catch (IOException e) {
						System.err.println("Problem with Server Output");
					}
				}
			} catch (InterruptedException e) {
				// Do nothing?
			}
		}
	}

}
