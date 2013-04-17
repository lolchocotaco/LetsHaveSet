package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Tween;

public class TableWindow {

	public JFrame frmTable;
	private SetTable setTable = null;
	private JTable playerList;
	private JPanel tablePanel;
	private JLabel lblStartGame;
	private boolean didVote = false;
	
	public TableWindow() {
		setTable = new SetTable();
		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
		SLAnimator.start();
		initialize();
	}
	
	@SuppressWarnings("serial")
	private void initialize() {
		
		frmTable = new JFrame();
		frmTable.setResizable(false);
		frmTable.setTitle("Table View");
		frmTable.setBounds(100, 100, 800, 600);
		frmTable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTable.getContentPane().setLayout(null);
		
		lblStartGame = new JLabel("Click Ready!");
		lblStartGame.setFont(new Font("Sans", Font.BOLD, 36));
		lblStartGame.setBounds(50, 145, 750, 150);
		
		JButton btnNewButton = new JButton("EXIT TABLE");
		btnNewButton.setBounds(634, 470, 130, 60);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainClient.sendMessage("E");
			}
		});
		
		frmTable.getContentPane().add(btnNewButton);

		setTable.tableView.setBorder(BorderFactory.createBevelBorder(1));
		setTable.tableView.add(lblStartGame);
		setTable.tableView.setBounds(10, 10, 600, 550);
		setTable.tableView.setLayout(null);
		frmTable.getContentPane().add(setTable.tableView);
		
		tablePanel = new JPanel();
		tablePanel.setBounds(634, 11, 150, 175);
		frmTable.getContentPane().add(tablePanel);
		
		playerList = new JTable();
		playerList.setBounds(0, 0, 150, 175);
		
		JTableHeader tableHeader = playerList.getTableHeader();
		playerList.setShowGrid(false);
		playerList.setModel(new DefaultTableModel(
			new Object[][] {},
			new String[] {"Player", "Points" })
		{
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(playerList,BorderLayout.CENTER);
		tablePanel.add(tableHeader, BorderLayout.NORTH);
		
		JButton btnNewButton_1 = new JButton("Ready!");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!didVote){
					MainClient.sendMessage("G");
					didVote = true;
				}
			}
		});
		btnNewButton_1.setBounds(634, 436, 130, 23);
		frmTable.getContentPane().add(btnNewButton_1);
		
	}
	
	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;Sameer;Vasily
		// TODO 
		int numPlayers = Integer.parseInt(splitLine[1]);
		Object data [][] = new Object[numPlayers][4];
		for(int i = 0; i<numPlayers; i++){
			data[i][0] = splitLine[i+3];
			data[i][1] = 0;
		}
		String colName [] = {"Player", "Points"};
		DefaultTableModel tm = new DefaultTableModel(data,colName);
		playerList.setModel(tm);
		tm.fireTableDataChanged();
	}

	//Parses incoming message and adds cards to the board
	public void tableCards(String[] splitLine) { // splitLine: T;12;00;01;02;10;11;12;20;21;22;100;101;102
		//TODO put start game information
		lblStartGame.setText("Enjoy your game!");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lblStartGame.setVisible(false);
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
	
	
	public void hideTable(){
		frmTable.setVisible(false);
	}
	
	public void showTable(){
		setTable = new SetTable();
		didVote = false;
		initialize();
		frmTable.setVisible(true);
	}


}
