package no.ntnu.eit.skeis.vlcplayer;


import java.net.InetAddress;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerCommand;
import no.ntnu.eit.skeis.vlcplayer.Lookout.ConnectionInfo;

/**
 * Sensor emulation
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class VLCPlayer implements CentralConnection.CommandListener {

	final Logger log;
	
	public volatile boolean running;
	
	private final static VLC vlc = new VLC();
	
	/**
	 * Client version. 
	 * 
	 * Increment after a change that will break backwards compatibility to prevent
	 * hard to detect bugs in VLCPLayer<->Central communication.
	 */
	public final static long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		if(args.length != 1 && args.length != 3) {
			System.out.println("Usage: vlcplayer client-alias [central-ip central-port]");
			System.exit(1);
		}
		vlc.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				vlc.stop();
			}
		});
		
		try {
			while(true) {
				VLCPlayer player;
				InetAddress address;
				int port;
				String alias = args[0];
				if (args.length == 1) {					
					Lookout lookout = new Lookout();
					ConnectionInfo info = lookout.detectCentral();
					Logger.getGlobal().info("Central detected "+info.toString());
					
					address = info.address;
					port = info.sensor_port;
				} else {
					address = InetAddress.getByName(args[1]);
					port = Integer.parseInt(args[2]);
				}
				player = new VLCPlayer(address, port, alias);
				
				while(player.running) {
					try {
						Thread.sleep(1000);
					} catch(Exception e) {
						
					}
				}
				Logger.getGlobal().info("Player exited cleanly, restarting!");				
			}
		} catch(Exception e) {
			Logger.getGlobal().warning("Exception escaped player, exitting.");
			e.printStackTrace();
		}
	}
	
	private CentralConnection central;
	
	/**
	 * 
	 * @throws Exception
	 */
	public VLCPlayer(InetAddress address, int port, String alias) throws Exception {
		log = Logger.getLogger(getClass().getName());	
		
		central = new CentralConnection(alias, this);
		central.connect(address, port);
		running = true;
	}

	@Override
	public void onCommand(PlayerCommand command) {
		switch(command.getCommand()) {
		case SET_VOLUME:
			vlc.setVolume(command.getVolume());
			break;
		case PLAY:
			vlc.start();
			break;
		case SET_URL:
			vlc.setUrl(command.getUrl());
			break;
		case SET_URL_AND_PLAY:
			vlc.setUrl(command.getUrl());
			break;
		case STOP:
			vlc.stop();
			break;
		}
		
	}

}
