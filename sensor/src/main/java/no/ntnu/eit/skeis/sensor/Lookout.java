package no.ntnu.eit.skeis.sensor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.BeaconProtos.CentralBeacon;

/**
 * Lookout
 * 
 * The lookout will scout for incoming beacons from the central server and 
 * deduce the central location based on that information
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 *
 */
public class Lookout {
	
	class ConnectionInfo {
		public final InetAddress address;
		public final int sensor_port;
		public ConnectionInfo(InetAddress address, int sensor_port) {
			this.address = address;
			this.sensor_port = sensor_port;
		}
		public String toString() {
			return address.getHostAddress()+" (sensor: "+sensor_port+"/TCP)";
		}
	}
	
	/**
	 * Open a UDP port and wait for a beacon package from a central on the network
	 * 
	 * @return
	 */
	public ConnectionInfo detectCentral() {
		DatagramSocket socket = null;
		Logger log = Logger.getLogger("Lookout");
		
		try {
			socket = new DatagramSocket(12354, InetAddress.getByName("0.0.0.0"));
			socket.setBroadcast(true);
			log.info("Lookout waiting for beacon on port 12354");
			
			byte[] buffer = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			socket.receive(packet);
			
			log.info("Received beacon package from "+packet.getAddress().getHostAddress());
			
			CentralBeacon beacon = CentralBeacon.parseDelimitedFrom(new ByteArrayInputStream(buffer));
			
			return new ConnectionInfo(packet.getAddress(), beacon.getSensorPort());			
		} catch(IOException e) {
		} finally {
			if(socket != null) {
				socket.close();
			}
		}
		return null;
	}
	
}
