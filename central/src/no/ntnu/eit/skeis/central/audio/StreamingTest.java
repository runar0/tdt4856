package no.ntnu.eit.skeis.central.audio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.mp3.Frame;

import org.fourthline.cling.support.lastchange.LastChange;

public class StreamingTest implements AudioSource {

	private final ServerSocket server;	
	private final Set<Socket> clients;	
	private final Device device;
	
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
	
	public StreamingTest(final BufferedInputStream in, final LastChange lastChange, Device device) throws Exception {
		this.device = device;
		clients = new HashSet<Socket>();
		server = new ServerSocket(0);
				
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
	
	public Device getDevice() {
		return device;
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

	public void stop() {
		// TODO Auto-generated method stub
		
	}
	
}
