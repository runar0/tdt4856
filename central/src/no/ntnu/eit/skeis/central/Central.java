package no.ntnu.eit.skeis.central;


// SEnsor manager -> flytt socket dit, listener events
// DEvice tracker -> lytter på sensor updates, har en device per observerte device
// DEvice lytter på sensor attaches/detachers oppdaterer intern state


import java.util.logging.Logger;

public class Central {

	public static final long VERSION = 1;
	private final Logger log;
	
	private final SensorManager manager;
	private final DeviceTracker tracker;
	private final Beacon beacon;
	
	public static void main(String[] args) throws Exception {
		new Central();
	}
	
	public Central() throws Exception {
		log = Logger.getLogger("Central");
		manager = new SensorManager(12354);		
		tracker = new DeviceTracker(manager);		
		manager.start();
		
		beacon = new Beacon(this);
		beacon.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Kill beacon thread
				beacon.stopBeacon();
				while(true) {
					try {
						beacon.join();
						break;
					} catch(InterruptedException e) {}					
				}
				
				// TODO Sensor manager, 1. stop server socket, 2. notify all sensors

				log.info("Stopping");
			}
		});
	}
	
	public DeviceTracker getDeviceTracker() {
		return tracker;
	}
	
	public SensorManager getSensorManager() {
		return manager;
	}
}
