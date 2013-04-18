package setServer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	
	public static class User {
		public String username;
		public int numWins;
		public int numLosses;
		public int currentTable;
		
		public User(String username, int numWins, int numLosses, int currentTable) {
			this.username = username;
			this.numWins = numWins;
			this.numLosses = numLosses;
			this.currentTable = currentTable;
		}
	};
	
	private static class Table {
		public String name;
		public int numPlayers;
		public int maxPlayers;
		public Map<Object, Integer> players = null; // userId --> score
		public int numGoPressed;
		public List<Card> deck;
		public List<Card> onTable;
		
		public Table(String name, int numPlayers, int maxPlayers) {
			this.name = name;
			this.numPlayers = numPlayers;
			this.maxPlayers = maxPlayers;
			this.players = new HashMap<Object, Integer>();
			this.numGoPressed = 0;
			this.deck = new ArrayList<Card>();
			this.onTable = new ArrayList<Card>();
		}
		
		public void addPlayer(int userID) {
			numPlayers++;
			players.put(userID, 0);
		}
		
		public void removePlayer(int userID) {
			numPlayers--;
			players.remove(userID);
		}
		
		public String playerString(Map<Object, User> userMap) {
			String out = "P;" + numPlayers + ";" + maxPlayers;
			Iterator<Entry<Object, Integer> > it = players.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
				int userID = (Integer) entry.getKey();
				String username = userMap.get(userID).username;
				int score = (Integer) entry.getValue();
				out += ";" + username + ";" + score;
			}
			return out;
		}
		
		public void resetScores() {
			Iterator<Entry<Object, Integer> > it = players.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
				entry.setValue(0);
			}
		}
		
		public void initializeDeck() {
			for(int i = 0; i<81; i++) {
				deck.add(new Card(i));
			}
			Collections.sort(deck);
			for(int i = 0; i<12; i++) {
				onTable.add(deck.get(0));
				deck.remove(0);
			}
		}
		
		public String tableString() {
			String out = "T;" + onTable.size();
			Iterator<Card> it = onTable.iterator();
			while(it.hasNext()) {
				out += ";" + it.next().cardNum;
			}
			return out;
		}
		
		public boolean setExists(int C1, int C2, int C3) {
			boolean b1 = false;
			boolean b2 = false;
			boolean b3 = false;
			Iterator<Card> it = onTable.iterator();
			while(it.hasNext()) {
				int C = it.next().cardNum;
				if(C == C1) b1 = true;
				if(C == C2) b2 = true;
				if(C == C3) b3 = true;
			}
			return (b1 && b2 && b3);
		}
		
		public int[] removeSet(int C1, int C2, int C3) { // returns {-1} if there is no more cards, {C1, C2, C3} if 3 new cards
			for(int i = 0; i < onTable.size(); i++) {
				int C = onTable.get(i).cardNum;
				if((C == C1)||(C == C2)||(C == C3)) {
					onTable.remove(i);
					i--; // Otherwise it will skip indices
				}
			}
			
			return newCards();
		}
		
		public int[] newCards() {
			if(deck.size() == 0) {
				int[] out = {-1};
				return out;
			} else {
				onTable.add(deck.get(0));
				onTable.add(deck.get(1));
				onTable.add(deck.get(2));
				int[] out = {deck.get(0).cardNum, deck.get(1).cardNum, deck.get(2).cardNum};
				deck.remove(0); deck.remove(0); deck.remove(0);
				return out;
			}
		}
		
		public boolean noMoreSets() {
			for(int i = 0; i < onTable.size(); i++) {
				int i4 = onTable.get(i).cardNum;
				int i1 = i4%3; i4/=3;
				int i2 = i4%3; i4/=3;
				int i3 = i4%3; i4/=3;
				for(int j = i+1; j < onTable.size(); j++) {
					int j4 = onTable.get(j).cardNum;
					int j1 = j4%3; j4/=3;
					int j2 = j4%3; j4/=3;
					int j3 = j4%3; j4/=3;
					for(int k = j+1; k < onTable.size(); k++) {
						int k4 = onTable.get(k).cardNum;
						int k1 = k4%3; k4/=3;
						int k2 = k4%3; k4/=3;
						int k3 = k4%3; k4/=3;
						if((((i1+j1+k1) % 3) == 0) && (((i2+j2+k2) % 3) == 0) && (((i3+j3+k3) % 3) == 0) && (((i4+j4+k4) % 3) == 0)) {
							return false;
						}
						
					}
				}
			}
			
			return true;
		}
		
	}
	
	public static class Card implements Comparable<Card>{
		public double index;
		public int cardNum;
		public Card(int cardNum) {
			this.index = Math.random();
			this.cardNum = cardNum;
		}
		
		@Override
		public int compareTo(Card otherCard) {
			if (this.index == otherCard.index)
		        return 0;
		    else if (this.index > otherCard.index)
		        return 1;
		    else
		        return -1;
		}
	}

	public static void main(String[] args) throws SQLException {
		
		// BlockingQueue receives all client messages, allows reads only when non-empty, and supports threads
		final BlockingQueue<Message> inMessages = new LinkedBlockingQueue<Message>();
		final BlockingQueue<Message> outMessages = new LinkedBlockingQueue<Message>();
		
		// Map from user id (integer) to User object
		Map<Object, User> userMap = new HashMap<Object, User>(); 
		
		// Map from tableNum to Table object
		int numTables = 0;
		Map<Object, Table> tableMap = new HashMap<Object, Table>();
	
		// Runs the client interface server
		MainServer mainServer = new MainServer(inMessages, outMessages);
		mainServer.start(); // thread

		// All of the server mechanics are handled by the MainServer and its subclasses,
		// so that SetServer only has to read from inMessages to get incoming client messages,
		// and write to outMessages to send them. These queues contain Message objects, which
		// simply contain a clientID and a message string.
		
		boolean isRunning = true;
		Connection connection = null;
		Statement stmt = null;
		ResultSet usertable = null;
	 
		
		
		while(isRunning) // Messages are handled in the order they are received. If each message-handling is fast, there shouldn't be a problem.
		{
			try {
				final Message inM = inMessages.take();
				String [] splitM = inM.message.split("[;]"); // Message parts split by semicolons
				switch(splitM[0].charAt(0)) {// Switch on first character in message (command character)
					case 'R': // Register: R;Username;Password
						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
					 
						try {
							connection = DriverManager
							.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
					 
						} catch (SQLException e) {
							System.out.println("Connection Failed!");
							e.printStackTrace();
							return;
						}	
						stmt = connection.createStatement();
						usertable=stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+splitM[1]+"';");
						if (usertable.next()){
							outMessages.put( new Message(inM.clientID, "X;R") );
							System.out.println("User already exists");
						}

						 else {
							 stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('"+splitM[1]+"', '"+splitM[2]+"');");	
							 System.out.println("User created");
							 User newUser = new User(splitM[1], 0, 0, -1);
							 userMap.put(inM.clientID, newUser);
							 outMessages.put( new Message(inM.clientID, allTableString(tableMap)) );
						}
						stmt.close();
						connection.close();
						break;
					case 'L': // Login:  L;Username;Password
						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
						try {
							connection = DriverManager
							.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
					 
						} catch (SQLException e) {
							System.out.println("Connection Failed!");
							e.printStackTrace();
							return;
						}	
						stmt = connection.createStatement();
						boolean loginSuccessful=false;
						usertable = stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+splitM[1]+"';");
						if (!usertable.next()){
							System.out.println("User not found");
						}
						else{
							String passt = null;
							passt = usertable.getString("password");
							//System.out.println("User: " + usert + "    Password: "+ passt);
							if (splitM[2].equals(passt)){
								loginSuccessful=true;
							}
							else{
							}	
						}
						int numWins = 0, numLosses = 0;
						//////////////////////////////////////////
						
						if(loginSuccessful) {
							User newUser = new User(splitM[1], numWins, numLosses, -1);
							userMap.put(inM.clientID, newUser);
							
							outMessages.put( new Message(inM.clientID, allTableString(tableMap)) );
						} else {
							outMessages.put( new Message(inM.clientID, "X;L") );
						}
						stmt.close();
						connection.close();
						break;
					case 'T': // Create Table: T;Name;NumPlayers
						if(splitM.length != 3) {System.err.println("Message Length Error!"); break;}
						
						User userT = userMap.get(inM.clientID);
						if(userT.currentTable >= 0) {
							outMessages.put(new Message(inM.clientID, "A"));
							break;
						}
						
						userT.currentTable = numTables;
						
						Table newTable = new Table(splitM[1], 0, Integer.parseInt(splitM[2]));
						newTable.addPlayer(inM.clientID);
						
						// Give client "Table Made" message
						outMessages.put(new Message(inM.clientID, newTable.playerString(userMap)));
						// Broadcast new table update
						outMessages.put(new Message(-1, "U;" + numTables + ";" + newTable.name + ";" + newTable.numPlayers + ";" + newTable.maxPlayers));
						
						tableMap.put(numTables, newTable);
						numTables++;
						
						break;
					case 'J': // Join Table: J;TableNum
						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}
						
						User userJ = userMap.get(inM.clientID);
						if(userJ.currentTable >= 0) {
							outMessages.put(new Message(inM.clientID, "A"));
							break;
						}
						
						userJ.currentTable = Integer.parseInt(splitM[1]);
						
						Table tableJ = tableMap.get(userJ.currentTable);
						if( tableJ != null ) {
							if(tableJ.numPlayers < tableJ.maxPlayers) {
								tableJ.addPlayer(inM.clientID);
								outMessages.put(new Message(-1, "U;" + userJ.currentTable + ";" + tableJ.name + ";" + tableJ.numPlayers + ";" + tableJ.maxPlayers));
								sendToTable(outMessages, tableJ, tableJ.playerString(userMap));
							} else {
								userJ.currentTable = -1;
								outMessages.put(new Message(inM.clientID, "F")); // Table is full
							}
						} else {
							System.err.println("Table requested that does not exist!");
							userJ.currentTable = -1;
							outMessages.put(new Message(inM.clientID, "F")); // Pretend table is full
						}
						
						break;
					case 'E': // Exit Table: E
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						
						User userE = userMap.get(inM.clientID);
						if(userE.currentTable < 0) { // User exited non-existent table
							System.err.println("User exited non-existent table!");
							outMessages.put(new Message(inM.clientID, "E")); 
							break;
						}
						
						Table tableE = tableMap.get(userE.currentTable);
						outMessages.put(new Message(inM.clientID, "E"));
						
						if( tableE != null ) {
							tableE.removePlayer(inM.clientID);
							outMessages.put(new Message(-1, "U;" + userE.currentTable + ";" + tableE.name + ";" + tableE.numPlayers + ";" + tableE.maxPlayers));
							if(tableE.numPlayers > 0) {
								sendToTable(outMessages, tableE, tableE.playerString(userMap));
							} else {
								tableMap.remove(userE.currentTable);
							}
							
						} else {
							System.err.println("Player exited non-existant table!");
						}
						userE.currentTable = -1;
						
						break;
					case 'D': // Disconnect: D
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						
						mainServer.socketMap.remove(inM.clientID);
						
						User userD = userMap.get(inM.clientID);
						if(userD != null) {
							
							if(userD.currentTable < 0) { // User not at table
								userMap.remove(inM.clientID);
								break;
							}
							
							Table tableD = tableMap.get(userD.currentTable);
							
							if( tableD != null ) {
								tableD.removePlayer(inM.clientID);
								outMessages.put(new Message(-1, "U;" + userD.currentTable + ";" + tableD.name + ";" + tableD.numPlayers + ";" + tableD.maxPlayers));
								if(tableD.numPlayers > 0) {
									sendToTable(outMessages, tableD, tableD.playerString(userMap));
								} else {
									tableMap.remove(userD.currentTable);
								}
							} else {
								System.err.println("Disconnect Table Error!");
							}
							
							userMap.remove(inM.clientID);
						}
						
						break;
					case 'G': // 'Go' (Start game) Signal: G
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						User userG = userMap.get(inM.clientID);
						Table tableG = tableMap.get(userG.currentTable);
						if( tableG != null ) {
							tableG.numGoPressed++;
							if(tableG.numGoPressed == tableG.maxPlayers) { 
								//String cardOrder = "T;12;01;02;03;04;05;06;07;08;09;10;11;12";
								tableG.resetScores();
								sendToTable(outMessages, tableG, tableG.playerString(userMap));
								tableG.initializeDeck();
								sendToTable(outMessages, tableG, tableG.tableString());
								inMessages.put(new Message(inM.clientID, "H")); //Initial set existence check
							}
						}
						break;
					case 'S': // Set made: S;Card1;Card2;Card3
						if(splitM.length != 4) {System.err.println("Message Length Error!"); break;}
						// TODO: Validate set; If invalid, ignore; If valid, award points, broadcast changes (lost/new cards)
						// Also make sure to check for: [No sets possible!] or [Game is over!] 
						System.out.println(splitM[1]+" " +splitM[2]+ " "+splitM[3]);
						User userS = userMap.get(inM.clientID);
						final Table tableS = tableMap.get(userS.currentTable);
						if(tableS.setExists(Integer.parseInt(splitM[1]), Integer.parseInt(splitM[2]), Integer.parseInt(splitM[3]))) {
							final int[] newCards = tableS.removeSet(Integer.parseInt(splitM[1]), Integer.parseInt(splitM[2]), Integer.parseInt(splitM[3]));
							outMessages.put(new Message(inM.clientID, "Y"));
							sendToTable(outMessages, tableS, "S;" + splitM[1] + ";" + splitM[2] + ";" + splitM[3]);
							tableS.players.put(inM.clientID, tableS.players.get(inM.clientID) + 1);
							sendToTable(outMessages, tableS, tableS.playerString(userMap));
							if(newCards[0] >= 0) { // There is new cards
								Thread sendNewCardsThread = new Thread() {
									public void run() {
									    try {
									    	Thread.sleep(2000);
										} catch (InterruptedException e) {
											// Do nothing
										}
									    try {
											inMessages.put(new Message(inM.clientID, "H"));
										} catch (InterruptedException e) {
											System.err.println("Error in Set Checking Thread!");
										}
									    sendToTable(outMessages, tableS, "N;" + newCards[0] + ";" + newCards[1] + ";" + newCards[2]);
									}
								};
								sendNewCardsThread.start();
							} else if(tableS.noMoreSets()) { // GAME OVER!!!
								sendToTable(outMessages, tableS, "G");
								tableS.numGoPressed = 0;
							}
							
						} // else ignore message
						break;
					case 'H': // Check for sets: H
						// THE SERVER SENDS THIS MESSAGE TO ITSELF TO RECURSIVELY CHECK FOR SETS
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						User userH = userMap.get(inM.clientID);
						final Table tableH = tableMap.get(userH.currentTable);
						if(tableH.noMoreSets()) {
							final int[] newCards = tableH.newCards();
							if(newCards[0] >= 0) {
								sendToTable(outMessages, tableH, "O");
								Thread sendNewCardsThread = new Thread() {
									public void run() {
									    try {
									    	Thread.sleep(2000);
										} catch (InterruptedException e) {
											// Do nothing
										}
									    try {
											inMessages.put(new Message(inM.clientID, "H"));
										} catch (InterruptedException e) {
											System.err.println("Error in Set Checking Thread!");
										}
									    sendToTable(outMessages, tableH, "N;" + newCards[0] + ";" + newCards[1] + ";" + newCards[2]);
									}
								};
								sendNewCardsThread.start();
							} else {
								sendToTable(outMessages, tableH, "G"); // GAME OVER!!!
								tableH.numGoPressed = 0;
							}
						}
						break;
					case 'X': // Mistake made: X
						if(splitM.length != 1) {System.err.println("Message Length Error!"); break;}
						User userX = userMap.get(inM.clientID);
						Table tableX = tableMap.get(userX.currentTable);
						tableX.players.put(inM.clientID, tableX.players.get(inM.clientID) - 1);
						sendToTable(outMessages, tableX, tableX.playerString(userMap));
						break;
					case 'C': //Chat sent: C;Message
						if(splitM.length != 2) {System.err.println("Message Length Error!"); break;}					
						User userC = userMap.get(inM.clientID);
						outMessages.put(new Message(-1, "C;" + userC.username + ";" + splitM[1]));
						break;
				}
			} catch (InterruptedException e) {
				// Do nothing?
			}
		}
		
	}
	
	private static void sendToTable(BlockingQueue<Message> outMessages, Table outTable, String outString) {
		Iterator<Entry<Object, Integer> > it = outTable.players.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Integer> entry = (Map.Entry<Object, Integer>) it.next();
			int userID = (Integer) entry.getKey();
			try {
				outMessages.put(new Message(userID, outString));
			} catch (InterruptedException e) {
				System.err.println("Failed to send message to table " + outTable.name + "!");
			}
		}
	}
	
	private static String allTableString(Map<Object, Table> tableMap) {
		String out = "I;" + tableMap.size();
		Iterator<Entry<Object, Table>> it = tableMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Table> entry = (Map.Entry<Object, Table>) it.next();
			int tableNum = (Integer) entry.getKey();
			Table curTable = (Table) entry.getValue();
			out += ";" + tableNum + ";" + curTable.name + ";" + curTable.numPlayers + ";" + curTable.maxPlayers;
		}
		return out;
	}

}
