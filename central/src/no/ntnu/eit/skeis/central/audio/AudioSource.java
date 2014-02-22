package no.ntnu.eit.skeis.central.audio;

/**
 * A audio source represents a resource that can be associated with a 
 * Device object and that can be played on a Player object
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public interface AudioSource {

	/**
	 * URL endpoint where a player object can reach this audio stream
	 * 
	 * @return
	 */
	public String getUrl();
	
}
