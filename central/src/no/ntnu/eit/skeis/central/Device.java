package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import no.ntnu.eit.skeis.central.audio.AudioSource;
import no.ntnu.eit.skeis.central.devices.PlayerConnection;

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
	}
	
	private static final int INFINITY = -90;
	
	private Map<String, Integer> readings;
	
	private String mac;
	
	private DeviceListener listener;
	
	private String closest_sensor = null;
	
	/**
	 * A audio source that has been associated with this device
	 */
	private AudioSource audio_source;
	
	
	public Device(String mac, Set<String> sensorAliases, DeviceListener listener) {
		this.mac = mac;
		this.listener = listener;
		readings = new HashMap<String, Integer>();
		
		for(String s : sensorAliases) {
			onSensorAttach(s);
		}
		
		// TODO This is just for testing
		if(mac.toLowerCase().startsWith("a8:26:d9")) {
			audio_source = new AudioSource() {
				@Override
				public String getUrl() {
					return "http://lyd.nrk.no/nrk_radio_p1_sogn_og_fjordane_mp3_h.m3u";
				}
			};
		}
	}
	
	public String getId() {
		return mac;
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
			closest_sensor = alias;
			listener.onDeviceNewClosest(this, alias);
		}
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
	
}
