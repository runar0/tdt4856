package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.audio.AudioSource;
import no.ntnu.eit.skeis.central.devices.player.PlayerInterface;

/**
 * Device
 * 
 * Represents a single device, and all recent readings it has.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class Device {

	public interface DeviceListener {
		/**
		 * Called when a device has a new closest sensor
		 * @param device
		 * @param sensor_alias
		 */
		public void onDeviceNewClosest(Device device, String sensor_alias);
		
		/**
		 * Called when the active status of a player changes
		 */
		public void onActiveStatusChange(Device device, String sensor_alias);
	}
	
	private static final int INFINITY = -150;
	
	/**
	 * PlayerConnection this device is related to
	 */
	private PlayerInterface player;
	
	/**
	 * Store readings for each of the sensors
	 */
	private Map<String, Integer> readings;
	
	/**
	 * Device id
	 */
	private String mac;
	
	/**
	 * Time stamp of last update received by Device
	 */
	private long last_update;
	
	/**
	 * Listener to be notified on device events
	 */
	private DeviceListener listener;
	
	/**
	 * Alias of the current closest sensor, used to detect if we got a new closest one
	 */
	private String closest_sensor = null;
	
	/**
	 * A audio source that has been associated with this device
	 */
	private AudioSource audio_source;
	
	private Logger log;
	
	/**
	 * Construct a new device
	 * 
	 * @param mac
	 * @param sensorAliases
	 * @param listener
	 */
	public Device(String mac, Set<String> sensorAliases, DeviceListener listener) {
		this.log = Logger.getLogger(getClass().getName());
		this.mac = mac;
		this.listener = listener;
		readings = new HashMap<String, Integer>();
		last_update = System.currentTimeMillis();
		
		// Emulate a attach event for all already attached sensors
		for(String s : sensorAliases) {
			onSensorAttach(s);
		}
		
		// TODO This is just for testing
		if(mac.toLowerCase().startsWith("a8:26:d9")) {
			audio_source = new AudioSource() {
				@Override
				public String getSonosUrl() {
					return "x-rincon-mp3radio://nrk-mms-live.online.no/nrk_radio_mp3_mp3_h";
				}
				@Override
				public String getHttpUrl() {
					return "http://nrk-mms-live.online.no/nrk_radio_mp3_mp3_h";
				}
			};
		}
		if(mac.toLowerCase().startsWith("f8:db:7f")) {
			audio_source = new AudioSource() {
				@Override
				public String getSonosUrl() {
					return "x-rincon-mp3radio://lyd.nrk.no/nrk_radio_klassisk_mp3_h";
				}
				@Override
				public String getHttpUrl() {
					return "http://lyd.nrk.no/nrk_radio_klassisk_mp3_h";
				}
			};
		}
		
		
	}
	
	/**
	 * Get device id (bluetooth mac)
	 * 
	 * @return
	 */
	public String getId() {
		return mac;
	}
	
	/**
	 * Time stamp of last update
	 * 
	 * @return
	 */
	public long getLastUpdate() {
		return last_update;
	}

	/**
	 * Remove sensor data related to a disconnected sensor
	 * @param alias
	 */
	public void onSensorDetach(String alias) {
		readings.remove(alias);
		
	}

	/**
	 * Create a new entry for a newly attached sensor
	 * @param alias
	 */
	public void onSensorAttach(String alias) {
		readings.put(alias, INFINITY);
		
	}

	/**
	 * Store a new sensor reading
	 * 
	 * @param alias
	 * @param rssi
	 */
	public void onSensorUpdate(String alias, int rssi) {
		last_update = System.currentTimeMillis();
		readings.put(alias, rssi);
		updateClosestSensor();
	}
	
	/**
	 * Get the last sensor reading
	 * 
	 * TODO We need to consider filtering of the data, and implement the rssi->distance calculation
	 * @param alias
	 * @return
	 */
	public int getLastReading(String alias) {
		return readings.get(alias);
	}
	
	/**
	 * Update closest sensor, and notify the device listener if it has changed
	 */
	private void updateClosestSensor() {
		int min = INFINITY;
		String alias = "";
		for(String sensor : readings.keySet()) {
			if(readings.get(sensor) > min) {
				min = readings.get(sensor);
				alias = sensor;
			}
		}
		if(!alias.equals(closest_sensor)) {
			log.info("Device "+mac+" new closest, was '"+closest_sensor+"' now '"+alias+"'");
			closest_sensor = alias;
			listener.onDeviceNewClosest(this, alias);
		}
	}
	
	/**
	 * Get the alias of the currently closest sensor
	 * 
	 * @return
	 */
	public String getClosestSensor() {
		return closest_sensor;
	}

	/**
	 * A device is considered active if it has a audio source attached
	 * @return
	 */
	public boolean isActive() {
		return audio_source != null;
	}
	
	/**
	 * @return currently attached source or null
	 */
	public AudioSource getAudioSource() {
		return audio_source;
	}
	
	/**
	 * Set the player connection this device is associated with
	 * 
	 * @param player
	 */
	public void setPlayerConnection(PlayerInterface player) {
		this.player = player;
	}
	
	/**
	 * Get associated player connection
	 * 
	 * @return
	 */
	public PlayerInterface getPlayerConnection() {
		return player;
	}
	
	public String toString() {
		return mac;
	}
}
