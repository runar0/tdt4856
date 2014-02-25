package no.ntnu.eit.skeis.central.devices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.devices.player.PlayerInterface;
import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerStateUpdate;

public class PlayerManager {

	public interface PlayerEventListener {
		public void onPlayerAttach(String alias);
		public void onPlayerDetach(String alias);
		public void onPlayerState(String alias, PlayerStateUpdate.States state, String url, int volume);
	}
	
	private Map<String, PlayerInterface> players;
	private Logger log;
	
	private Set<PlayerEventListener> listeners;
	
	private Central central;
	
	public PlayerManager(Central central) {
		this.central = central;
		listeners = new HashSet<PlayerEventListener>();
		players = new HashMap<String, PlayerInterface>();
		log = Logger.getLogger(getClass().getName());
	}
		
	/**
	 * Get a set of all current player aliases
	 * 
	 * @return
	 */
	public Set<String> getSensorAliases() {
		return players.keySet();
	}
	
	public void addListener(PlayerEventListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(PlayerEventListener listener) {
		listeners.remove(listener);
	}
	
	public boolean addPlayer(String alias, final PlayerInterface player) {
		if (players.containsKey(alias)) {
			return false;
		}
		log.info("New player added "+alias);
		players.put(alias, player);
		for (PlayerEventListener listener : listeners) {
			listener.onPlayerAttach(alias);
		}
		
		// Associate any devices that are already closest to the matching sensor
		for(Device device : central.getDeviceTracker().getDevicesClosestTo(alias)) {
			player.registerDevice(device);
		}
		return true;
	}
	
	public void removePlayer(String alias) {
		log.info("Removing player "+alias);
		PlayerInterface player = players.remove(alias);
		if (player != null) {
			for (PlayerEventListener listener : listeners) {
				listener.onPlayerDetach(alias);
			}
		}
	}
	
	public void onStateUpdate(PlayerInterface player, PlayerStateUpdate update) {
		for (PlayerEventListener listener : listeners) {
			listener.onPlayerState(player.getAlias(), update.getState(), update.getUrl(), update.getVolume());
		}
	}

	public PlayerInterface getPlayer(String alias) {
		return players.get(alias);
	}
	
	/**
	 * Called by the DeviceTracker when a device has a new closest sensor, if a player with the same
	 * alias as the new closest sensor exists we will register the device with that player. At the same
	 * time any previous registration has to be invalidated.
	 * 
	 * @param alias
	 * @param device
	 */
	public void updateDevicePosition(String alias, Device device) {
		if(device.getPlayerConnection() != null && device.getPlayerConnection().getAlias().equals(alias)) {
			return;
		}		
		removeDevice(device);
		
		PlayerInterface newPlayer = players.get(alias);
		if(newPlayer != null) {
			newPlayer.registerDevice(device);
		}
	}
	
	/**
	 * Called by the DeviceTracker when a device is to be removed, this can be caused by a device
	 * timing out (not being detected for a set amount of time) or stopping playback.
	 * 
	 * @param device
	 */
	public void removeDevice(Device device) {
		if(device.getPlayerConnection() != null) {
			device.getPlayerConnection().unregisterDevice(device);
		}
	}
	
}
