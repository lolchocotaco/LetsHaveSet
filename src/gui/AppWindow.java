package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import aurelienribon.slidinglayout.SLAnimator;
import aurelienribon.tweenengine.Tween;


/**
 * @author Sameer
 * Animation functionality is almost up. Its funky right now. 
 * Many of the add/remove functionalities of the buttons does not link with the animation method
 * Need to remove obsolete functions/ interfering functions. 
 * Animations rely on event listeners on panels. Buttons do nothing. Maybe link the two somehow?
 *
 */


public class AppWindow {
	
	private Socket clientSocket = null;

	public JFrame frmLetsHaveSet;
	private SetTable setGame = new SetTable();
	private JTable gameList;
	
	/**
	 * Create the application.
	 */
	public AppWindow(Socket clientSocket) {
		this.clientSocket = clientSocket;
		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
		SLAnimator.start();
		initialize();
		displayCards();
		populateTable();
	}

	/**`
	 * Initialize the contents of the frame.
	 */
	@SuppressWarnings("serial")
	private void initialize() {
		frmLetsHaveSet = new JFrame();
		frmLetsHaveSet.setResizable(false);
		frmLetsHaveSet.setTitle("Let's Have Set");
		frmLetsHaveSet.setBounds(100, 100, 830, 650);
		frmLetsHaveSet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLetsHaveSet.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 13, 800, 575);
		frmLetsHaveSet.getContentPane().add(tabbedPane);
			
			final JPanel gameLobby = new JPanel();
			tabbedPane.addTab("Game Lobby", null, gameLobby, null);
			gameLobby.setLayout(null);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(50, 50, 700, 400);
			gameLobby.add(scrollPane_1);
			
			gameList = new JTable();
			gameList.setAutoCreateRowSorter(true);
			gameList.setModel(new DefaultTableModel(
				new Object[][] {},
				new String[] {
					"Table Number", "Table Name", "Players", "Status"
				}
			) {
				boolean[] columnEditables = new boolean[] {
					false, false, false, false
				};
				public boolean isCellEditable(int row, int column) {
					return columnEditables[column];
				}
			});
			gameList.getColumnModel().getColumn(0).setResizable(false);
			gameList.getColumnModel().getColumn(0).setPreferredWidth(100);
			gameList.getColumnModel().getColumn(1).setResizable(false);
			gameList.getColumnModel().getColumn(1).setPreferredWidth(250);
			gameList.getColumnModel().getColumn(2).setResizable(false);
			gameList.getColumnModel().getColumn(2).setPreferredWidth(250);
			gameList.getColumnModel().getColumn(3).setResizable(false);
			gameList.getColumnModel().getColumn(3).setPreferredWidth(100);
			gameList.setFillsViewportHeight(true);
			scrollPane_1.setViewportView(gameList);
			
			JButton createTable = new JButton("Create Game\r\n");
			createTable.setBounds(100, 475, 150, 45);
			gameLobby.add(createTable);
			
			final JPanel deckView = new JPanel();
			tabbedPane.addTab("See Cards", null, deckView, null);
			deckView.setLayout(null);
			deckView.setVisible(false);
			
			JButton joinTable = new JButton("Join Table");
/*			joinTable.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					deckView.setVisible(true);
					gameLobby.setVisible(false);
				}
			});
*/			joinTable.setBounds(550, 475, 150, 45);
			gameLobby.add(joinTable);
			
			

			setGame.tableView.setBounds(12, 13, 641, 523);
			setGame.tableView.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					populateTable();
					
				}
			});
			
			deckView.add(setGame.tableView);
			setGame.tableView.setLayout(null);
			
			JButton btnClearSelected = new JButton("Clear Selected");
			btnClearSelected.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setGame.clearSelected();
				}
			});
			btnClearSelected.setBounds(636, 511, 147, 25);
			deckView.add(btnClearSelected);
		
		JMenuBar menuBar = new JMenuBar();
		frmLetsHaveSet.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Back to Game Lobby");
		mnFile.add(mntmOpen);
		
		JMenuItem mntmLogout = new JMenuItem("Logout");
		mntmLogout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setLogout();
			}
		});
		mnFile.add(mntmLogout);
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
	}
	/* Filles table with cards currently on the deck*/
	/* Actually a useless function that needs to go */
	public void populateTable(){
//		int dLength = setGame.tableSize();
		int dLength = setGame.selectedCards.size();
		
		
		Object data [][] = new Object[dLength][4];
		for(int i = 0; i<dLength; i++){
			SetCard tmpCard= setGame.selectedCards.elementAt(i);
			data[i][0] = tmpCard.getColor();
			data[i][1] = tmpCard.getNumber();
			data[i][2] = tmpCard.getShape();
			data[i][3] = tmpCard.getShade();
		}
		
		String colName [] = {"Color", "Number", "Shape", "Shade"};

		
		DefaultTableModel tableModel = new DefaultTableModel(data,colName);
		tableModel.fireTableDataChanged();
	}
	
	private void setLogout(){
		frmLetsHaveSet.dispose();
//		new LoginWindow();
	}
	/*Draws 12 cards and puts them on the table*/
	private void displayCards(){
		setGame.tableView.removeAll();
		for( int n = 0; n <4; n++){
			for(int i = 0;  i <3; i++){
				if (setGame.deckSize()>0){
					setGame.drawCard();
					
//					tCard.setBounds(20+n*(100+10), 5+i*(150+15), 100, 150);
//					setGame.tableView.add(tCard);
				}
				else{
					// TODO Link Panels with Cards in onTable Vector
					// TODO Figure out Tween for annimations. 
					// TODO Game Logic
				}			
			}
		}
		setGame.tableView.updateUI();
		setGame.tableView.repaint();
		setGame.tableView.revalidate();
		setGame.tableView.initialize(setGame.defaultLayout());
	}//End displayCards()
}//End Appwindow
