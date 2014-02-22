package no.ntnu.eit.skeis.central.devices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.device.PlayerProtos.PlayerStateUpdate;
import no.ntnu.eit.skeis.protocol.device.SensorProtos.SensorUpdate;

public class PlayerManager {

	public interface PlayerEventListener {
		public void onPlayerAttach(String alias);
		public void onPlayerDetach(String alias);
		public void onPlayerState(String alias, PlayerStateUpdate.States state, String url, int volume);
	}
	
	private Map<String, PlayerConnection> players;
	private Logger log;
	
	private Set<PlayerEventListener> listeners;
	
	public PlayerManager() {
		listeners = new HashSet<PlayerEventListener>();
		players = new HashMap<String, PlayerConnection>();
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
	
	public boolean addPlayer(String alias, final PlayerConnection player) {
		if (players.containsKey(alias)) {
			return false;
		}
		log.info("New player added "+alias);
		players.put(alias, player);
		for (PlayerEventListener listener : listeners) {
			listener.onPlayerAttach(alias);
		}
		return true;
	}
	
	public void removeSensor(String alias) {
		log.info("Removing player "+alias);
		players.remove(alias);
		for (PlayerEventListener listener : listeners) {
			listener.onPlayerDetach(alias);
		}
	}
	
	public void onStateUpdate(PlayerConnection player, PlayerStateUpdate update) {
		for (PlayerEventListener listener : listeners) {
			listener.onPlayerState(player.getAlias(), update.getState(), update.getUrl(), update.getVolume());
		}
	}

	public PlayerConnection getPlayer(String alias) {
		return players.get(alias);
	}
	
}
