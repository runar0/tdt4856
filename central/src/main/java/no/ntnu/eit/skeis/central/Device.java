package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.audio.AudioSource;
import no.ntnu.eit.skeis.central.audio.StreamingSource;
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
		 * Called by a device after it has updated its readings
		 * 
		 * @param device
		 * @param sensor_alias
		 */
		public void onDeviceClosestSensor(Device device, String old_sensor_alias, String new_sensor_alias);
		
		/**
		 * Called when the active status of a player changes
		 */
		public void onActiveStatusChange(Device device, String sensor_alias);
	}
	
	/**
	 * Sensor reading container
	 * 
	 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
	 */
	private class SensorReading {
		private double distance = INFINITY;
		private long timestamp = System.currentTimeMillis();
		
		/**
		 * Update reading with a new distance
		 * 
		 * @param distance
		 */
		public void updateDistance(double distance) {
			// TODO Do we take into consideration time between readings as well?
			if(this.distance >= INFINITY) {
				this.distance = distance;
			} else {
				this.distance = (1-Config.SENSOR_READING_COEFF)*this.distance + (Config.SENSOR_READING_COEFF)*distance;
			}
			timestamp = System.currentTimeMillis();
		}
		
		/**
		 * Get reading distance
		 */
		public double getDistance() {
			// Age reading by 1 meter per 10 s TODO evaluate!
			double age = (System.currentTimeMillis()-timestamp) / 10000.0;
			return distance + age;
		}
	}
	
	/**
	 * 'Infinity', any distance larger than this will be ignored
	 */
	private static final double INFINITY = 50;
	
	/**
	 * PlayerConnection this device is related to
	 */
	private PlayerInterface player;
	
	/**
	 * Store readings for each of the sensors
	 */
	private Map<String, SensorReading> readings;
	
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
	public Device(String mac, DeviceListener listener) {
		this.log = Logger.getLogger(getClass().getName());
		this.mac = mac;
		this.listener = listener;
		readings = new HashMap<String, SensorReading>();
		last_update = System.currentTimeMillis();
				
		// TODO This is just for testing
		/*if(mac.toLowerCase().startsWith("a8:26:d9")) {
			audio_source = new AudioSource() {
				@Override
				public String getSonosUrl() {
					//return "x-rincon-mp3radio://nrk-mms-live.online.no/nrk_radio_mp3_mp3_h";
					return "x-rincon-mp3radio://10.0.0.1:8080/test";
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
		}*/
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
	 * Get device alias, or null if none exists
	 * 
	 * @return
	 */
	public String getAlias() {
		return Config.deviceAliases.get(mac);
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
		readings.put(alias, new SensorReading());
		
	}

	/**
	 * Store a new sensor reading
	 * 
	 * @param alias
	 * @param rssi
	 */
	public void onSensorUpdate(String alias, int rssi) {
		// Convert rssi to distance
		double A = Config.getClientAValue(alias, mac);
		double n = Config.getSensorNValue(alias);
		double distance = Math.pow(10.0, -(((double) rssi)+A)/10.0*n);
		
		// Update sensor reading
		SensorReading reading = readings.get(alias);
		reading.updateDistance(distance);
				
		// Check if we got a new closest sensor 
		updateClosestSensor();
	}
	
	/**
	 * Get the last sensor reading
	 * 
	 * @param alias
	 * @return
	 */
	public double getLastReading(String alias) {
		return readings.get(alias).getDistance();
	}
	
	/**
	 * Called by the device tracker when it wants us to re-evaluate our closest sensor
	 */
	public void updateClosestSensor() {
		double min = INFINITY;
		String alias = "";
		for(String sensor : readings.keySet()) {
			if(readings.get(sensor).getDistance() < min) {
				min = readings.get(sensor).getDistance();
				alias = sensor;
			}
		}
		String old_alias = closest_sensor;
		closest_sensor = alias;
		listener.onDeviceClosestSensor(this, old_alias, alias);
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
		return (getAlias() == null ? mac : getAlias());
	}

	/**
	 * Set new device audio source
	 * @param audio
	 */
	public void setAudioSource(StreamingSource audio) {
		if (audio_source != null) {
			audio_source = null;
			listener.onActiveStatusChange(this, closest_sensor);			
		}
		audio_source = audio;
		listener.onActiveStatusChange(this, closest_sensor);
	}
}
