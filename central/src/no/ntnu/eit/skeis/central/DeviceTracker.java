package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DeviceTracker
 * 
 * Keeps track of all devices seen by the system and their latest reading
 * for each sensor
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class DeviceTracker implements SensorManager.SensorEventListener {

	private Map<String, Device> devices;
	private SensorManager sensors;
	
	public DeviceTracker(SensorManager sensors) {
		this.sensors = sensors;
		devices = new HashMap<String, Device>();
		

		sensors.addListener(this);
	}

	@Override
	public void onSensorAttach(String alias) {
		for(Device d : devices.values()) {
			d.onSensorAttach(alias);
		}
	}

	@Override
	public void onSensorDetach(String alias) {
		for(Device d : devices.values()) {
			d.onSensorDetach(alias);
		}
		
	}

	@Override
	public void onSensorUpdate(String alias, String mac, int rssi) {
		if (!devices.containsKey(mac)) {
			Device device = new Device(mac, sensors.getSensorAliases());
			devices.put(mac, device);
		}
		devices.get(mac).onSensorUpdate(alias, rssi);	
		System.out.println(this);
	}
	
	public String toString() {
		String out = "Device:\t\t\t";
		Set<String> aliases = sensors.getSensorAliases();
		
		for(String sensor : aliases) {
			out += "| "+sensor+"\t";
		}
		out += "\n";
		
		for(String mac : devices.keySet()) {
			out += mac+"\t";
			for(String sensor : aliases) {
				out += "| "+devices.get(mac).getLastReading(sensor)+"\t";
			}
			out += "\n";
		}
		return out;
	}
	
}
