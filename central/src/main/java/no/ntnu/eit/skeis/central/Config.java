package no.ntnu.eit.skeis.central;

import java.net.Inet4Address;
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

	public static final double SENSOR_READING_COEFF = 0.75;
	
	public static final boolean ONLY_KNOWN_DEVICES = true;
	
	/**
	 * We experience quite a bit of delay when getting the ip at times, causing issues with speaker switching.
	 * Hence we store it here
	 */
	public static String IP;
	
	static {
		try {
			IP  = Inet4Address.getLocalHost().getHostAddress();
		} catch(Exception e) {
			System.err.println("Cannot detect central IP!");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Static mapping between wifi ips and bluetooth mac addresses
	 */
	public static final Map<String, String> ipBMacMapping = new HashMap<String, String>();

	public static final Map<String, String> deviceAliases = new HashMap<String, String>();
	
	/**
	 * Number of media renderer endpoints to expose
	 */
	public static final int NUM_MEDIA_RENDERERS = 2;
	

	/**
	 * UPNP uuid to alias mapping
	 */
	public static Map<String, String> upnpAliases = new HashMap<String, String>();
	
	static {
		// Our two SONOS PLAY:1's
		upnpAliases.put("RINCON_B8E93758042E01400", "left");
		upnpAliases.put("RINCON_B8E937581CDC01400", "right");
		
		// Add static routes
		ipBMacMapping.put("192.168.1.201", "f8:db:7f:04:a0:71");
		deviceAliases.put("f8:db:7f:04:a0:71", "desire");
		ipBMacMapping.put("192.168.1.200", "a8:26:d9:f2:dc:27");
		deviceAliases.put("a8:26:d9:f2:dc:27", "one");		
		ipBMacMapping.put("192.168.1.202", "cc:fa:00:58:03:81");
		deviceAliases.put("cc:fa:00:58:03:81", "nexus5");
	}
	
	/**
	 * N values per sensor
	 */
	public static Map<String, Double> sensorNValues = new HashMap<String, Double>();
	
	static {
		sensorNValues.put("right", 0.2);
		sensorNValues.put("left", 0.2);
	}
	
	public static double getSensorNValue(String sensor) {
		if(sensorNValues.containsKey(sensor)) {
			return sensorNValues.get(sensor);
		}
		//Logger.getLogger(Config.class.getName()).info("Missing N value for sensor "+sensor+"!");
		return 0.2;
	}
	
	/**
	 * N values per sensor
	 */
	public static Map<String, Map<String, Double>> deviceSensorAValue = new HashMap<String, Map<String, Double>>();
	
	static {
		deviceSensorAValue.put("a8:26:d9:f2:dc:27", new HashMap<String, Double>());
		deviceSensorAValue.get("a8:26:d9:f2:dc:27").put("left", 67.0);
		deviceSensorAValue.get("a8:26:d9:f2:dc:27").put("right", 58.0);
		
		deviceSensorAValue.put("cc:fa:00:58:03:81", new HashMap<String, Double>());
		deviceSensorAValue.get("cc:fa:00:58:03:81").put("left", 67.0);
		deviceSensorAValue.get("cc:fa:00:58:03:81").put("right", 58.0);
	}
	
	public static double getClientAValue(String sensor, String device) {
		if(deviceSensorAValue.containsKey(device)) {
			Map<String, Double> map = deviceSensorAValue.get(device);
			if(map.containsKey(sensor)) {
				return map.get(sensor);
			}
			//Logger.getLogger(Config.class.getName()).info("Missing A value for device "+device+" for sensor "+sensor+"!");
			return 45;
		}
		//Logger.getLogger(Config.class.getName()).info("Missing A value for device "+device+"!");
		return 45;
	}
}
