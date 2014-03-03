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
	
	
	static {
		ipBMacMapping.put("192.168.0.105", "f8:db:7f:04:a0:71");
	}
}
