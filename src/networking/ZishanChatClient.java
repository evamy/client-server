package networking;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

public class ZishanChatClient {
	JTextField outgoing;
	JTextArea incoming;
	JTextField nameField;
	JLabel nameLabel;
	PrintWriter writer;
	BufferedReader reader;
	Socket sock;
	String name;
	
	public static void main(String[] args) {
		ZishanChatClient client = new ZishanChatClient();
		client.start();
	}
	
	public void start() {
		JFrame frame = new JFrame("ZANT Chat Client");
		JPanel header = new JPanel();
		JPanel mainPanel = new JPanel();
		JPanel footer = new JPanel();
		
		outgoing = new JTextField(20);
		incoming = new JTextArea(15,50);
		incoming.setLineWrap(true);
		incoming.setWrapStyleWord(true);
		incoming.setEditable(false);
		
		DefaultCaret caret = (DefaultCaret)incoming.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane qScroller = new JScrollPane(incoming);
		qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		nameField = new JTextField(10);
		nameField.setName("Bob");
		nameLabel = new JLabel("Enter name: ");
		
		JButton sendButton = new JButton("Send");
		JButton setName = new JButton("Set");
		
		sendButton.addActionListener(new SendButtonListener());
		setName.addActionListener(new SetNameListener());
		
		header.add(nameLabel);
		header.add(nameField);
		header.add(setName);
		
		mainPanel.add(qScroller);
		
		footer.add(outgoing);
		footer.add(sendButton);
		
		setUpNetworking();
		
		Thread readerThread = new Thread(new IncomingReader());
		readerThread.start();
		
		frame.getContentPane().add(BorderLayout.NORTH, header);
		frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
		frame.getContentPane().add(BorderLayout.SOUTH, footer);
	
		frame.setSize(580, 350);
		frame.setVisible(true);
	}
	
	private void setUpNetworking() {
		try {
			sock = new Socket("127.0.0.1", 5000);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			reader = new BufferedReader(streamReader);
			writer = new PrintWriter(sock.getOutputStream());
			System.out.println("Networking established");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				String message;
				if(outgoing.getText().equals("bye")) {
					message = outgoing.getText();
					writer.println(message);
					writer.flush();
					System.exit(0);
				}
				int pos = outgoing.getText().indexOf("[");
				if(pos == 0) {
					String rec = outgoing.getText().substring(0, outgoing.getText().indexOf("]") + 1);
					String mes = outgoing.getText().substring(outgoing.getText().indexOf("]") + 1);
					message = rec + name + ":" + mes;
				} else {
					message = name + ": " + outgoing.getText();
				}
				writer.println(message);
				writer.flush();
				message = "";
			} catch (Exception e) {
				e.printStackTrace();
			}
			outgoing.setText("");
			outgoing.requestFocus();
		}
	}
	
	public class SetNameListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			name = nameField.getText();
			String nameSend = "$^@" + name;
			writer.println(nameSend);
			writer.flush();
		}
	}
	
	public class IncomingReader implements Runnable {
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					int pos = message.indexOf("$^@");
					if(pos != -1) continue;
					System.out.println(message);
					incoming.append(message + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}