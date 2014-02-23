package no.ntnu.eit.skeis.central;


// SEnsor manager -> flytt socket dit, listener events
// DEvice tracker -> lytter på sensor updates, har en device per observerte device
// DEvice lytter på sensor attaches/detachers oppdaterer intern state


import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.PlayerManager;
import no.ntnu.eit.skeis.central.devices.SensorManager;

public class Central {

	public static final long VERSION = 1;
	private final Logger log;
	
	private final DeviceServerSocket deviceServerSocket;
	private final SensorManager sensor_manager;
	private final PlayerManager player_manager;
	private final DeviceTracker tracker;
	
	public static void main(String[] args) throws Exception {
		new Central();
	}
	
	public Central() throws Exception {
		log = Logger.getLogger(getClass().getName());
		
		sensor_manager = new SensorManager();		
		player_manager = new PlayerManager(this);		
		tracker = new DeviceTracker(this);		
		deviceServerSocket = new DeviceServerSocket(this);
		deviceServerSocket.startServer(12354);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Stop device server
				deviceServerSocket.stopServer();
				
				// TODO Sensor manager, 1. stop server socket, 2. notify all sensors
				// TODO player manager --"--

				log.info("Stopping");
			}
		});
	}
	
	public DeviceTracker getDeviceTracker() {
		return tracker;
	}
	
	public SensorManager getSensorManager() {
		return sensor_manager;
	}
	
	public PlayerManager getPlayerManager() {
		return player_manager;
	}
}
