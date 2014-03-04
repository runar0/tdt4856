package no.ntnu.eit.skeis.central.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.devices.sensor.SensorConnection;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterRequest;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterResponse;

/**
 * DeviceServerSocket
 * 
 * This thread will open a TCP connection on a given port and accept connections
 * from both sensors and players. After doing basic version validation the connection
 * objects, SensorConnection or PlayerConnection, to the correct manager object.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 *
 */
public class DeviceServerSocket extends Thread {

	private boolean running;
	
	private Central central;
	private int port;
	
	private Beacon beacon;
	
	private Logger log;
	
	public DeviceServerSocket(Central central) {
		this.central = central;
		log = Logger.getLogger(getName());
	}
	
	/**
	 * Get port number for incoming connections
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}
	
	private ServerSocket ss;
	
	public void startServer(int port) throws IOException {
		this.port = port;
		ss = new ServerSocket(port);		
		log.info("Active on port "+port+"/TCP");
		
		// Start client accepting thread, make sure its not a daemon
		setDaemon(false);
		start();
		
		// Start beacon for device auto discover
		beacon = new Beacon(this);
		beacon.start();
	}
	
	public void stopServer() {
		running = false;
		beacon.stopBeacon();
		try {
			if (ss != null) {
				ss.close();
			}
		} catch (IOException e) {}
		log.info("Stopping Device Server");
	}
	
	/**
	 * 
	 */
	@Override
	public void run() {
		running = true;
		Socket s;
		
		while(running) {
			s = null;
			try {	
				// Block waiting for a connection, when we return make sure we're still running
				s = ss.accept();
				if(!running) {
					s.close();
					continue;
				}
				
				log.info("Incoming device connection");
				InputStream in = new BufferedInputStream(s.getInputStream());
				OutputStream out = new BufferedOutputStream(s.getOutputStream());				
				
				DeviceRegisterRequest request = DeviceRegisterRequest.parseDelimitedFrom(in);
				
				// We only accept devices with the same version number
				if(request.getDeviceVersion() == Central.VERSION) {
					DeviceRegisterResponse.newBuilder()
							.setStatus(DeviceRegisterResponse.StatusCodes.OK)
							.setServerVersion(Central.VERSION)
							.setStatusMessage("Welcome!")
							.build().writeDelimitedTo(out);
					out.flush();
				
					// Hand device to the correct manager
					switch(request.getDeviceType()) {
					case PLAYER:
//						central.getPlayerManager().addPlayer(
//							request.getDeviceAlias(),
//							new PlayerConnection(
//								central.getPlayerManager(), 
//								request.getDeviceAlias(),
//								in, 
//								out
//							)
//						);
						break;
					case SENSOR:
						central.getSensorManager().addSensor(
							request.getDeviceAlias(), 
							new SensorConnection(
								central.getSensorManager(), 
								request.getDeviceAlias(), 
								in, 
								out
							)
						);
						break;
					}
				} else {
					DeviceRegisterResponse.newBuilder()
						.setStatus(DeviceRegisterResponse.StatusCodes.DENIED)
						.setServerVersion(Central.VERSION)
						.setStatusMessage("Version missmatch!")
						.build().writeDelimitedTo(out);
					out.flush();
				}
			} catch(IOException e) {
				log.warning("Exception while handling device connection");
				if(s != null) {
					try {
						s.close();
					} catch (IOException ioe) {}
				}
			}
					
		}
	}
	
}
