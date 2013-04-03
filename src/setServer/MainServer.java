package setServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
 * MainServer:
 * 	Handles all client/server I/O
 * 
 * 		Attributes:
 * 			inMessages/outMessages inherited from SetServer
 * 			socketMap-		Maps clientID to Sockets
 * 			numClients-		Manages clientID assignments (New clients are assigned ID# (numClients++) )
 * 			outServer-		Server thread that handles all out messages sequentially
 * 			subServer-		A new SubServer thread is created for each client that becomes a dedicated
 * 						listener for messages from that specific client. In-bound messages are stamped with
 * 						a clientID in a Message object and then passed to the inMessages queue.
 * 
 */

public class MainServer extends Thread {
	
	protected BlockingQueue<Message> inMessages = null;
	protected BlockingQueue<Message> outMessages = null;
	
	protected ConcurrentMap<Object, Socket> socketMap = new ConcurrentHashMap<Object, Socket>();

	public MainServer(BlockingQueue<Message> inMessages, BlockingQueue<Message> outMessages) {
		super("MainServer");
		this.inMessages = inMessages;
		this.outMessages = outMessages;
	}
	
	public void run(){
		
		int numClients = 0; // numClients never goes down (increments on login, does not decrement on logout); This is used for the map
		
		System.out.println("Main Server running...");
		
		OutServer outServer = new OutServer(socketMap, outMessages);
		outServer.start(); // Starts OutServer thread
		
		ServerSocket serverSocket = null;
		
		try {
			serverSocket = new ServerSocket(5342);
		} catch (IOException e) {
			System.err.println("Problem listening on port 5342");
		}
		
		boolean isListening = true;
		
		while(isListening)
		{
			Socket clientSocket;
			try {
				clientSocket = serverSocket.accept();
				SubServer subServer = new SubServer(numClients++, clientSocket, inMessages);
				subServer.start();
			} catch (IOException e) {
				System.err.println("Problem accepting incoming connection");
			}
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Problem closing client connection");
		}

	}

}
