package no.ntnu.eit.skeis.central.audio;

/**
 * A audio source represents a resource that can be associated with a 
 * Device object and that can be played on a Player object
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public interface AudioSource {

	/**
	 * Http url endpoint where the stream is at
	 * 
	 * @return
	 */
	public String getHttpUrl();
	
	/**
	 * Url to the same stream, but formatted for sonos devices
	 * @return
	 */
	public String getSonosUrl();
	
}
