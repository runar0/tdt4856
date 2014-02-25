package no.ntnu.eit.skeis.central.devices.player;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.devices.PlayerManager;

/**
 * Abstract Player
 * 
 * This implementation contains all code related to device control handling, this is 
 * common to all Players no matter how the player itself is controlled
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
abstract public class AbstractPlayer implements PlayerInterface {

	protected final Logger log;
	protected final PlayerManager manager;
	protected final String alias;
	/**
	 * Queue of all active devices that are currently registered as closest to this player
	 */
	protected final Deque<Device> devices;
	
	/**
	 * The device that is currently controlling this player
	 */
	private Device active_device;

	public AbstractPlayer(PlayerManager manager, String alias) {
		log = Logger.getLogger(getClass().getName());
		this.manager = manager;
		this.alias = alias;
		devices = new LinkedList<Device>();
	}

	/**
	 * Get player alias, this is a unique identifier
	 */
	@Override
	public String getAlias() {
		return alias;
	}

	/**
	 * Register a new device
	 */
	@Override
	public void registerDevice(Device device) {
		if(!devices.contains(device)) {
			devices.addLast(device);
			device.setPlayerConnection(this);
		}
		updateActivePlayer();
	}

	/**
	 * Remove device
	 */
	@Override
	public void unregisterDevice(Device device) {
		if(devices.contains(device)) {
			devices.remove(device);
			device.setPlayerConnection(null);
		}
		updateActivePlayer();
	}

	/**
	 * Called after the device queue has been updated, this is where we update
	 * the controlling device
	 */
	private void updateActivePlayer() {	
		log.info("Player "+alias+": Device queue "+devices);
		
		// TODO Priority can be implemented here, the best way would be to replace the LL with a PQ
		// TODO deivce.getLastUpdate() is the timestamp of the last update, might be an idea to time out devices after a set amount of time
		
		Device d = devices.peekFirst();
		if (d == null) {
			setPlayState(false);
		} else if(!d.equals(active_device)) {
			setUrl(d.getAudioSource().getUrl());
			setPlayState(true);
			setVolume(50);
			active_device = d;
		}
	}
}