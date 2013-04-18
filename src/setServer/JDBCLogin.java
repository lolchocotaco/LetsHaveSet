package setServer;
import java.math.BigInteger;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.security.*;
public class JDBCLogin {
 
  public static void main(String[] argv) throws SQLException {
 
	System.out.println("-------- DB TESTING ------------");
	boolean Register=false;
	boolean Won=true;
	boolean Played=false;
	String user="username";
	String plaintextpwd ="";
	MessageDigest m=null;
	try {
		m = MessageDigest.getInstance("MD5");
	} catch (NoSuchAlgorithmException e1) {
		e1.printStackTrace();
	}
	m.reset();
	m.update(plaintextpwd.getBytes());
	byte[] digest = m.digest();
	BigInteger bigInt = new BigInteger(1,digest);
	String hashtext = bigInt.toString(16);
	while(hashtext.length() < 32 ){
	  hashtext = "0"+hashtext;
	}
	System.out.println(hashtext);
	String passwd=hashtext;	
	
	try {
		Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException e) {
		System.out.println("No JDBC Driver?");
		e.printStackTrace();
		return;
	}
	Connection connection = null;
	Statement stmt = null;
	//Statement stmt2 = null;
	ResultSet usertable = null;
 
	try {
		connection = DriverManager
		.getConnection("jdbc:mysql://199.98.20.119:3306/set","java", "eeonly1");
 
	} catch (SQLException e) {
		System.out.println("Connection Failed!");
		e.printStackTrace();
		return;
	}	
	stmt = connection.createStatement();
	if(Played){
		if(Won){
			stmt.executeUpdate("UPDATE `users` SET `wins`=wins+1 WHERE `username`='"+user+"';");
			stmt.executeUpdate("UPDATE `users` SET `games`=games+1 WHERE `username`='"+user+"';");
		}
		else{
			stmt.executeUpdate("UPDATE `users` SET `games`=games+1 WHERE `username`='"+user+"';");
		}
	}
	else{
		if (!Register){
			usertable=stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+user+"'");
			if (!usertable.next()){
				System.out.println("User not found");
			}
			else {
				String usert = null;
				String passt = null;
				usert = usertable.getString("username");
				passt = usertable.getString("password");
				System.out.println("User: " + usert + "    Password: "+ passt);
				if (passwd.equals(passt)){
					System.out.println("Username/Password matches, Login confirmed");
				}
				else{
					System.out.println("Username/Password does not match");
				}
			}
		}
		else {
			usertable=stmt.executeQuery("SELECT * FROM `users` WHERE `username` =  '"+user+"'");
			if (usertable.next()){
				System.out.println("User already exists");
			}
			else{;
			stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('"+user+"', '"+passwd+"');");	
			System.out.println("User created");
			}
		}
	}
	stmt.close();
	connection.close();

  }

}
