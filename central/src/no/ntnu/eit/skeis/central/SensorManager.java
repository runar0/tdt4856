package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.device.SensorProtos.SensorUpdate;

public class SensorManager {

	interface SensorEventListener {
		public void onSensorAttach(String alias);
		public void onSensorDetach(String alias);
		public void onSensorUpdate(String alias, String mac, int rssi);
	}
	
	private Map<String, SensorConnection> sensors;
	private Logger log;
	
	private Set<SensorEventListener> listeners;
	
	public SensorManager() {
		listeners = new HashSet<SensorEventListener>();
		sensors = new HashMap<String, SensorConnection>();
		log = Logger.getLogger("SensorManager");
	}
		
	/**
	 * Get a set of all current sensor aliases
	 * 
	 * @return
	 */
	public Set<String> getSensorAliases() {
		return sensors.keySet();
	}
	
	public void addListener(SensorEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(SensorEventListener listener) {
		listeners.remove(listener);
	}
	
	public boolean addSensor(String alias, SensorConnection sensor) {
		if (sensors.containsKey(alias)) {
			return false;
		}
		log.info("New sensor added "+alias);
		sensors.put(alias, sensor);
		for (SensorEventListener listener : listeners) {
			listener.onSensorAttach(alias);
		}
		return true;
	}
	
	public void removeSensor(String alias) {
		log.info("Removing sensor "+alias);
		sensors.remove(alias);
		for (SensorEventListener listener : listeners) {
			listener.onSensorDetach(alias);
		}
	}
	
	public synchronized void onSensorUpdate(SensorConnection source, SensorUpdate update) {
		//log.info(source+" update "+update.getUnitMac()+" - "+update.getRssi());
		for (SensorEventListener listener : listeners) {
			listener.onSensorUpdate(source.getAlias(), update.getUnitMac(), update.getRssi());
		}
	}
	
}
