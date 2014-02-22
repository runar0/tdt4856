package no.ntnu.eit.skeis.sensor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterRequest;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterRequest.DeviceType;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterResponse;
import no.ntnu.eit.skeis.protocol.device.SensorProtos.SensorUpdate;

/**
 * ServerConnection
 * 
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class CentralConnection {

	private Logger log;
	
	private String client_alias;
	
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	
	public CentralConnection(InetAddress address, int port, String client_alias) throws IOException {
		log = Logger.getLogger("CentralConnection");
		this.client_alias = client_alias;
		
		connect(address, port);
	}
	
	/**
	 * Connect to the central server
	 * 
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	private void connect(InetAddress address, int port) throws IOException {
		log.info("Attempting to reach central at "+address.getHostAddress()+":"+port);
		
		socket = new Socket(address, port);
		in = new BufferedInputStream(socket.getInputStream());
		out = new BufferedOutputStream(socket.getOutputStream());
		
		// Create connection request
		DeviceRegisterRequest request = DeviceRegisterRequest.newBuilder()
				.setDeviceVersion(Sensor.VERSION)
				.setDeviceAlias(client_alias)
				.setDeviceType(DeviceType.SENSOR)
				.build();
		request.writeDelimitedTo(out);
		out.flush();
		
		log.info("Register request sent, waiting for central response");
		
		// Wait for server to respond
		DeviceRegisterResponse response = DeviceRegisterResponse.parseDelimitedFrom(in);
		
		if(response.getStatus() == DeviceRegisterResponse.StatusCodes.OK) {
			log.info("Central accepted our connection: "+response.getStatusMessage());
		} else {
			log.info("Central denied our connection: "+response.getStatusMessage());
		}
	}
	
	/**
	 * Send a sensor update to the central server
	 * 
	 * @param mac
	 * @param rssi
	 * @throws IOException
	 */
	public void sendSensorUpdate(String mac, int rssi) throws IOException {
		SensorUpdate.newBuilder().setUnitMac(mac).setRssi(rssi).build().writeDelimitedTo(out);
		out.flush();
	}
	
}
