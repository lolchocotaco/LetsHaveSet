package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class TableWindow {

	public JFrame frmTable;
	
	public TableWindow() {
		initialize();
	}
	
	private void initialize() {
		
		frmTable = new JFrame();
		frmTable.setResizable(false);
		frmTable.setTitle("Table View");
		frmTable.setBounds(100, 100, 800, 500);
		frmTable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTable.getContentPane().setLayout(null);
		
		JLabel lblThisWindowMay = new JLabel("THIS WINDOW MAY\r\n NEED MORE WORK");
		lblThisWindowMay.setFont(new Font("Tahoma", Font.BOLD, 36));
		lblThisWindowMay.setBounds(35, 145, 750, 150);
		frmTable.getContentPane().add(lblThisWindowMay);
		
		JButton btnNewButton = new JButton("EXIT TABLE");
		btnNewButton.setBounds(625, 380, 130, 60);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainClient.sendMessage("E");
			}
		});
		frmTable.getContentPane().add(btnNewButton);
		
	}
	
	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;Sameer;Vasily
		// TODO 
	}

	public void tableCards(String[] splitLine) { // splitLine: T;12;01;02;03;04;05;06;07;08;09;10;11;12
		// TODO 
	}

	public void dockPoint(String playerName) {
		// TODO 
	}

	public void setMade(String playerName, int C1, int C2, int C3) {
		// TODO 
	}

	public void newCards(int C1, int C2, int C3) {
		// TODO 
	}

	public void noSets() {
		// TODO 
	}

	public void gameOver() {
		// TODO 
	}
}