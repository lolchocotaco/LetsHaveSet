package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class LoginWindow {

	public JFrame frmLogin;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JTextField txtError;
//	private static final String websiteAddress = "http://sable10.ee.cooper.edu:5080/account/add.php";
	 
	/**
	 * Launch the application.
	 */
	
	/*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow window = new LoginWindow();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	*/

	/**
	 * Create the application.
	 */
	public LoginWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLogin = new JFrame();
		frmLogin.setResizable(false);
		frmLogin.setTitle("Login");
		frmLogin.setBounds(100, 100, 372, 236);
		frmLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLogin.getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Let's Have Set");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 26));
		lblNewLabel.setBounds(58, 11, 231, 22);
		frmLogin.getContentPane().add(lblNewLabel);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblUsername.setBounds(65, 73, 103, 22);
		frmLogin.getContentPane().add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblPassword.setBounds(65, 106, 88, 14);
		frmLogin.getContentPane().add(lblPassword);
		
		txtUsername = new JTextField();
		txtUsername.setText("username");
		txtUsername.setBounds(190, 76, 86, 20);
		frmLogin.getContentPane().add(txtUsername);
		txtUsername.setColumns(10);
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(190, 105, 86, 20);
		frmLogin.getContentPane().add(txtPassword);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkLogin();
			}
		});
		btnLogin.setBounds(187, 136, 89, 23);
		frmLogin.getContentPane().add(btnLogin);
		
		JButton btnRegister = new JButton("Register");
		btnRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openRegister();
			}
		});
		btnRegister.setBounds(65, 136, 89, 23);
		frmLogin.getContentPane().add(btnRegister);
		
		txtError = new JTextField();
		txtError.setHorizontalAlignment(SwingConstants.CENTER);
		txtError.setText("Please Login");
		txtError.setForeground(Color.BLACK);
		txtError.setEditable(false);
		txtError.setBounds(78, 44, 198, 22);
		frmLogin.getContentPane().add(txtError);
		txtError.setColumns(10);
		
		frmLogin.getRootPane().setDefaultButton(btnLogin);
	}
	
	
	/*
	 * Class Functions.
	 */
	private void checkLogin(){
		String userName = txtUsername.getText();
		char[] passWord = txtPassword.getPassword();
		String strPassword = passWord.toString();
		String message = "L;" + userName + ";" + strPassword;
		MainClient.sendMessage(message);
		
		/*
		if(userName.equals("username")){
			new AppWindow();
			frmLogin.dispose();
		}
		else{
			System.out.println("Password is incorrect");
			System.out.println("Username: " +userName);
			System.out.println("Password: "+strPassword);
			txtError.setForeground(new Color(16711680));
			txtError.setText("Invalid Username/Password");
		}		
		*/
	}
	
	private void openRegister(){
		String userName = txtUsername.getText();
		char[] passWord = txtPassword.getPassword();
		String strPassword = passWord.toString();
		String message = "R;" + userName + ";" + strPassword;
		MainClient.sendMessage(message);
		
		/*
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	    		URI uri = new URI(websiteAddress);
	            desktop.browse(uri);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    */
	}
	
	public void loginError() {
		txtError.setForeground(new Color(16711680));
		txtError.setText("Invalid Username/Password");
	}
	
}
