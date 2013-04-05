package setServer;

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
//		Map<Object, User> userMap = new HashMap<Object, User>(); 
	
		// Runs the client interface server
		MainServer mainServer = new MainServer(inMessages, outMessages);
		mainServer.start(); // thread

		// All of the server mechanics are handled by the MainServer and its subclasses,
		// so that SetServer only has to read from inMessages to get incoming client messages,
		// and write to outMessages to send them. These queues contain Message objects, which
		// simply contain a clientID and a message string.
		
		boolean isRunning = true;
		
		while(isRunning) // Messages are handled in the order they are received. If each message-handling is fast, there shouldn't be a problem.
		{
			try {
				Message inM = inMessages.take();
				String [] splitM = inM.message.split("[;]"); // Message parts split by semicolons
				switch(splitM[0].charAt(0)) {// Switch on first character in message (command character)
					case 'R': // Register: R;Username;Password
						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
						// TODO: Add login information to MySQL
						
						//////////////////////////////////////////
						// DEBUG
						Message out1 = new Message(inM.clientID, "X");
						outMessages.put(out1);
						//////////////////////////////////////////
						
						break;
					case 'L': // Login:  L;Username;Password
						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
						// TODO: Check MySQL for login information
						
						//////////////////////////////////////////
						// DEBUG
						Message out2 = new Message(inM.clientID, "I;0");
						outMessages.put(out2);
						//////////////////////////////////////////
						
						break;
					case 'T': // Create Table: T;NumPlayers
						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}
						// TODO: Create Table with maximum of "NumPlayers"; add user to table
						break;
					case 'J': // Join Table: J;TableNum
						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}
						// TODO: If table has room, add user to table; If table becomes full, allow "Start"
						break;
					case 'E': // Exit Table: E
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						// TODO: Take player out of table
						break;
					case 'G': // 'Go' (Start game) Signal: G
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						// TODO: If all players have selected start game, initiate gameplay; Send card info's
						break;
					case 'S': // Set made: S;Card1;Card2;Card3
						if(splitM.length != 4) {System.err.println("Message Length Error!"); break;}
						// TODO: Validate set; If invalid, ignore; If valid, award points, broadcast changes (lost/new cards)
						// Also make sure to check for: [No sets possible!] or [Game is over!] 
						break;
					case 'X': // Mistake made: X
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						// TODO: Dock a point from user; Broadcast docked point
						break;
				}
			} catch (InterruptedException e) {
				// Do nothing?
			}
		}
		
	}
	
	/*
	private class User {
		public String username;
		public int numWins;
		public int numLosses;
		public int currentTable;
	};
	*/

}
