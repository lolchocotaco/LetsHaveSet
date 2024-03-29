package setServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class SubServer extends Thread {
	
	private int clientID;
	private Socket socket = null;
	private BlockingQueue<Message> inMessages = null;
	
	public SubServer(int clientID, Socket socket, BlockingQueue<Message> inMessages) {
		
		super("SubServer");
		this.clientID = clientID;
		this.socket = socket;
		this.inMessages = inMessages;
	}
	
	public void run() {
		String inLine;
		
		System.out.println("New client connected, ID: " + clientID);
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			while(true) {
				try {
					inLine = in.readLine();
					if(inLine != null)
					{
						Message inM = new Message(clientID, inLine);
						try {
							inMessages.put(inM);
						} catch (InterruptedException e) {
							// Never gets here (no interrupts?)
						}
					} else {
						System.out.println("Client disconnected, ID: " + clientID);
						try {
							inMessages.put(new Message(clientID, "D"));
						} catch (InterruptedException e) {
							System.err.println("Disconnect Error!");
						}
						return;
					}
				} catch (IOException e) {
//					System.err.println("Problem with SubServer Input");
					System.out.println("Client disconnected, ID: " + clientID);
					try {
						inMessages.put(new Message(clientID, "D"));
					} catch (InterruptedException e1) {
						System.err.println("Disconnect Error!");
					}
					return; // Stop Thread on IOException
				}
			}
		} catch (IOException e1) {
			System.err.println("Problem with client connection!");
		}
		
	}

}
