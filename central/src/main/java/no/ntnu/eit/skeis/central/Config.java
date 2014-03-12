package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import sun.rmi.runtime.Log;

/**
 * This class contains all hard-coded configuration which is needed for a successful
 * prototype but would have been hidden away in beautiful graphical user interfaces in
 * a finalised product
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
abstract public class Config {

	public static final double SENSOR_READING_COEFF = 0.8;
	
	/**
	 * Static mapping between wifi ips and bluetooth mac addresses
	 */
	public static final Map<String, String> ipBMacMapping = new HashMap<String, String>();
	
	public static final int NUM_MEDIA_RENDERERS = 2;
	
	
	static {
		ipBMacMapping.put("192.168.43.69", "98:b8:e3:82:6e:28");
		ipBMacMapping.put("192.168.43.119", "b8:5e:7b:2a:c9:5c");
		ipBMacMapping.put("192.168.43.10", "a8:26:d9:f2:dc:27");
	}
	

	/**
	 * UPNP uuid to alias mapping
	 */
	public static Map<String, String> upnpAliases = new HashMap<String, String>();
	
	static {
		upnpAliases.put("RINCON_B8E93758042E01400", "sonos1");
		upnpAliases.put("RINCON_B8E937581CDC01400", "sonos2");
	}
	
	/**
	 * N values per sensor
	 */
	public static Map<String, Double> sensorNValues = new HashMap<String, Double>();
	
	static {
		sensorNValues.put("runar", 0.2);
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
		deviceSensorAValue.get("a8:26:d9:f2:dc:27").put("runar", 45.0);
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
