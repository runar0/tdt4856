package no.ntnu.eit.skeis.vlcplayer;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterRequest;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterRequest.DeviceType;
import no.ntnu.eit.skeis.protocol.DeviceProtos.DeviceRegisterResponse;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerStateUpdate;

/**
 * CentralConnection
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class CentralConnection extends Thread {

	interface CommandListener {
		public void onCommand(PlayerCommand command);
	}
	
	private boolean running;
	
	private Logger log;	
	private String client_alias;	
	private CommandListener listener;
	
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	
	public CentralConnection(String client_alias, CommandListener listener) throws IOException {
		log = Logger.getLogger("CentralConnection");
		this.client_alias = client_alias;
		this.listener = listener;
	}
	
	/**
	 * Connect to the central server
	 * 
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public void connect(InetAddress address, int port) throws IOException {
		log.info("Attempting to reach central at "+address.getHostAddress()+":"+port);
		
		socket = new Socket(address, port);
		in = new BufferedInputStream(socket.getInputStream());
		out = new BufferedOutputStream(socket.getOutputStream());
		
		// Create connection request
		DeviceRegisterRequest request = DeviceRegisterRequest.newBuilder()
				.setDeviceVersion(VLCPlayer.VERSION)
				.setDeviceAlias(client_alias)
				.setDeviceType(DeviceType.PLAYER)
				.build();
		request.writeDelimitedTo(out);
		out.flush();
		
		log.info("Register request sent, waiting for central response");
		
		// Wait for server to respond
		DeviceRegisterResponse response = DeviceRegisterResponse.parseDelimitedFrom(in);
		
		if(response.getStatus() == DeviceRegisterResponse.StatusCodes.OK) {
			log.info("Central accepted our connection: "+response.getStatusMessage());
			setDaemon(false);
			start();
		} else {
			log.info("Central denied our connection: "+response.getStatusMessage());
		}
	}
	
	public void stopConnection() {
		running = false;
	}
	
	public void sendStatus(PlayerStateUpdate state) {
		try {
			state.writeDelimitedTo(out);
		} catch(IOException e) {
			log.warning("Unable to send status update");
		}
	}
	
	@Override
	public void run() {
		running = true;
		try {
			while(running) {
				PlayerCommand command = PlayerCommand.parseDelimitedFrom(in);
				if(!running) continue;
				if (command == null) {
					throw new IOException("Unable to read command from server");		
				}
				listener.onCommand(command);
			}
		} catch(IOException e) {
			if (running) {
				log.warning("Exception in read loop");
				log.warning(e.toString());
			}
		}
		try {
			in.close();
			out.close();
			socket.close();
		} catch(IOException e) {}
	}
	
}
