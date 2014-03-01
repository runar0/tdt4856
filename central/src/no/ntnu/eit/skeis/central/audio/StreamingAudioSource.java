package no.ntnu.eit.skeis.central.audio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Audio source implementation that acts as a bridge between a source audio stream
 * and up to multiple targets
 *  
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class StreamingAudioSource extends Thread implements AudioSource {

	private class ClientConnectorThread extends Thread {
		@Override
		public void run() {
			try {
				outer: while(true) {
					Socket socket = serverSocket.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					System.out.println("Accepted streaming client");
					String line;
					while((line = in.readLine()) != null && !line.trim().equals("")) {
						if(line.startsWith("GET") && !line.startsWith("GET / ")) {
							socket.close();
							continue outer;
						}
						System.out.println(line);
					}
					
					socket.getOutputStream().write((
						"HTTP/1.1 200\r\n"+
						"SERVER: UPnP/1.0 SKEIS/1.0\r\n"+
						"CACHE-CONTROL: no-cache\r\n"+
						"Content-Type: audio/mp3\r\n"+
						"Content-Range:bytes 0-100000000\r\n"+
						"contentFeatures.dlna.org:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01;DLNA.ORG_FLAGS=01700000000000000000000000000000\r\n"+
						"transferMode.dlna.org:Streaming\r\n"+
						"\r\n"
					).getBytes());
					socket.getOutputStream().flush();
					
					synchronized(clients) {
						clients.add(socket);
					}
				}
			} catch(IOException ioe) {
				
			}
		}
	}
	
	private URI sourceURI;
	
	private final ServerSocket serverSocket;
	private final Set<Socket> clients;
	
	public StreamingAudioSource(URI sourceURI) throws IOException {
		this.sourceURI = sourceURI;
		
		serverSocket = new ServerSocket(0);
		clients = new HashSet<Socket>();
		
		start();
	}
	
	/**
	 * Get source url for a http based playback device
	 */
	@Override
	public String getHttpUrl() {
		return "http://"+serverSocket.getInetAddress().getHostAddress()+":"+serverSocket.getLocalPort();
	}

	/**
	 * Get source url for sonos players
	 */
	@Override
	public String getSonosUrl() {
		return "x-rincon-mp3radio://"+serverSocket.getInetAddress().getHostAddress()+":"+serverSocket.getLocalPort();
	}
	
	@Override
	public void run() {
		Socket socket = null;
		try {
			socket = new Socket(sourceURI.getHost(), sourceURI.getPort());
			socket.getOutputStream().write(
				("GET "+sourceURI.getPath()+" HTTP/1.1\r\n" +
				"Host: "+sourceURI.getHost()+":"+sourceURI.getPort()+"\r\n\r\n").getBytes()
			);
			socket.getOutputStream().flush();
			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
			
			// Read up until the first sync frame
			int state = 0;
			int b;
			while(state < 2 && (b = in.read()) != -1) {
				if((state == 0 && b == 0xFF) || (state == 1 && (b&0xF0) == 0xF0)) {
					//System.out.println("Found initial sync byte");
					state++;
				} else {
					//System.out.println("Initial scan reset");
					state = 0;
				}
			}
			
			new ClientConnectorThread().start();
			System.out.println(getHttpUrl());
			
			// Read one MPEG frame from source, and distribute it to all current clients, repeat until end of stream
			byte[] buffer = new byte[1024*10];
			int offset = 0, read = 0;
			while((read = in.read(buffer, offset, buffer.length-offset)) != -1) {
				read += offset;
				offset = 0;
				boolean scan = true;
				while(scan) {
					// Locate next sync header
					int i;
					for(i = offset, state = 0; i < offset+read && state < 2; i++) {
						if((state == 0 && (buffer[i]&0xFF) == 0xFF) || (state == 1 && (buffer[i]&0xF0) == 0xF0)) {
							//System.out.println("Found next sync byte");
							state++;
						} else {
							//System.out.println("Next sync scan reset");
							state = 0;
						}
					}
					if(state == 2) {
						// We found a sync header, data to send is between offset and i - 2 + 2 sync bytes
						synchronized(clients) {
							Iterator<Socket> it = clients.iterator();
							while(it.hasNext()) {
								Socket client = it.next();
								try {
									client.getOutputStream().write(new byte[]{buffer[i-2], buffer[i-1]});
									client.getOutputStream().write(buffer, offset, i-offset-2);
									client.getOutputStream().flush();
								} catch(IOException e) {
									System.out.println("Client dropped, removing");
									it.remove();
								}
								
							}
						}
						//System.out.println("Sent one frame, size: "+(i-offset)+" old offset: "+offset+" old read: "+read+" new read: "+(read-i+offset)+" new offset: "+i);
						read -= i-offset;
						offset = i;
					} else {
						// Reposition buffer data and repeat outer loop
						//System.out.println("Reposition: Before: offset="+offset+ " read="+read);
						System.arraycopy(buffer, offset, buffer, 0, read);
						offset = read;
						read = 0;
						//System.out.println("Reposition: After: offset="+offset+ " read="+read);
						scan = false;
						//try { Thread.sleep(10000); } catch(Exception e) {}
					}
				}
			}
			
			
			
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
