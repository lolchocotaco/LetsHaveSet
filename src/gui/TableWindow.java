package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Tween;

public class TableWindow {

	public JFrame frmTable;
	private SetTable setTable = null;
	
	public TableWindow() {
		setTable = new SetTable();
		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
		SLAnimator.start();
		initialize();
	}
	
	private void initialize() {
		
		frmTable = new JFrame();
		frmTable.setResizable(false);
		frmTable.setTitle("Table View");
		frmTable.setBounds(100, 100, 800, 600);
		frmTable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTable.getContentPane().setLayout(null);
		
		JLabel lblThisWindowMay = new JLabel("THIS WINDOW MAY\r\n NEED MORE WORK");
		lblThisWindowMay.setFont(new Font("Tahoma", Font.BOLD, 36));
		lblThisWindowMay.setBounds(0, 145, 750, 150);
		
		JButton btnNewButton = new JButton("EXIT TABLE");
		btnNewButton.setBounds(625, 380, 130, 60);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainClient.sendMessage("E");
			}
		});
		frmTable.getContentPane().add(btnNewButton);

		setTable.tableView.setBorder(BorderFactory.createBevelBorder(1));
		setTable.tableView.add(lblThisWindowMay);
		setTable.tableView.setBounds(10, 10, 600, 550);
		setTable.tableView.setLayout(null);
		frmTable.getContentPane().add(setTable.tableView);
		
		JButton btnGetCards = new JButton("Get Cards");
		btnGetCards.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainClient.sendMessage("N");				
			}
		});
		btnGetCards.setBounds(625, 343, 130, 25);
		frmTable.getContentPane().add(btnGetCards);
		
	}
	
	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;Sameer;Vasily
		// TODO 
	}

	//Parses incoming message and adds cards to the board
	public void tableCards(String[] splitLine) { // splitLine: T;12;00;01;02;10;11;12;20;21;22;100;101;102
		int cardNum = 0;
		for( int i =0; i< Integer.parseInt(splitLine[1]); i++){
			cardNum = Integer.parseInt(splitLine[i+2]);
			setTable.addToTable(cardNum);
		}
		setTable.tableView.removeAll();
		setTable.tableView.updateUI();
		setTable.tableView.repaint();
		setTable.tableView.revalidate();
		setTable.tableView.initialize(setTable.defaultLayout());			
	}

	public void dockPoint(String playerName) {
		// TODO 
	}

	public void setMade(String playerName, int C1, int C2, int C3) {
		//TODO
	}
	
	public static void sendSet( int C1, int C2, int C3){
		//TODO
		MainClient.sendMessage("S;"+ C1+ ";"+ C2 + ";" + C3);
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
