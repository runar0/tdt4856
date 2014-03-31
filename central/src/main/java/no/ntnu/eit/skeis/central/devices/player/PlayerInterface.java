package no.ntnu.eit.skeis.central.devices.player;

import no.ntnu.eit.skeis.central.Device;

/**
 * Common interface for all Players
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public interface PlayerInterface {

	/**
	 * Get player alias
	 * 
	 * @return
	 */
	public abstract String getAlias();

	/**
	 * Set playback url
	 * 
	 * @param url
	 */
	public abstract void setUrl(String url);

	/**
	 * Set play state
	 * 
	 * @param play if true, stop if false
	 */
	public abstract void setPlayState(boolean play);

	/**
	 * Set playback volume 0-100
	 * 
	 * @param volume
	 */
	public abstract void setVolume(int volume);

	public abstract int getVolume();
	
	public abstract void setMute(boolean flag);
	
	public abstract boolean getMute();

	/**
	 * Register a new device to this player
	 * 
	 * When a device is registered to a player the device is expected to be active, 
	 * and wanting to control the player
	 * 
	 * @param device
	 */
	public abstract void setControllingDevice(Device device);
	
	/**
	 * Get device currently controlling the player, or null
	 * 
	 * @return
	 */
	public abstract Device getControllingDevice();

}