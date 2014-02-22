package no.ntnu.eit.skeis.central;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.ControllerEventListener;

import no.ntnu.eit.skeis.central.devices.PlayerConnection;
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
	
	/**
	 * Maps room alias to currently controlling device
	 */
	private final Map<String, Device> controllingDevice;
	
	/**
	 * Maps device MAC to player being controlled
	 */
	private final Map<String, PlayerConnection> controlledPlayer;
			
	public DeviceTracker(Central central) {
		this.central = central;
		devices = new HashMap<String, Device>();
		controllingDevice = new HashMap<String, Device>();
		controlledPlayer = new HashMap<String, PlayerConnection>();
		
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

	// TODO This will only work with one device!
	@Override
	public void onDeviceNewClosest(Device device, String sensor_alias) {
		if (!device.isActive()) {
			return;
		}
		
		PlayerConnection newPlayer = central.getPlayerManager().getPlayer(sensor_alias);
		PlayerConnection oldPlayer = controlledPlayer.put(device.getId(), newPlayer);
		
		// No change, do nothing
		if (oldPlayer != null && oldPlayer.equals(newPlayer)) {
			return;
		}
		
		// Stop old player if it exists
		if(oldPlayer != null) {
			oldPlayer.setPlayState(false);
			
		}
		
		System.out.println(sensor_alias + " is "+newPlayer);
		
		if(newPlayer != null) {
			newPlayer.setUrl(device.getAudioSource().getUrl());
			newPlayer.setPlayState(true);	
			newPlayer.setVolume(50);
		}
	}
	
}
