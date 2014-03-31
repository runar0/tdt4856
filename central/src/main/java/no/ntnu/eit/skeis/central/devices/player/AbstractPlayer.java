package no.ntnu.eit.skeis.central.devices.player;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.AudioSource;
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
	 * The device that is currently controlling this player
	 */
	private Device active_device;

	public AbstractPlayer(PlayerManager manager, String alias) {
		log = Logger.getLogger(getClass().getName());
		this.manager = manager;
		this.alias = alias;
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
	public void setControllingDevice(Device device) {
		if(active_device != device) {
			if(device == null) {
				log.info("Player "+alias+": cleared device control");
				setPlayState(false);
			} else {
				log.info("Player "+alias+": New controlling device "+device);
				playAudioSource(device.getAudioSource());
			}
			active_device = device;
		}
	}
	
	@Override
	public Device getControllingDevice() {
		return active_device;
	}
	
	/**
	 * Start playback of the given audio source object
	 * 
	 * @param source
	 */
	protected abstract void playAudioSource(AudioSource source);
}