package no.ntnu.eit.skeis.central.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerStateUpdate;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand.Commands;

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
	
	
	public PlayerConnection(PlayerManager manager, String alias, InputStream in, OutputStream out) {
		log = Logger.getLogger(getClass().getName());
		this.alias = alias;
		this.in = in;
		this.out = out;
		this.manager = manager;
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
	
}
