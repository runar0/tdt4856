package no.ntnu.eit.skeis.central.devices;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.devices.player.PlayerInterface;

public class PlayerManager {

	public interface PlayerEventListener {
		public void onPlayerAttach(String alias);
		public void onPlayerDetach(String alias);
	}
	
	private Map<String, PlayerInterface> players;
	private Logger log;
	
	private Set<PlayerEventListener> listeners;
	
	
	public PlayerManager() {
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

	public PlayerInterface getPlayer(String alias) {
		return players.get(alias);
	}
	
}
