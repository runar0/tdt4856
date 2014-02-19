package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Device
 * 
 * Represents a single device, and all recent readings it has.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Device {

	private static final int INFINITY = -90;
	
	private Map<String, Integer> readings;
	
	private String mac;
	
	public Device(String mac, Set<String> sensorAliases) {
		this.mac = mac;
		readings = new HashMap<String, Integer>();
		
		for(String s : sensorAliases) {
			onSensorAttach(s);
		}
	}

	public void onSensorDetach(String alias) {
		readings.remove(alias);
		
	}

	public void onSensorAttach(String alias) {
		readings.put(alias, INFINITY);
		
	}

	public void onSensorUpdate(String alias, int rssi) {
		readings.put(alias, rssi);
	}
	
	public int getLastReading(String alias) {
		return readings.get(alias);
	}

}
