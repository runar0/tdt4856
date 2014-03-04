package no.ntnu.eit.skeis.central.audio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.mp3.Frame;

import org.fourthline.cling.support.lastchange.LastChange;

public class StreamingSource implements AudioSource {

	private final Logger log;
	
	private final ServerSocket server;	
	private final Set<Socket> clients;	
	private final Device device;
	
	private final Queue<Frame> frameBuffer;
	
	private final Socket sourceSocket;
	private final InputStream in;
	private final LastChange lastChange;
	
	private boolean drained = false;
	
	/**
	 * Thread that accepts new clients and puts them in the client set
	 * 
	 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
	 */
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
							//System.out.println(line);
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
	
	/**
	 * Thread that will read the source stream as fast as possible and buffer the contents 
	 * in the frameBuffer
	 * 
	 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
	 */
	class SourceReaderThread extends Thread {
		@Override
		public void run() {
			Frame frame;
			try {
				drained = false;
				log.info("Starting stream drain");
				while((frame = Frame.fromInputStream(in)) != null) {
					frameBuffer.add(frame);
					try { Thread.sleep(10); } catch(Exception e) {}
				}
				drained = true;
				log.info("Drained stream");
			} catch(IOException ioe) {
				log.info("Error reading source stream, stopping.");
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (sourceSocket != null) {
						sourceSocket.close();
					}
					in.close();
				} catch(IOException e) {}
			}
		}
	}
	
	/**
	 * A client feeder that will constantly feed all connected clients frame by frame
	 * at a constant rate until no more data is available
	 * 
	 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
	 */
	class ClientFeedThread extends Thread {
		@Override
		public void run() {
			for(int i = 0; i < 20; i++) { try { Thread.sleep(100); } catch(Exception e) {} }
			
			Frame frame;
			long time = -1;
			int count = 0;
			
			while(true) {
				try {
					frame = frameBuffer.remove();
				} catch(NoSuchElementException e) {
					// If we're not under-run delay for a bit and keep going
					if (!drained) {
						for(int i = 0; i < 20; i++) { try { Thread.sleep(100); } catch(Exception ex) {} }
						continue;
					}
					// If we're drained we kill the thread
					log.info("Client feeder buffer underrun!");
					break;
				}
				
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
			
			// Close all client connections if any are still open
			synchronized(clients) {
				Iterator<Socket> it = clients.iterator();
				while(it.hasNext()) {
					try {
						it.next().close();
					} catch(Exception e) {
						// ignore
					}
				}
			}
			
			// If we've reached the end, unregister ourselves with the device
			if(frameBuffer.size() == 0) {
				device.setAudioSource(null);
			}
		}
	}
	
	public StreamingSource(Socket socket, final BufferedInputStream in, final LastChange lastChange, Device device) throws Exception {
		log = Logger.getLogger(getClass().getName());
		this.device = device;
		sourceSocket = socket;
		this.in = in;
		this.lastChange = lastChange;
		frameBuffer = new ConcurrentLinkedQueue<Frame>();
		clients = new HashSet<Socket>();
		server = new ServerSocket(0);
		System.out.println(server.getLocalPort());

		new SourceReaderThread().start();
		new ClientConnectionThread().start();
		new ClientFeedThread().start();

		device.setAudioSource(this);
	}
	
	public static void main(String[] args) throws Exception {
		new StreamingSource(null, new BufferedInputStream(new FileInputStream(new File("/home/runar/84.mp3"))), null, null);
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
		try {
			log.info("Closing stream endpoints");
			// Not really a team player here, close all resources and hope that all our threads die in peace		
			synchronized(clients) {
				log.info("Killing all client connections");
				for(Socket client : clients) {
					client.close();
				}
			}	
			server.close();
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		device.setAudioSource(null);
		log.info("Killed stream");		
	}
	
}
