package no.ntnu.eit.skeis.central.devices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
public class PlayerConnection extends AbstractPlayer {

	private InputStream in;
	private OutputStream out;
	
	private Thread readThread;
	
	public PlayerConnection(PlayerManager manager, String alias, InputStream in, OutputStream out) {
		super(manager, alias);
		
		this.in = in;
		this.out = out;
		startReadLoop();
	}
	
	/**
	 * Start a read thread that will read player state updates
	 * 
	 */
	private void startReadLoop() {
		final PlayerInterface player = this;
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
						manager.removePlayer(alias);
						return;
					}
				}
			}
		};
		readThread.start();
	}
	
	/**
	 * Send a command 
	 * 
	 * @param command
	 */
	private void sendCommand(PlayerCommand command) {
		try {
			command.writeDelimitedTo(out);
			out.flush();
		} catch(IOException e) {}
	}
	
	/* (non-Javadoc)
	 * @see no.ntnu.eit.skeis.central.devices.PlayerInterface#setUrl(java.lang.String)
	 */
	@Override
	public void setUrl(String url) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand(Commands.SET_URL)
			.setUrl(url)
			.build()
		);
	}
	
	/* (non-Javadoc)
	 * @see no.ntnu.eit.skeis.central.devices.PlayerInterface#setPlayState(boolean)
	 */
	@Override
	public void setPlayState(boolean play) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand((play ? Commands.PLAY : Commands.STOP))
			.build()
		);
	}
	
	/* (non-Javadoc)
	 * @see no.ntnu.eit.skeis.central.devices.PlayerInterface#setVolume(int)
	 */
	@Override
	public void setVolume(int volume) {
		sendCommand(PlayerCommand.newBuilder()
			.setCommand(Commands.SET_VOLUME)
			.setVolume(volume)
			.build()
		);
		
	}
	
}
