package gui;

import gameLogic.SetCard;
import gameLogic.SetTable;


import javax.print.attribute.standard.DocumentName;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.*;
import javax.swing.text.Document;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;

import aurelienribon.slidinglayout.*;
import aurelienribon.tweenengine.Tween;

public class AppWindow {

	private JFrame frmLetsHaveSet;
	private JTable table;
	private JTextField textField;
	private SetTable setGame = new SetTable();
	
	/**
	 * Create the application.
	 */
	public AppWindow() {
		Tween.registerAccessor(SetCard.class, new SetCard.Accessor());
		SLAnimator.start();
		initialize();
		displayCards();
		populateTable();
		setGame.setLayout();
	    frmLetsHaveSet.setVisible(true);
	}

	/**`
	 * Initialize the contents of the frame.
	 */
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
		
		JPanel deckList = new JPanel();
		tabbedPane.addTab("View Deck", null, deckList, null);
		deckList.setLayout(null);
		
			
			JButton btnClickMeh = new JButton("Generate New Deck");
			btnClickMeh.setBounds(63, 471, 209, 46);
			deckList.add(btnClickMeh);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(63, 117, 657, 336);
			deckList.add(scrollPane);
			
			table = new JTable();
			scrollPane.setViewportView(table);
			table.setFillsViewportHeight(true);
			
			JLabel lblSetDeckGenerator = new JLabel("Set Deck Generator");
			lblSetDeckGenerator.setBounds(147, 13, 437, 38);
			deckList.add(lblSetDeckGenerator);
			lblSetDeckGenerator.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 36));
			lblSetDeckGenerator.setHorizontalAlignment(SwingConstants.CENTER);
			
			JLabel lblOfCards = new JLabel("# Of Cards:");
			lblOfCards.setBounds(625, 486, 72, 16);
			deckList.add(lblOfCards);
			
			textField = new JTextField();
			textField.setBounds(694, 483, 72, 22);
			deckList.add(textField);
			textField.setHorizontalAlignment(SwingConstants.CENTER);
			textField.setEditable(false);
			textField.setText("0");
			textField.setColumns(10);
			
			JButton btnClickToRemove = new JButton("Remove Card");
			btnClickToRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayCards();
					populateTable();
				}
			});
			btnClickToRemove.setBounds(368, 471, 209, 46);
			deckList.add(btnClickToRemove);
			
			JPanel deckView = new JPanel();
			tabbedPane.addTab("See Cards", null, deckView, null);
			deckView.setLayout(null);
			

			setGame.tableView.setBounds(12, 13, 641, 523);
			deckView.add(setGame.tableView);
			setGame.tableView.setLayout(null);
			
			JButton btnDraw = new JButton("Draw");
			btnDraw.setBounds(690, 33, 95, 17);
			deckView.add(btnDraw);
			
			JButton btnNewDeal = new JButton("New Deal");
			btnNewDeal.setBounds(690, 13, 95, 17);
			deckView.add(btnNewDeal);
			
			JButton btnRmFromTbl = new JButton("Rm from Tbl");
			btnRmFromTbl.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					rmCard(0);
				}
			});
			btnRmFromTbl.setBounds(690, 57, 95, 17);
			deckView.add(btnRmFromTbl);
			btnNewDeal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					newDeck();
				}
			});
			btnDraw.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					displayCards();
					populateTable();
				}
			});
			btnClickMeh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					newDeck();
				}
			});
		
		JMenuBar menuBar = new JMenuBar();
		frmLetsHaveSet.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
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

	private void newDeck(){
		setGame.newDeck();
		displayCards();
		populateTable();
		
	}
	/* Filles table with cards currently on the deck*/
	private void populateTable(){
		int dLength = setGame.deckSize();
		Object data [][] = new Object[dLength][4];
		for(int i = 0; i<dLength; i++){
			SetCard tmpCard= setGame.getElementAt(i);
			data[i][0] = tmpCard.getColor();
			data[i][1] = tmpCard.getNumber();
			data[i][2] = tmpCard.getShape();
			data[i][3] = tmpCard.getShade();
		}
		
		String colName [] = {"Color", "Number", "Shape", "Shade"};

		
		DefaultTableModel tableModel = new DefaultTableModel(data,colName);
		tableModel.fireTableDataChanged();
		table.setModel(tableModel);
	
		textField.setText(Integer.toString(dLength));
	}
	
	private void setLogout(){
		frmLetsHaveSet.dispose();
		new LoginWindow();
	}
	private void rmCard(int loc){
		setGame.rmTableCard(loc);
		setGame.setLayout();
	}
	/*Draws 12 cards and puts them on the table*/
	private void displayCards(){
		setGame.tableView.removeAll();
		for( int n = 0; n <4; n++){
			for(int i = 0;  i <3; i++){
				if (setGame.deckSize()>0){
					SetCard tCard = setGame.drawCard();
					
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
		setGame.setLayout();
	}//End displtableView.ayCards();
}//End Appwindow
