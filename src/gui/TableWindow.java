package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import setServer.Message;
import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Tween;

public class TableWindow {

	public JFrame frmTable;
	private SetTable setTable = null;
	private JTable playerList;
	private JPanel tablePanel;
	private JLabel lblStartGame;
	private boolean didVote = false;
	
	private BlockingQueue<Message> guiMessages = null;
	
	public TableWindow() {
		setTable = new SetTable();
		guiMessages = new LinkedBlockingQueue<Message>();
		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
		SLAnimator.start();
		initialize();
	}
	
	public class GUIThread extends Thread {
		
		BlockingQueue<Message> guiMessages = null;
		
		public GUIThread(BlockingQueue<Message> guiMessages) {
			super("GUIThread");
			this.guiMessages = guiMessages;
		}
		
		public void run() {
			
		}
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
		
		JButton btnExit = new JButton("EXIT TABLE");
		btnExit.setBounds(634, 470, 130, 60);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainClient.sendMessage("E");
			}
		});
		
		frmTable.getContentPane().add(btnExit);

		setTable.tableView.setBorder(BorderFactory.createBevelBorder(1));
		setTable.tableView.add(lblStartGame);
		setTable.tableView.setBounds(10, 10, 600, 550);
		setTable.tableView.setLayout(null);
		frmTable.getContentPane().add(setTable.tableView);
		
		tablePanel = new JPanel();
		tablePanel.setBounds(634, 11, 150, 175);
		frmTable.getContentPane().add(tablePanel);
		
		playerList = new JTable();
		playerList.setEnabled(false);
		playerList.setBounds(0, 0, 150, 175);
		
		JTableHeader tableHeader = playerList.getTableHeader();
		playerList.setShowGrid(false);
		playerList.setModel(new DefaultTableModel(
			new Object[][] {},
			new String[] {"Player", "Points" })
		{
			boolean[] columnEditables = new boolean[] {
				false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(playerList,BorderLayout.CENTER);
		tablePanel.add(tableHeader, BorderLayout.NORTH);
		
		final JButton btnReady = new JButton("Ready!");
		btnReady.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(!didVote){
					MainClient.sendMessage("G");
					didVote = true;
					btnReady.setEnabled(false);
					btnReady.setText("Waiting");
					lblStartGame.setText("Waiting for Players...");
				}
			}
		});
		btnReady.setBounds(634, 436, 130, 23);
		frmTable.getContentPane().add(btnReady);
		
		GUIThread guiThread = new GUIThread(guiMessages);
		guiThread.start();
		
	}
	
	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;0;Sameer;0;Vasily;0
		int numPlayers = Integer.parseInt(splitLine[1]);
		Object data [][] = new Object[numPlayers][4];
		for(int i = 0; i<numPlayers; i++){
			data[i][0] = splitLine[2*i+3];
			data[i][1] = splitLine[2*i+4];
		}
		String colName [] = {"Player", "Points"};
		DefaultTableModel tm = new DefaultTableModel(data,colName);
		playerList.setModel(tm);
		tm.fireTableDataChanged();
	}

	//Parses incoming message and adds cards to the board
	public void tableCards(String[] splitLine) { // splitLine: T;12;00;01;02;10;11;12;20;21;22;100;101;102
		lblStartGame.setText("Enjoy your game!");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			//do nothing
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

	public void setMade(int C1, int C2, int C3) {
		//TODO
	}
	
	public void youMadeASet() {
		// TODO
	}
	
	public static void sendSet( int C1, int C2, int C3){
		MainClient.sendMessage("S;"+ C1+ ";"+ C2 + ";" + C3);
	}

	public void newCards(int C1, int C2, int C3) {
		//TODO 
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
