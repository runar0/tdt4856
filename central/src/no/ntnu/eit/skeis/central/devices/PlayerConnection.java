package no.ntnu.eit.skeis.central.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand.Commands;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerStateUpdate;

/**
 * PlayerConnection
 * 
 * Central<->Sensor connection object
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class PlayerConnection {

	private final Logger log;
	private final PlayerManager manager;
	
	private InputStream in;
	private OutputStream out;
	
	private String alias;
	
	private Thread readThread;
	
	/**
	 * Queue of all active devices that are currently registered as closest to this player
	 */
	private Deque<Device> devices;
	
	/**
	 * The device that is currently controlling this player
	 */
	private Device active_device;
	
	
	public PlayerConnection(PlayerManager manager, String alias, InputStream in, OutputStream out) {
		log = Logger.getLogger(getClass().getName());
		this.alias = alias;
		this.in = in;
		this.out = out;
		this.manager = manager;
		devices = new LinkedList<Device>();
		startReadLoop();
	}
	
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Start a read thread that will read player state updates
	 * 
	 */
	private void startReadLoop() {
		final PlayerConnection player = this;
		readThread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						PlayerStateUpdate update = PlayerStateUpdate.parseDelimitedFrom(in);
						
						if (update == null) {
							throw new IOException("Player state update is null");
						}
						
						manager.onStateUpdate(player, update);						
					} catch(IOException ioe) {
						log.info("Excpetion in read loop, dropping player");
						log.warning(ioe.toString());
						manager.removeSensor(alias);
						return;
					}
				}
			}
		};
		readThread.start();
	}
	
	private void sendCommand(PlayerCommand command) {
		try {
			command.writeDelimitedTo(out);
			out.flush();
		} catch(IOException e) {}
	}
	
	public void setUrl(String url) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand(Commands.SET_URL)
			.setUrl(url)
			.build()
		);
	}
	
	public void setPlayState(boolean play) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand((play ? Commands.PLAY : Commands.STOP))
			.build()
		);
	}
	
	public void setVolume(int volume) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand(Commands.SET_VOLUME)
			.setVolume(volume)
			.build()
		);
		
	}
	
	/**
	 * Called after a device has been added or removed to the control queue at this player
	 */
	private void updateActivePlayer() {	
		System.out.println("Player "+alias+": "+devices);
		// TODO Priority can be implemented here, the best way would be to replace the LL with a PQ
		Device d = devices.peekFirst();
		if (d == null) {
			setPlayState(false);
		} else if(!d.equals(active_device)) {
			setUrl(d.getAudioSource().getUrl());
			setPlayState(true);
			setVolume(50);
			active_device = d;
		}
	}

	/**
	 * Register a new device to this player
	 * 
	 * @param device
	 */
	public void registerDevice(Device device) {
		if(!devices.contains(device)) {
			devices.addLast(device);
			device.setPlayerConnection(this);
		}
		updateActivePlayer();
	}

	/**
	 * Unregister a previously registered device
	 * 
	 * @param device
	 */
	public void unregisterDevice(Device device) {
		if(devices.contains(device)) {
			devices.remove(device);
			device.setPlayerConnection(null);
		}
		updateActivePlayer();
	}
	
}
