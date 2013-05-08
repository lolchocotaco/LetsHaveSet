package setServer;

public class User {
	public String username;
	public int rating;
	public int currentTable;
	public boolean isAdmin;	
	
	public User(String username, int rating, int currentTable, boolean isAdmin) {
		this.username = username;
		this.rating = rating;
		this.currentTable = currentTable;
		this.isAdmin = isAdmin;
	}
};
