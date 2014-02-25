package no.ntnu.eit.skeis.central.net;

import java.util.HashMap;
import java.util.Map;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.devices.player.PlayerUPNP;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

/**
 * UPNP Device server
 * 
 * This is a UPNP control point implementation that will scan the local network
 * looking for supported services. 
 * 
 * Currently we support AVTransport services for audio playback
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 *
 */
public class UPNPDeviceServer extends Thread {

	/**
	 * UPNP uuid to alias mapping
	 */
	private static Map<String, String> aliases;
	
	static {
		aliases = new HashMap<String, String>();
		// XBMC runar-archy
		aliases.put("3cdbb3d4-8c1a-d6f0-494f-93fa97d93337", "runar");
		aliases.put("09527f1f-ab89-bf64-0000-000005ff505c", "runar");
	}

	private static String getUUIDAlias(String uuid) {
		String alias = aliases.get(uuid);
		if(alias == null) {
			alias = uuid;
		}
		return alias;
	}
	
	private final Central central;
	private boolean running;
	
	/**
	 * Service ID for playback devices
	 */
	private final ServiceId serviceId = new UDAServiceId("AVTransport");
	
	public UPNPDeviceServer(Central central) {
		this.central = central;
	}
	
	/**
	 * Start UPNP server
	 */
	@Override
	public void run() {
		running = true;
		final UpnpService upnp = new UpnpServiceImpl(); 
		
		// Add listener for device add and removed
		upnp.getRegistry().addListener(new DefaultRegistryListener() {
			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				String alias = getUUIDAlias(device.getIdentity().getUdn().getIdentifierString());
				RemoteService service = device.findService(serviceId);
				if(service != null) {
					central.getPlayerManager().addPlayer(
						alias,
						new PlayerUPNP(central.getPlayerManager(), alias, upnp, service)
					);
				}
			}
			
			@Override
			public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
				String alias = getUUIDAlias(device.getIdentity().getUdn().getIdentifierString());
				RemoteService service = device.findService(serviceId);
				if(service != null) {
					central.getPlayerManager().removePlayer(alias);
				}
			}
		});
		
		// Keep scanning for devices
		while(running) {
			try {
				if(!running) continue;
				
				upnp.getControlPoint().search(
	                new STAllHeader()
	            );
				
				Thread.sleep(30000);
			} catch(InterruptedException e) {
				
			}
		}
		
		// If we reach this point we're about to kill the server
		upnp.shutdown();
	}
	
	/**
	 * Kill server
	 */
	public void stopDeviceServer() {
		running = false;
	}
	
}
