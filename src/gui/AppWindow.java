package gui;

import gameLogic.SetDeck;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.Color;

public class AppWindow {

	private JFrame frmLetsHaveSet;
	private JTable table;
	private JTextField textField;
	
	private JFrame frmLogin;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JTextField txtError;

	/**
	 * Create the application.
	 */
	public AppWindow() {
		initialize();
	    frmLetsHaveSet.setVisible(true);
	}

	/**`
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLetsHaveSet = new JFrame();
		frmLetsHaveSet.setResizable(false);
		frmLetsHaveSet.setTitle("Let's Have Set");
		frmLetsHaveSet.setBounds(100, 100, 800, 600);
		frmLetsHaveSet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLetsHaveSet.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 13, 772, 527);
		frmLetsHaveSet.getContentPane().add(tabbedPane);
		
		JPanel deckList = new JPanel();
		deckList.setToolTipText("");
		tabbedPane.addTab("View Deck", null, deckList, null);
		deckList.setLayout(null);
		
			
			JButton btnClickMeh = new JButton("Click to generate deck");
			btnClickMeh.setBounds(41, 442, 314, 46);
			deckList.add(btnClickMeh);
			
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(49, 77, 657, 336);
			deckList.add(scrollPane);
			
			table = new JTable();
			scrollPane.setViewportView(table);
			table.setFillsViewportHeight(true);
			
			JLabel lblSetDeckGenerator = new JLabel("Set Deck Generator");
			lblSetDeckGenerator.setBounds(145, 21, 437, 38);
			deckList.add(lblSetDeckGenerator);
			lblSetDeckGenerator.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 36));
			lblSetDeckGenerator.setHorizontalAlignment(SwingConstants.CENTER);
			
			JLabel lblOfCards = new JLabel("# Of Cards:");
			lblOfCards.setBounds(441, 457, 72, 16);
			deckList.add(lblOfCards);
			
			textField = new JTextField();
			textField.setBounds(510, 454, 72, 22);
			deckList.add(textField);
			textField.setHorizontalAlignment(SwingConstants.CENTER);
			textField.setEditable(false);
			textField.setText("0");
			textField.setColumns(10);
			
			JPanel deckView = new JPanel();
			tabbedPane.addTab("See Cards", null, deckView, null);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			deckView.add(scrollPane_1);
			btnClickMeh.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					populateTable();
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
		
		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);
		
	}
	
	
	private void populateTable(){

		SetDeck setGame = new SetDeck();
		Object data [][] = new Object[81][4];
		
		for(int i = 0; i<81; i++){
			data[i][0] = setGame.deck.elementAt(i).getColor();
			data[i][1] = setGame.deck.elementAt(i).getNumber();
			data[i][2] = setGame.deck.elementAt(i).getShape();
			data[i][3] = setGame.deck.elementAt(i).getShade();
		}
		
		String colName [] = {"Color", "Number", "Shape", "Shade"};

		
		DefaultTableModel tableModel = new DefaultTableModel(data,colName);
		tableModel.fireTableDataChanged();
		table.setModel(tableModel);
		int dLength = setGame.deck.size();
		textField.setText(Integer.toString(dLength));
		
	}
}
