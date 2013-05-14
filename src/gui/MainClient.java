package gui;



import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainClient {
	
	static Socket clientSocket = null;
	static DataOutputStream out = null;


	
	public static void main(String[] args) {
		
		try {
//			clientSocket = new Socket("sable10.ee.cooper.edu", 5342);
			// TODO : CHANGE BACK TO NORMAL ADDRESS
			clientSocket = new Socket("127.0.0.1",5342);
			out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host Exception thrown!");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error connecting to Server!");
			System.exit(-1);
		}
		
		MP3 gameMusic = null;
		
		LoginWindow loginWindow = new LoginWindow();
		LobbyWindow lobbyWindow = new LobbyWindow();
		TableWindow tableWindow = new TableWindow();
		//boolean isAdmin  = false;

		//AppWindow appWindow = new AppWindow(clientSocket);
		
		loginWindow.frmLogin.setVisible(true);
		
		boolean isListening = true;
		String inLine;
		
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
			while(isListening) {
				try {
					inLine = in.readLine();
					String [] splitLine = inLine.split("[;]"); // Message parts split by semicolons
					switch(splitLine[0].charAt(0)) {// Switch on first character in message (command character)
						case 'X': // Login/Register Error: X;L or X;R
							if(splitLine.length != 2) {System.err.println("Message Length Error!"); break;}
							loginWindow.loginError(splitLine[1]);
							break;
						case 'I': // Table Information: I;false;3;12;Hello;2;4;Open;13;Test;1;2;Open;14;TheBestTable;3;4;Playing;2;Vasily;N/A;Sameer;22
							if(splitLine.length < 2) {System.err.println("Message Length Error!"); break;}
							int numTables = Integer.parseInt(splitLine[2]);
							int numUsers = Integer.parseInt(splitLine[3+5*numTables]);
							if(splitLine.length != (4 + 5*numTables + 2*numUsers)) {System.err.println("Message Length Error!"); break;}
							//System.out.println(splitLine[1]);
							if(splitLine[1].equals("true")){
								TableWindow.isAdmin=true;
							}
							loginWindow.frmLogin.setVisible(false);
							lobbyWindow.frmLobby.setVisible(true);
							for(int tableNum = 0; tableNum < numTables; tableNum++) {
								int ind = 3 + 5*tableNum;
								lobbyWindow.addTable(splitLine[ind], splitLine[ind+1], splitLine[ind+2], splitLine[ind+3], splitLine[ind+4]);
							}
							
							for(int userNum = 0; userNum < numUsers; userNum++) {
								int ind = 4 + 5*numTables + 2*userNum;
								lobbyWindow.updateUser(splitLine[ind], splitLine[ind+1]);
							}
							
							gameMusic = new MP3(2);
							gameMusic.loopPlay();
						    
							break;
						case 'U': // Table/User Update: U;12;Hello;3;4
							if(splitLine.length == 6) {
								lobbyWindow.updateTable(splitLine[1], splitLine[2], splitLine[3], splitLine[4], splitLine[5]);
							} else if(splitLine.length == 3) {
								lobbyWindow.updateUser(splitLine[1], splitLine[2]);
							} else {
								System.err.println("Message Length Error!");
							}
							break;
						case 'F': // Table is Full: F
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							lobbyWindow.tableIsFull();
							break;
						case 'A': // Already in a table: A
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							lobbyWindow.alreadyAtTable();
							break;
						case 'H': // Table is playing game: H
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							lobbyWindow.alreadyPlaying();
							break;
						case 'E': // Exited table: E
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							if(tableWindow.frmTable.isVisible())
							{
								tableWindow.hideTable();
								lobbyWindow.frmLobby.setVisible(true);
								
								gameMusic.close();
								gameMusic = new MP3(2);
								gameMusic.loopPlay();
							}
							break;
						case 'P': // Players at Table: P;3;4;Nico;0;Sameer;3;Vasily;12
							if(splitLine.length < 4) {System.err.println("Message Length Error!"); break;}
							int numPlayers = Integer.parseInt(splitLine[1]);
							if(splitLine.length != 3+2*numPlayers) {System.err.println("Message Length Error!"); break;}
							if(lobbyWindow.frmLobby.isVisible())
							{
								lobbyWindow.frmLobby.setVisible(false);
								tableWindow.showTable();
								
								gameMusic.close();
								gameMusic = new MP3(3);
								gameMusic.loopPlay();						
							}
							tableWindow.updatePlayers(splitLine);
							break;
						case 'R': // Table Reset: R
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							tableWindow.tableReset();
							break;
						case 'T': // Table Cards:  T;12;01;02;03;04;05;06;07;08;09;10;11;12
							if(splitLine.length < 1) {System.err.println("Message Length Error!"); break;}
							tableWindow.tableCards(splitLine);
							
							gameMusic.close();
							gameMusic = new MP3(4);
							gameMusic.loopPlay();
							
							break;
						case 'S': // Set made:  S;03;21;76
							if(splitLine.length != 4) {System.err.println("Message Length Error!"); break;}
							tableWindow.setMade(Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3]));
							break;
						case 'D': // Docked point!: D
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							tableWindow.youScrewedUp();
							break;
						case 'Y': // You made a set!: Y
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							tableWindow.youMadeASet();
							break;
						case 'N': // New Cards: N;01;02;03
							if(splitLine.length != 4) {System.err.println("Message Length Error!"); break;}
							tableWindow.newCards(Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3]));
							break;
						case 'O': // No Sets!: O
							if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
							tableWindow.noSets();
							break;
						case 'G': // Game Over!: G;1;1000;1010
							if(splitLine.length != 4) {System.err.println("Message Length Error!"); break;}
							tableWindow.gameOver(Integer.parseInt(splitLine[1])==1, splitLine[2], splitLine[3]);							
							break;
						case 'C': // Lobby Chat: C;Username;Message
							if(splitLine.length != 3) {System.err.println("Message Length Error!"); break;}
							lobbyWindow.newChat(splitLine[1], splitLine[2]);
							break;
						case 'Q': // Table Chat: Q;Username;Message
							if(splitLine.length != 3) {System.err.println("Message Length Error!"); break;}
							tableWindow.newChat(splitLine[1], splitLine[2]);
							break;
					}
				} catch (IOException e) {
					System.err.println("Server disconnected!");
					System.exit(-1);
				}
			}
		} catch (IOException e1) {
			System.err.println("Could not read from server!");
			System.exit(-1);
		}
		
		
	}
	
	public static void sendMessage(String message) {
		try {
			out.writeBytes(message + '\n');
		} catch (IOException e) {
			System.err.println("Error sending message!");
		}
	}
	

}
