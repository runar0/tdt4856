package no.ntnu.eit.skeis.central;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.api.ApiServer;
import no.ntnu.eit.skeis.central.devices.PlayerManager;
import no.ntnu.eit.skeis.central.devices.SensorManager;
import no.ntnu.eit.skeis.central.net.Beacon;
import no.ntnu.eit.skeis.central.net.DeviceServerSocket;
import no.ntnu.eit.skeis.central.net.UPNPDeviceServer;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;


public class Central {

	public static final long VERSION = 1;
	private final Logger log;
	
	private final DeviceServerSocket deviceServerSocket;
	private final UPNPDeviceServer upnpDeviceServer;
	private final SensorManager sensor_manager;
	private final PlayerManager player_manager;
	private final DeviceTracker tracker;
	private final ApiServer api_server;
	private final Beacon beacon;
	
	public static void main(String[] args) throws Exception {
		new Central();
	}
	
	public Central() throws Exception {
		log = Logger.getLogger(getClass().getName());
		
		// Basic managers
		sensor_manager = new SensorManager();		
		player_manager = new PlayerManager();
		
		// Device tracker that combines all our data
		tracker = new DeviceTracker(this);	
		
		// JSON API server
		api_server = new ApiServer(12355, this);
		api_server.setDaemon(true);
		api_server.start();
		
		// Device socket for sensors and primitive TCP players
		deviceServerSocket = new DeviceServerSocket(this);
		deviceServerSocket.startServer(12354);
		
		
		// Start Beacon server, this will enable phones and sensors to autodetect the central
		beacon = new Beacon(deviceServerSocket, api_server);
		beacon.start();
		
		// Start UPNP control point
		final UpnpService upnp = new UpnpServiceImpl();
		upnpDeviceServer = new UPNPDeviceServer(this, upnp);
		upnpDeviceServer.start();
		
		// Finally register a shutdown hook that ensures a clean shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Stop servers
				deviceServerSocket.stopServer();
				api_server.stopServer();
				beacon.stopBeacon();
				
				upnpDeviceServer.stopDeviceServer();				
				upnp.shutdown();
				
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
