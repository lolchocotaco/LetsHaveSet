package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import setServer.Message;
import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Tween;
import javax.swing.JTextArea;
import java.awt.Color;
import javax.swing.JTextField;

public class TableWindow {

	public JFrame frmTable;
	private SetTable setTable = null;
	private JTable playerList;
	private JPanel tablePanel;
	private JLabel lblStartGame;
	private JTextArea chatWindow;
	private boolean didVote = false;
	
	private BlockingQueue<Message> guiMessages = null;
	private JTextField textField;
	
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
		frmTable.setBounds(100, 100, 1050, 600);
		frmTable.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmTable.getContentPane().setLayout(null);
		
		lblStartGame = new JLabel("Click Ready!");
		lblStartGame.setFont(new Font("Sans", Font.BOLD, 36));
		lblStartGame.setBounds(50, 145, 750, 150);
		
		JButton btnExit = new JButton("EXIT TABLE");
		btnExit.setBounds(884, 526, 130, 23);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				MainClient.sendMessage("E");
			}
		});
		
		frmTable.getContentPane().add(btnExit);

		setTable.tableView.setBorder(BorderFactory.createBevelBorder(1));
		setTable.tableView.add(lblStartGame);
		setTable.tableView.setBounds(10, 10, 850, 550);
		setTable.tableView.setLayout(null);
		frmTable.getContentPane().add(setTable.tableView);
		
		tablePanel = new JPanel();
		tablePanel.setBounds(873, 11, 150, 175);
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
		btnReady.setBounds(884, 502, 130, 23);
		frmTable.getContentPane().add(btnReady);
		
		JButton btnCheat = new JButton("CHEAT");
		btnCheat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SetTable.cheat();
			}
		});
		btnCheat.setBounds(884, 478, 130, 23);
		frmTable.getContentPane().add(btnCheat);
		
		chatWindow = new JTextArea();
		chatWindow.setForeground(Color.BLUE);
		chatWindow.setEditable(false);
		chatWindow.setBounds(873, 197, 150, 234);
		frmTable.getContentPane().add(chatWindow);
		
		textField = new JTextField();
		textField.setColumns(10);
		textField.setBounds(873, 437, 150, 30);
		textField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent evt) {
				if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                   sendChat();
                }
			}
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}
		});
		
		frmTable.getContentPane().add(textField);
		
		GUIThread guiThread = new GUIThread(guiMessages);
		guiThread.start();
		
	}
	
	public void updatePlayers(String[] splitLine) { // splitLine: P;3;4;Nico;2;Sameer;3;Vasily;14
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
	public void tableCards(final String[] splitLine) { // splitLine: T;12;00;01;02;03;04;05;06;07;08;09;10;11
		Thread startGameThread = new Thread() {
			public void run() {
				lblStartGame.setText("Enjoy your game! 3");
				try {
					Thread.sleep(700);
					lblStartGame.setText("Enjoy your game! 2");
					Thread.sleep(700);
					lblStartGame.setText("Enjoy your game! 1");
					Thread.sleep(600);
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
		};			
		startGameThread.start();
	}
	
	public void sendChat() {
		String txt = textField.getText();
		if(txt.contains(";")) {
			JOptionPane.showMessageDialog(frmTable, "Cannot send message with \";\" in it.");
		} else if(!txt.isEmpty()) {
			MainClient.sendMessage("Q;" + txt);
			textField.setText(null);
		}
	}
	
	public void newChat(String user, String chat) {
		chatWindow.append(user + ": " + chat + "\n");
	}

	public void setMade(int C1, int C2, int C3) {
		setTable.setMade(C1, C2, C3);
	}
	
	public void youScrewedUp() {
		// TODO : Display a "bad" indicator
	}
	
	public void youMadeASet() {
		// TODO : Display a "good" indicator
		// JOptionPane.showMessageDialog(frmTable, "You made a set! Nice!\nIsn't this window distracting?");
	}
	
	public static void sendSet( int C1, int C2, int C3){
		MainClient.sendMessage("S;"+ C1+ ";"+ C2 + ";" + C3);
	}

	public void newCards(int C1, int C2, int C3) {
		setTable.newCards(C1, C2, C3);
	}

	public void noSets() {
		// TODO : Display a "No More Sets" indicator
		JOptionPane.showMessageDialog(frmTable, "No more sets, bro!");
	}

	public void gameOver() {
		// TODO : Display a "Game Over" screen
		
		setTable.clearCards();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SetTable.clearSelected();
			};
		});
		JOptionPane.showMessageDialog(frmTable, "Game Over, bro!");
		
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
