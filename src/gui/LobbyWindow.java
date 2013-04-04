package gui;

import javax.swing.JFrame;

public class LobbyWindow {

	public JFrame frmLobby;
	
	public LobbyWindow() {
		initialize();
	}
	
	private void initialize() {
		// TODO: initialize
		
		////////////////////////////////
		// DEBUG
		frmLobby = new JFrame();
		frmLobby.setResizable(false);
		frmLobby.setTitle("Lobby!!!");
		frmLobby.setBounds(100, 100, 500, 500);
		frmLobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLobby.getContentPane().setLayout(null);
		////////////////////////////////
		
	}
	
	public void addTable(int tableNum, String tableName, int numPlayers, int maxPlayers) {
		// TODO
	}
	
	public void updateTable(int tableNum, String tableName, int numPlayers, int maxPlayers) {
		// TODO
	}
	
	public void tableIsFull() {
		// TODO
	}

	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;Sameer;Vasily
		// TODO 
	}
	
	
	
}
