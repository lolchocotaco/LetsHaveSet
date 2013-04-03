package gui;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import setServer.Message;

public class MainClient {
	
	public static void main(String[] args) {
		
		Socket clientSocket = null;
		
		try {
			clientSocket = new Socket("sable10.ee.cooper.edu", 5342);
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host Exception thrown!");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error connecting to Server!");
			System.exit(-1);
		}
		
		LoginWindow loginWindow = new LoginWindow(clientSocket);
//		LobbyWindow lobbyWindow = new LobbyWindow(clientSocket);
//		TableWindow tableWindow = new TableWindow(clientSocket);
		//AppWindow appWindow = new AppWindow(clientSocket);
		
		loginWindow.frmLogin.setVisible(true);
		
		boolean isListening = true;
		String inLine;
		
		while(isListening) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				inLine = in.readLine();
				String [] splitLine = inLine.split("[;]"); // Message parts split by semicolons
				switch(splitLine[0].charAt(0)) {// Switch on first character in message (command character)
					case 'X': // Login/Register Error: X
						if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
//						loginWindow.loginError();
						break;
					case 'I': // Table Information: I;3;12;Hello;2;4;13;Test;1;2;14;TheBestTable;3;4
						if(splitLine.length < 2) {System.err.println("Message Length Error!"); break;}
						int numTables = Integer.parseInt(splitLine[1]);
						if(splitLine.length != (2 + 4*numTables)) {System.err.println("Message Length Error!"); break;}
//						loginWindow.frmLogin.setVisible(false);
//						lobbyWindow.frmLobby.setVisible(true);
						for(int tableNum = 0; tableNum < numTables; tableNum++)
						{
							int ind = 2 + 4*tableNum;
//							lobbyWindow.addTable(Integer.parseInt(splitLine[ind]), splitLine[ind+1], Integer.parseInt(splitLine[ind+2]), Integer.parseInt(splitLine[ind+3]));
						}
						break;
					case 'U': // Table Update: U;12;Hello;3;4
						if(splitLine.length != 5) {System.err.println("Message Length Error!"); break;}
//						lobbyWindow.updateTable(Integer.parseInt(splitLine[1]), splitLine[2], Integer.parseInt(splitLine[3]), Integer.parseInt(splitLine[4]));
						break;
					case 'F': // Table is Full: F
						if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
//						lobbyWindow.tableIsFull();
						break;
					case 'P': // Players at Table: P;3;4;Nico;Sameer;Vasily
						if(splitLine.length < 4) {System.err.println("Message Length Error!"); break;}
//						if(lobbyWindow.frmLobby.isVisible())
//						{
//							lobbyWindow.frmLobby.setVisible(false);
//							tableWindow.frmTable.setVisible(true);
//						}
//						lobbyWindow.updatePlayers(splitLine);
						break;
					case 'T': // Table Cards:  T;12;01;02;03;04;05;06;07;08;09;10;11;12
						if(splitLine.length < 1) {System.err.println("Message Length Error!"); break;}
//						tableWindow.tableCards(splitLine);
						break;
					case 'D': // Docked Point:  D;Nico
						if(splitLine.length != 2) {System.err.println("Message Length Error!"); break;}
//						tableWindow.dockPoint(splitLine[1]);
						break;
					case 'S': // Set made:  S;Nico;03;21;76
						if(splitLine.length != 5) {System.err.println("Message Length Error!"); break;}
//						tableWindow.setMade(splitLine[1], Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3]), Integer.parseInt(splitLine[4]));
						break;
					case 'N': // New Cards: N;01;02;03
						if(splitLine.length != 4) {System.err.println("Message Length Error!"); break;}
//						tableWindow.newCards(Integer.parseInt(splitLine[1]), Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3]));
						break;
					case 'O': // No Sets!: O
						if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
//						tableWindow.noSets();
						break;
					case 'G': // Game Over!: G
						if(splitLine.length != 1) {System.err.println("Message Length Error!"); break;}
//						tableWindow.gameOver();
//						if(tableWindow.frmLobby.isVisible())
//						{
//							tableWindow.frmLobby.setVisible(false);
//							lobbyWindow.frmTable.setVisible(true);
//						}
						break;
				}
			} catch (IOException e) {
				System.err.println("Problem with Client Input");
			}
		}
		
	}

}
