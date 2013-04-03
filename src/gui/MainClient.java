package gui;



import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainClient {
	
	public static void main(String[] args) {
		
		try {
			Socket clientSocket = new Socket("sable10.ee.cooper.edu", 5342);
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host Exception thrown!");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Error connecting to Server!");
			System.exit(-1);
		}
		
		LoginWindow loginWindow = new LoginWindow();
		AppWindow appWindow = new AppWindow();
		
	 loginWindow.frmLogin.setVisible(true);
		
	}

}
