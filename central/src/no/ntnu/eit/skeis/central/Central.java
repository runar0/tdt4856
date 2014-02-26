package no.ntnu.eit.skeis.central;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.PlayerManager;
import no.ntnu.eit.skeis.central.devices.SensorManager;
import no.ntnu.eit.skeis.central.devices.player.PlayerSonos;
import no.ntnu.eit.skeis.central.net.DeviceServerSocket;
import de.kalass.sonoscontrol.api.control.ExecutionMode;
import de.kalass.sonoscontrol.api.control.SonosDevice;
import de.kalass.sonoscontrol.api.control.SonosDeviceCallback;
import de.kalass.sonoscontrol.api.core.Callback0;
import de.kalass.sonoscontrol.api.model.avtransport.AVTransportURI;
import de.kalass.sonoscontrol.api.services.AVTransportService;
import de.kalass.sonoscontrol.cli.commands.CliCommandResultCallback;
import de.kalass.sonoscontrol.clingimpl.core.SonosControlClingImpl;

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
		
		// Start UPNP control point
		//UPNPDeviceServer upnpDeviceServer = new UPNPDeviceServer(this);
		//upnpDeviceServer.start();
		
		// TODO Make UPNPDeviceServer and SonosControlClingImpl share a UpnpService instance
		SonosControlClingImpl sonos = new SonosControlClingImpl();
		sonos.executeOnAnyZone(new SonosDeviceCallback() {
			
			@Override
			public ExecutionMode execute(SonosDevice device) {
				String alias = null;
				if(device.getDeviceId().getValue().equals("RINCON_000E586D336E01400")) {
					// Bridge
					return ExecutionMode.EACH_DEVICE_DETECTION;
				} else if(device.getDeviceId().getValue().equals("RINCON_B8E93758042E01400")) {
					alias = "jon";
				} else if(device.getDeviceId().getValue().equals("RINCON_B8E937581CDC01400")) {
					alias = "runar";
				}
				player_manager.addPlayer(
					alias, 
					new PlayerSonos(player_manager, alias, device)
				);
				return ExecutionMode.EACH_DEVICE_DETECTION;
			}
		});
		
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
