package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.ntnu.eit.skeis.central.devices.SensorManager;

/**
 * DeviceTracker
 * 
 * Keeps track of all devices seen by the system and their latest reading
 * for each sensor
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class DeviceTracker implements SensorManager.SensorEventListener, Device.DeviceListener {

	private final Map<String, Device> devices;
	private final Central central;
	
			
	public DeviceTracker(Central central) {
		this.central = central;
		devices = new HashMap<String, Device>();
		
		central.getSensorManager().addListener(this);
		
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
			Device device = new Device(mac, central.getSensorManager().getSensorAliases(), this);
			devices.put(mac, device);
		}
		devices.get(mac).onSensorUpdate(alias, rssi);
	}
	
	public String toString() {
		String out = "Device:\t\t\t";
		Set<String> aliases = central.getSensorManager().getSensorAliases();
		
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

	/**
	 * Called by a device when it has detected that it has a new closest sensor
	 */
	@Override
	public void onDeviceNewClosest(Device device, String sensor_alias) {
		if (!device.isActive()) {
			return;
		}
		// Let the player manager know about the new device position
		central.getPlayerManager().updateDevicePosition(sensor_alias, device);
	}

	/**
	 * Called by a device when its active status has changed
	 */
	@Override
	public void onActiveStatusChange(Device device, String sensor_alias) {
		if(device.isActive()) {
			onDeviceNewClosest(device, sensor_alias); 
		} else {
			central.getPlayerManager().removeDevice(device);
		}
		
	}

	/**
	 * Get all devices closest to the sensor given by alias
	 * 
	 * @param alias
	 * @return
	 */
	public List<Device> getDevicesClosestTo(String alias) {
		List<Device> devices = new LinkedList<Device>();
		for(Device device : this.devices.values()) {
			if(device.getClosestSensor().equals(alias)) {
				devices.add(device);
			}
		}
		return devices;
	}

	/**
	 * Get device by id, or null if unknown device
	 * 
	 * @param mac
	 * @return
	 */
	public Device getDevice(String mac) {
		return devices.get(mac);
	}
	
}
