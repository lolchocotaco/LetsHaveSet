package gui;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JTextField;

public class AppWindow {

	private JFrame frame;
	private JTextField txtWhat;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppWindow window = new AppWindow();
					window.frame.setVisible(true);
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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 318, 205);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JButton btnNewButton = new JButton("Le Button");
		btnNewButton.setBounds(39, 93, 175, 67);
		frame.getContentPane().add(btnNewButton);
		
		txtWhat = new JTextField();
		txtWhat.setText("What?");
		txtWhat.setBounds(79, 46, 86, 20);
		frame.getContentPane().add(txtWhat);
		txtWhat.setColumns(10);
	}
}
