package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all hard-coded configuration which is needed for a successful
 * prototype but would have been hidden away in beautiful graphical user interfaces in
 * a finalised product
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
abstract public class Config {

	/**
	 * Static mapping between wifi ips and bluetooth mac addresses
	 */
	public static final Map<String, String> ipBMacMapping = new HashMap<String, String>();
	
	public static final int NUM_MEDIA_RENDERERS = 2;
	
	
	static {
		ipBMacMapping.put("192.168.0.105", "f8:db:7f:04:a0:71");
		ipBMacMapping.put("192.168.0.124", "a8:26:d9:f2:dc:27");
	}
	

	/**
	 * UPNP uuid to alias mapping
	 */
	public static Map<String, String> upnpAliases = new HashMap<String, String>();
	
	static {
		upnpAliases.put("RINCON_B8E93758042E01400", "sonos1");
		upnpAliases.put("RINCON_B8E937581CDC01400", "sonos2");
	}
	
}