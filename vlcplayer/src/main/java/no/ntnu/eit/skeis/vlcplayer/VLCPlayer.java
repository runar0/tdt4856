package no.ntnu.eit.skeis.vlcplayer;

/**
 * A extremely minimal AVTransport implementation allowing our central to control
 * a VLC instance running in the background as if it was just an other DLNA player
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class VLCPlayer {
	
	public final static VLC vlc = new VLC();
	
	/**
	 * Client version. 
	 * 
	 * Increment after a change that will break backwards compatibility to prevent
	 * hard to detect bugs in VLCPLayer<->Central communication.
	 */
	public final static long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		// Start UPNP server, this will take care of controlling our public vlc instance
		UPNPServer upnp = new UPNPServer();
		upnp.start();
	}
}
