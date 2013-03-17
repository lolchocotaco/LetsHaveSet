package gui;

import gameLogic.SetDeck;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTextField;
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

public class AppWindow {

	private JFrame frmLetsHaveSet;
	private JTable table;
	private JTextField textField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppWindow window = new AppWindow();
					window.frmLetsHaveSet.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AppWindow() {
		initialize();
	}

	/**`
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLetsHaveSet = new JFrame();
		frmLetsHaveSet.setTitle("Let's Have Set");
		frmLetsHaveSet.setBounds(100, 100, 572, 461);
		frmLetsHaveSet.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLetsHaveSet.getContentPane().setLayout(null);
	
		
		JButton btnClickMeh = new JButton("Click to generate deck");
		btnClickMeh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				populateTable();
			}
		});
		btnClickMeh.setBounds(22, 325, 314, 46);
		frmLetsHaveSet.getContentPane().add(btnClickMeh);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 64, 532, 248);
		frmLetsHaveSet.getContentPane().add(scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		table.setFillsViewportHeight(true);
		
		JLabel lblSetDeckGenerator = new JLabel("Set Deck Generator");
		lblSetDeckGenerator.setFont(new Font("Segoe UI", Font.BOLD | Font.ITALIC, 36));
		lblSetDeckGenerator.setHorizontalAlignment(SwingConstants.CENTER);
		lblSetDeckGenerator.setBounds(53, 13, 437, 38);
		frmLetsHaveSet.getContentPane().add(lblSetDeckGenerator);
		
		JLabel lblOfCards = new JLabel("# Of Cards:");
		lblOfCards.setBounds(359, 340, 72, 16);
		frmLetsHaveSet.getContentPane().add(lblOfCards);
		
		textField = new JTextField();
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setEditable(false);
		textField.setText("0");
		textField.setBounds(428, 337, 72, 22);
		frmLetsHaveSet.getContentPane().add(textField);
		textField.setColumns(10);
		
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
	
	
	public void populateTable(){

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
