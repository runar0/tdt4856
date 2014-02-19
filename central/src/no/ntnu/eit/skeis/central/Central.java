package no.ntnu.eit.skeis.central;


// SEnsor manager -> flytt socket dit, listener events
// DEvice tracker -> lytter på sensor updates, har en device per observerte device
// DEvice lytter på sensor attaches/detachers oppdaterer intern state


import java.util.logging.Logger;

public class Central {

	public static final long VERSION = 1;
	private final Logger log;
	
	private SensorManager manager;
	
	public static void main(String[] args) throws Exception {
		new Central();
	}
	
	public Central() throws Exception {
		log = Logger.getLogger("Central");
		manager = new SensorManager(12354);
		
		DeviceTracker tracker = new DeviceTracker(manager);
		
		manager.start();
	}
}
