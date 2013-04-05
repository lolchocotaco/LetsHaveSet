package gui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LobbyWindow {

	public JFrame frmLobby;
	private JTable gameList;
	
	public LobbyWindow() {
		initialize();
	}
	
	@SuppressWarnings("serial")
	private void initialize() {
		// TODO: initialize
		
		////////////////////////////////
		// DEBUG
		frmLobby = new JFrame();
		frmLobby.setResizable(false);
		frmLobby.setTitle("Lobby!!!");
		frmLobby.setBounds(100, 100, 500, 500);
		frmLobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLobby.getContentPane().setLayout(null);
		
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
		
		////////////////////////////////
		
	}
	
	private void createTable() {
		
	}
	
	private void joinTable() {
		
	}
	
	public void addTable(String tableNum, String tableName, String numPlayers, String maxPlayers) {
		String status;
		if(Integer.parseInt(numPlayers) < Integer.parseInt(maxPlayers)) {
			status = "Open";
		} else {
			status = "Full";
		}
		( (DefaultTableModel) gameList.getModel() ).addRow(new Object[]{tableNum, tableName, numPlayers + "/" + maxPlayers, status});
	}
	
	public void updateTable(String tableNum, String tableName, String numPlayers, String maxPlayers) {
		int i = 0;
		DefaultTableModel TM = ( (DefaultTableModel) gameList.getModel() );
		Vector<Vector<String> > tableVector = TM.getDataVector();
		Iterator<Vector<String> > it = tableVector.iterator();
	    while(it.hasNext()) {
	      Vector<String> v = it.next();
	      if(Integer.parseInt(tableNum) == Integer.parseInt(v.elementAt(0))) {
	    	  TM.removeRow(i);
	    	  String status;
		  		if(Integer.parseInt(numPlayers) < Integer.parseInt(maxPlayers)) {
		  			status = "Open";
		  		} else {
		  			status = "Full";
		  		}
	    	  TM.insertRow(i, new Object[]{tableNum, tableName, numPlayers + "/" + maxPlayers, status});
	    	  return;
	      }
	      i++;
	    }
	}
	
	public void tableIsFull() {
		JOptionPane.showMessageDialog(frmLobby, "Table is full!");
	}
	
}
