package setServer;

public class User {
	public String username;
	public int numWins;
	public int numLosses;
	public int currentTable;
	
	public User(String username, int numWins, int numLosses, int currentTable) {
		this.username = username;
		this.numWins = numWins;
		this.numLosses = numLosses;
		this.currentTable = currentTable;
	}
};
