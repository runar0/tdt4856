package no.ntnu.eit.skeis.central;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.BeaconProtos.CentralBeacon;

/**
 * Beacon
 * 
 * The beacon thread will periodically send UDP broadcast packages on all network
 * interfaces, enabling sensors and playback devices to detect the central automatically.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Beacon extends Thread {

	/**
	 * Time in ms between beacon packages
	 */
	private static final long BEACON_GAP = 5*1000;
	
	private boolean running;
	private DeviceServerSocket server;
	
	private Logger log;
	
	public Beacon(DeviceServerSocket server) {
		this.server = server;
		log = Logger.getLogger(getClass().getName());
	}
	
	public void stopBeacon() {
		running = false;
	}
	
	@Override
	public void run() {
		running = true;
		
		log.info("Beacon thread starting");
		
		long lastBeacon = System.currentTimeMillis()-BEACON_GAP;
		while(running) {
			while(running && (System.currentTimeMillis() - lastBeacon) < BEACON_GAP) {
				try {
					Thread.sleep(1000);
				} catch(InterruptedException e) {}
			}
			if (!running) continue;
			
			DatagramSocket socket = null;
			try {
				CentralBeacon beaconPackage = CentralBeacon.newBuilder()
						.setServerVersion(Central.VERSION)
						.setSensorPort(server.getPort())
						.setPlayerPort(server.getPort())
						.build();
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				beaconPackage.writeDelimitedTo(data);
						
				socket = new DatagramSocket();
				socket.setBroadcast(true);
				
				List<InetAddress> broadcasts = new LinkedList<InetAddress>();
				
				// Add global broadcast address
				try {
					broadcasts.add(InetAddress.getByName("255.255.255.255"));
				} catch(UnknownHostException e) {}
				
				// Add all broadcast addresses for all known links
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				while(interfaces.hasMoreElements()) {
					NetworkInterface networkInterface = interfaces.nextElement();
					if(networkInterface.isLoopback() || !networkInterface.isUp()) {
						continue; // Skip loopback and interfaces that are down
					}
					for(InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
						broadcasts.add(address.getBroadcast());
					}
				}
				
				// Send the beacon package on all braodcast addresses we got
				for(InetAddress address : broadcasts) {
					try {
						socket.send(
							new DatagramPacket(
								data.toByteArray(), 
								data.toByteArray().length, 
								address,
								12354
							)
						);
						//log.info("Beacon package sent to "+address.getHostAddress());
					} catch(Exception e) {
						// Ignore
					}
				}
				
				
			} catch(SocketException e) {
				log.warning("SocketException in beacon send block");
				log.warning(e.toString());
			} catch(IOException e) {
				log.warning("IOException in beacon send block");
				log.warning(e.toString());
			} finally {
				lastBeacon = System.currentTimeMillis();
				
				if(socket != null) {
					socket.close();
				}
			}
		}
		log.info("Beacon thread stopping");
	}
	
}
