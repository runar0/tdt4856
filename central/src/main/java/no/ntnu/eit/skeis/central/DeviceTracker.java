package no.ntnu.eit.skeis.central;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import no.ntnu.eit.skeis.central.devices.PlayerManager.PlayerEventListener;
import no.ntnu.eit.skeis.central.devices.SensorManager;
import no.ntnu.eit.skeis.central.devices.player.PlayerInterface;

/**
 * DeviceTracker
 * 
 * The device tracker is notified by the Sensor Manager every time a sensor sends a update,
 * and based on that information keeps a updated list of device entries. A background thread is
 * started that at fixed intervals calculate which sensor each device is closest to. 
 * 
 * Internally the device tracker keeps a sorted list of devices per sensor, where the top device
 * that is considered to be the controlling device and the related player instance is notified of this.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class DeviceTracker implements SensorManager.SensorEventListener, Device.DeviceListener, PlayerEventListener {

	/**
	 * A device entry in the sensor queue
	 * 
	 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
	 */
	public class DeviceSensorEntry implements Comparable<DeviceSensorEntry> {

		/**
		 * Time when the entry was added
		 */
		public long timestamp = 0;
		
		/**
		 * The device's priority at the given sensor, higher is better
		 */
		public int priority = 0;

		/**
		 * Related device
		 */
		public Device device;
		
		/**
		 * Compare two device entries
		 * 
		 * They are ordered by three rules:
		 *  1 - If one is active, it is considered to be less than the other
		 *  2 - If one has higher priority than an other, it is considered to be lesser
		 *  3 - If all else fails the entry time stamps is used, here the oldest one is the lesser one
		 */
		@Override
		public int compareTo(DeviceSensorEntry other) {
			// If this is active but other is not, this is greater
			if(this.device.isActive() && !other.device.isActive()) {
				return -1;
			} 
			// If other is active and this is not, other wins
			else if(!this.device.isActive() && other.device.isActive()) {
				return 1;
			}
			// If priorities do not match, the one with the greater priority is greater
			if(this.priority != other.priority) {
				return other.priority - this.priority;
			}
			
			// Finally we sort by time stamp, here lesser is better
			return (int)(this.timestamp - other.timestamp);			
		}
		
		/**
		 * For debugging
		 */
		public String toString() {
			return device.toString() + "(" + (device.isActive() ? "A" : "-") + "-P:"+priority+")";
		}
		
	}
	
	/**
	 * All known devices, indexed by their bluetooth mac address
	 */
	private final Map<String, Device> devices;
	
	/**
	 * A map of sorted sets, one for each known sensor. Within each sorted set each device that is registered as closest
	 * to the related sensor is listed.
	 */
	private final Map<String, SortedSet<DeviceSensorEntry>> sensorRegistrations;
	
	/**
	 * This flag is set to true when a re-evaluation of the device queues has to happen,
	 * this is caused by a new device, device moving, device changing closest sensor,
	 * sensors or player added.
	 * 
	 */
	private volatile boolean devicesChanged = false;
	
	private final Central central;
	
			
	public DeviceTracker(final Central central) {
		this.central = central;
		devices = new HashMap<String, Device>();
		sensorRegistrations = Collections.synchronizedMap(new HashMap<String, SortedSet<DeviceSensorEntry>>());
		
		central.getSensorManager().addListener(this);
		
		/**
		 * Start the device tracker update thread that will trigger a distance update for all devices
		 * and if required re-evaluates the sensor registration queues
		 */
		Thread thread = new Thread("DeviceUpdate") {
			public void run() {
				while(true) {
					try {
						// Have all devices update their state
						/*for(Device d : devices.values()) {
							d.updateClosestSensor();
						}*/
						
						if(devicesChanged) {
							Iterator<String> it = sensorRegistrations.keySet().iterator();
							while(it.hasNext()) { // For each sensor queue
								String sensor = it.next();
								
								// Check if we have a player with a matching name, if not we do nothing
								PlayerInterface player = central.getPlayerManager().getPlayer(sensor);
								if(player != null) {
									// Attempt to find the head of the queue
									DeviceSensorEntry entry = null;								
									try {
										entry = sensorRegistrations.get(sensor).first();
									} catch(NoSuchElementException e) {}
								
									// If the queue is empty, or the top one is not active we clear player control
									if(entry == null || !entry.device.isActive()) {
										if(player.getControllingDevice() != null) {
											System.out.println("Clearing control of player "+sensor);
											player.setControllingDevice(null); 
										}
									} 
									// Otherwise we update player control
									else {
										if(player.getControllingDevice() == null || !player.getControllingDevice().equals(entry.device)) {
											System.out.println(entry + " should control "+sensor);										
											player.setControllingDevice(entry.device);
										}
									}
								}
							}
							System.out.println(sensorRegistrations);
							devicesChanged = false;
						}
						
						//System.out.println(that);
						Thread.sleep(250);
					} catch(InterruptedException e) {}
				}
			};
		};
		thread.setDaemon(true);
		thread.start();
		
	}

	public Map<String, SortedSet<DeviceSensorEntry>> getSensorRegistrations() {
		return sensorRegistrations;
	}
	
	/**
	 * A new sensor has connected to the system
	 */
	@Override
	public void onSensorAttach(String alias) {
		sensorRegistrations.put(alias, new TreeSet<DeviceSensorEntry>());
		for(Device d : devices.values()) {
			d.onSensorAttach(alias);
		}
		devicesChanged = true;
	}

	/**
	 * A sensor has disconnected
	 */
	@Override
	public void onSensorDetach(String alias) {
		sensorRegistrations.remove(alias);
		for(Device d : devices.values()) {
			d.onSensorDetach(alias);
		}
		devicesChanged = true;		
	}

	/**
	 * Sensor update
	 */
	@Override
	public void onSensorUpdate(String alias, String mac, int rssi) {
		// Create a new device entry if none exists
		if (!devices.containsKey(mac)) {
			Device device = new Device(mac, this);

			// Emulate a attach event for all already attached sensors to ensure that the internal state is good
			for(String s : central.getSensorManager().getSensorAliases()) {
				device.onSensorAttach(s);
			}
			
			devices.put(mac, device);
		}
		// Pass update along to the device entry
		devices.get(mac).onSensorUpdate(alias, rssi);
	}

	/**
	 * Called by a device when it has detected that it has a new closest sensor
	 */
	@Override
	public void onDeviceClosestSensor(Device device, String old_alias, String sensor_alias) {		
		if(old_alias != null && old_alias.equals(sensor_alias)) {
			return;
		}
		devicesChanged = true;
		
		// If we got an old closest sensor we need to de-register
		if(old_alias != null && sensorRegistrations.containsKey(old_alias)) {
			Iterator<DeviceSensorEntry> it = sensorRegistrations.get(old_alias).iterator();
			while(it.hasNext()) {
				DeviceSensorEntry entry = it.next();
				if(entry.device.equals(device)) {
					it.remove();
					break;
				}
			}
		}
		
		// Create a new registration
		DeviceSensorEntry entry = new DeviceSensorEntry();
		entry.device = device;
		entry.timestamp = System.currentTimeMillis();
		entry.priority = 1; // TODO Get priority from somewhere
		sensorRegistrations.get(sensor_alias).add(entry);
	}

	/**
	 * Called by a device when its active status has changed
	 */
	@Override
	public void onActiveStatusChange(Device device, String sensor_alias) {		
		devicesChanged = true;
		
		// Remove and re-insert the entry as the active flag has changed (yey, sets!)
		Iterator<DeviceSensorEntry> it = sensorRegistrations.get(sensor_alias).iterator();
		DeviceSensorEntry entry = null;
		while(it.hasNext()) {
			DeviceSensorEntry e = it.next();
			if(e.device.equals(device)) {
				entry = e;
				it.remove();
			}
		}
		if(entry != null) {
			sensorRegistrations.get(sensor_alias).add(entry);
		}
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

	/**
	 * A new player has been attached, set devices changed to true in case we already have a queue
	 * for the device
	 */
	@Override
	public void onPlayerAttach(String alias) {
		devicesChanged = true;
	}

	/**
	 * We do nothing if a player disconnects
	 */
	@Override
	public void onPlayerDetach(String alias) {}	
}
