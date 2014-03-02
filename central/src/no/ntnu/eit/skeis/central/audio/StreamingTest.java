package no.ntnu.eit.skeis.central.audio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.ntnu.eit.skeis.central.audio.mp3.Frame;

public class StreamingTest implements AudioSource {

	private final ServerSocket server;
	
	private final Set<Socket> clients;
	
	class ClientConnectionThread extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					final Socket s = server.accept();
					
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
						
						String line;
						while((line = reader.readLine()) != null) {
							if(line.trim().equals("")) break;
							System.out.println(line);
						}
						
						OutputStream out = s.getOutputStream();
						out.write((
							"HTTP/1.1 200 OK\r\n"+
							"Content-Type: audio/mpeg\r\n"+
							"\r\n"
						).getBytes());
						
						synchronized (clients) {
							clients.add(s);
						}
					} catch(IOException e) {
						// Client fail, drop it
					}
				}
			} catch(IOException e) {
				// Socket closed most likely
			}
		}
	}
	
	public StreamingTest(final InputStream in) throws Exception {
				
		
		clients = new HashSet<Socket>();
		server = new ServerSocket(8520);
				
		new ClientConnectionThread().start();
		
		new Thread() {
			public void run() {
				try {
					Frame frame;
					long time = -1;
					while((frame = Frame.fromInputStream(in)) != null) {
						synchronized(clients) {
							Iterator<Socket> it = clients.iterator();
							while(it.hasNext()) {
								Socket client = it.next();
								try {
									OutputStream out = client.getOutputStream();
									frame.writeToOutputStream(out);
									out.flush();
								} catch(IOException e) {
									it.remove();
								}
							}
						}
						if (time != -1) {
							long diff = 26+System.currentTimeMillis()-time;
							try { Thread.sleep(diff); } catch(Exception e) {}
						}
						time = System.currentTimeMillis();
						
					}
				} catch(IOException e) {
					e.printStackTrace();
				} finally {
					try {
						server.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}.start();
	}
	
	public static void main(String[] args) throws Exception {
		new StreamingTest(new FileInputStream(new File("/home/runar/84.mp3")));
	}

	@Override
	public String getHttpUrl() {
		try {
			return "http://"+InetAddress.getLocalHost().getHostAddress()+":"+server.getLocalPort();
		} catch(Exception e) {
			return "";
		}
	}

	@Override
	public String getSonosUrl() {
		try {
			return "x-rincon-mp3radio://"+InetAddress.getLocalHost().getHostAddress()+":"+server.getLocalPort();
		} catch(Exception e) {
			return "";
		}
	}
	
}
