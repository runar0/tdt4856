package no.ntnu.eit.skeis.central;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.PlayerManager;
import no.ntnu.eit.skeis.central.devices.SensorManager;
import no.ntnu.eit.skeis.central.net.DeviceServerSocket;
import no.ntnu.eit.skeis.central.net.UPNPDeviceServer;
import no.ntnu.eit.skeis.central.upnp.mediarenderer.CentralMediaRenderer;

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
		
		// Start UPNP control point

		final UpnpService upnp = new UpnpServiceImpl();
		upnpDeviceServer = new UPNPDeviceServer(this, upnp);
		upnpDeviceServer.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Stop servers
				deviceServerSocket.stopServer();
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
