package no.ntnu.eit.skeis.central.net;

import java.util.HashMap;
import java.util.Map;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.devices.player.PlayerSonos;
import no.ntnu.eit.skeis.central.devices.player.PlayerUPNP;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;

import de.kalass.sonoscontrol.api.control.ExecutionMode;
import de.kalass.sonoscontrol.api.control.SonosDevice;
import de.kalass.sonoscontrol.api.control.SonosDeviceCallback;
import de.kalass.sonoscontrol.clingimpl.core.SonosControlClingImpl;

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
		aliases.put("c5121ef4-05bf-8d71-ae30-e21f2ad03850", "runar-htpc");
		
		aliases.put("RINCON_B8E93758042E01400", "sonos1");
		aliases.put("RINCON_B8E937581CDC01400", "sonos2");
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
	private final UpnpService upnp;
	
	/**
	 * Service ID for playback devices
	 */
	private final ServiceId serviceId = new UDAServiceId("AVTransport");
	
	public UPNPDeviceServer(Central central, UpnpService upnp) {
		this.central = central;
		this.upnp = upnp;
	}
	
	/**
	 * Start UPNP server
	 */
	@Override
	public void run() {
		running = true;
		
		// Add listener for device add and removed
		upnp.getRegistry().addListener(new DefaultRegistryListener() {
			@Override
			public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
				// RINCON_* are Sonos devices, these are handled elsewhere and hence ignored here
				if (device.getIdentity().getUdn().getIdentifierString().startsWith("RINCON_")) {
					return;
				}
				
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
		
		// Start a sonos control instance
		SonosControlClingImpl sonos = new SonosControlClingImpl(upnp);
		sonos.executeOnAnyZone(new SonosDeviceCallback() {
			
			@Override
			public ExecutionMode execute(SonosDevice device) {
				String alias = null;
				if(device.getDeviceId().getValue().equals("RINCON_000E586D336E01400")) {
					// Bridge TODO Autodetect bridge by name
					System.out.println(device);
					return ExecutionMode.EACH_DEVICE_DETECTION;
				}
				alias = getUUIDAlias(device.getDeviceId().getValue());
				central.getPlayerManager().addPlayer(
					alias, 
					new PlayerSonos(central.getPlayerManager(), alias, device)
				);
				return ExecutionMode.EACH_DEVICE_DETECTION;
			}
		});
		
		// Keep scanning for devices at regular intervals
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
	}
	
	/**
	 * Kill server
	 */
	public void stopDeviceServer() {
		running = false;
	}
	
}
