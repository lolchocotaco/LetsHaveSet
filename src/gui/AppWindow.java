package gui;

import gameLogic.SetCard;
import gameLogic.SetDeck;


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

public class AppWindow {

	private JFrame frmLetsHaveSet;
	private JTable table;
	private JTextField textField;
	private SetDeck setGame = new SetDeck();
	private JPanel cardView = new JPanel();
	
	/**
	 * Create the application.
	 */
	public AppWindow() {
		initialize();
		populateTable();
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
			

			cardView.setBounds(12, 13, 771, 519);
			deckView.add(cardView);
			cardView.setLayout(null);
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
		setGame = new SetDeck();
		populateTable();
	}
	
	private void populateTable(){
		int dLength = setGame.deck.size();
		Object data [][] = new Object[dLength][4];
		for(int i = 0; i<dLength; i++){
			data[i][0] = setGame.deck.elementAt(i).getColor();
			data[i][1] = setGame.deck.elementAt(i).getNumber();
			data[i][2] = setGame.deck.elementAt(i).getShape();
			data[i][3] = setGame.deck.elementAt(i).getShade();
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
	
	private void displayCards(){

		
		for( int n = 0; n <4; n++){
			for(int i = 0;  i <3; i++){
				if (setGame.deck.size()>0){
					SetCard tCard = setGame.deck.remove(0);
					setGame.onTable.add(tCard);
					
					JPanel tCardView = new JPanel();
					tCardView.setBounds(20+n*(100+10), 5+i*(150+15), 100, 150);
					tCardView.setBorder(BorderFactory.createLineBorder(Color.black));
					
					String tCardStat = "Color: " + tCard.getColor() + "\nNumber: " + tCard.getNumber() + "\nShape: " + tCard.getShape() + "\nShade: " + tCard.getShade();
					JTextArea tCardInfo = new JTextArea(tCardStat);
					tCardInfo.setEditable(false);
					tCardInfo.setBackground(null);
					tCardView.add(tCardInfo);
					cardView.add(tCardView);
				}
				else{
					// TODO Link Panels with Cards in onTable Vector
					// TODO Figure out Tween for annimations. 
					// TODO Game Logic
				}			
			}
		}
	}
	
	
	
}
