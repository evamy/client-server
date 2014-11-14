package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import javax.sound.midi.Receiver;

public class ZANTChatServer {
	ArrayList<PrintWriter> clientOutputStreams;
	HashMap<String, PrintWriter> map;
	int noc;
	
	public static void main(String[] args) {
		new ZANTChatServer().start();
	}
	
	public void start() {
		clientOutputStreams = new ArrayList<PrintWriter>();
		map = new HashMap<String, PrintWriter>();
		noc = -1;
		try {
			ServerSocket serverSock = new ServerSocket(5000);
			while(true) {
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
				clientOutputStreams.add(writer);
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				noc++;
				System.out.println("Got a connection at " + clientSocket.toString());
				System.out.println("Clients connected: " + (noc+1));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		
		public ClientHandler(Socket clientSocket) {
			sock = clientSocket;
			try {
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public void run() {
			String message;
			try {
				while((message = reader.readLine()) != null) {
					int pos = message.indexOf("$^@");
					if(pos == 0) {
						String name = message.substring(3);
						System.out.println(name);
						map.put(name, clientOutputStreams.get(noc));
						//System.out.println(name + " : " + map.get(name));
						continue;
					}
					pos = message.indexOf("[");
					//System.out.println("Pos: " + pos);
					if(pos == 0) {
						String recipients = message.substring(1, message.indexOf("]"));
						//System.out.println(recipients);
						message = message.substring(message.indexOf("]") + 1);
						String name = message.substring(0, message.indexOf(":"));
						recipients += "," + name;
						for (String rec : recipients.split(",")) {
							//System.out.println(rec);
							PrintWriter writer = map.get(rec);
							writer.println(message);
							writer.flush();
						}
						System.out.println("reading: " + message);
						continue;
					}
					System.out.println("read: " + message);
					broadcast(message);
					if(message.equals("bye")) {
						sock.close();
						break;
					}
				}
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void broadcast(String message) {
		ListIterator<PrintWriter> it = clientOutputStreams.listIterator();
		while(it.hasNext()) {
			try {
				PrintWriter writer =  it.next();
				writer.println(message);
				writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}