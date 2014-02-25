package no.ntnu.eit.skeis.vlcplayer;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

public class UPNPServer extends Thread {
	
	
	@Override
	public void run() {
		try {
		final UpnpService upnpService = new UpnpServiceImpl();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				upnpService.shutdown();
			}
		});
		
		upnpService.getRegistry().addDevice(createDevice());
		} catch(ValidationException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	/**
	 * Build the local device
	 * 
	 * @return
	 * @throws ValidationException
	 */
	private LocalDevice createDevice() throws ValidationException {
		DeviceIdentity identity = new DeviceIdentity(
			UDN.uniqueSystemIdentifier("test-vlc-player")
		);
		
		DeviceType type = new UDADeviceType("AVTransport", 1);
		DeviceDetails details = new DeviceDetails(
			"VLC AVTransport Gateway",
			new ManufacturerDetails("SKEIS"),
			new ModelDetails("VLC", "A Simplistic AVTransport Gateway For VLC")
		);
		
		@SuppressWarnings("unchecked")
		LocalService<AVTransportService> avtransportService = 
			new AnnotationLocalServiceBinder().read(AVTransportService.class);
				
		avtransportService.setManager(
			new DefaultServiceManager<AVTransportService>(avtransportService, AVTransportService.class)
		);
		
		return new LocalDevice(identity, type, details, avtransportService);
	}
}
