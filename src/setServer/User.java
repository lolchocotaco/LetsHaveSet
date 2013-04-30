package setServer;

public class User {
	public String username;
	public int numWins;
	public int numLosses;
	public int currentTable;
	public boolean isAdmin;	
	
	public User(String username, int numWins, int numLosses, int currentTable, boolean isAdmin) {
		this.username = username;
		this.isAdmin=isAdmin;
		this.numWins = numWins;
		this.numLosses = numLosses;
		this.currentTable = currentTable;
	}
};
