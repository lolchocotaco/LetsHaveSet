package setServer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * SetServer: 
 * 	Executable that manages all set operations and server/client I/O
 * 
 * 		Attributes:
 * 			inMessages-		Input message queue
 * 			outMessages-	Output message queue
 * 			(userMap-		Maps clientID to User object)
 * 
 * 		Runs:
 * 			mainServer thread (handles all client I/O)
 * 			Game Management Loop
 * 				- Read and respond to client messages
 * 				- Check table statuses
 * 
 */

public class SetServer {

	public static void main(String[] args) {
		
		// BlockingQueue receives all client messages, allows reads only when non-empty, and supports threads
		BlockingQueue<Message> inMessages = new LinkedBlockingQueue<Message>();
		BlockingQueue<Message> outMessages = new LinkedBlockingQueue<Message>();
		
		// Map from user id (integer) to User object
		Map<Object, User> userMap = new HashMap<Object, User>(); 
	
		// Runs the client interface server
		MainServer mainServer = new MainServer(inMessages, outMessages);
		mainServer.start(); // thread
		
		// TODO: Lots of junk here
		// All of the server mechanics are handled by the MainServer and its subclasses,
		// so that SetServer only has to read from inMessages to get incoming client messages,
		// and write to outMessages to send them. These queues contain Message objects, which
		// simply contain a clientID and a message string.
		
	}
	
	private class User {
		public String username;
		public int numWins;
		public int numLosses;
		public int currentTable;
	};

}
