package no.ntnu.eit.skeis.central.devices;

import java.util.Deque;
import java.util.LinkedList;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;

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
		System.out.println("Player "+alias+": "+devices);
		
		// TODO Priority can be implemented here, the best way would be to replace the LL with a PQ
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