package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
import org.fourthline.cling.model.ModelUtil;
import org.fourthline.cling.model.ServiceManager;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.meta.DeviceDetails;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.ManufacturerDetails;
import org.fourthline.cling.model.meta.ModelDetails;
import org.fourthline.cling.model.types.UDADeviceType;
import org.fourthline.cling.model.types.UDN;

public class CentralMediaRenderer {

	final protected ServiceManager<CentralConnectionManagerService> connectionManager;
	final protected ServiceManager<CentralAVTransportService> avTransport;
	final protected ServiceManager<CentralAudioRenderingControl> renderingControl;

	final protected LocalDevice device;

	@SuppressWarnings("unchecked")
	public CentralMediaRenderer() {
		LocalServiceBinder binder = new AnnotationLocalServiceBinder();
		// The connection manager doesn't have to do much, HTTP is stateless
		LocalService<CentralConnectionManagerService> connectionManagerService = binder
				.read(CentralConnectionManagerService.class);
		connectionManager = new DefaultServiceManager<CentralConnectionManagerService>(
				connectionManagerService) {
			@Override
			protected CentralConnectionManagerService createServiceInstance()
					throws Exception {
				return new CentralConnectionManagerService();
			}
		};
		connectionManagerService.setManager(connectionManager);

		// The AVTransport just passes the calls on to the backend players
		LocalService<CentralAVTransportService> avTransportService = binder
				.read(CentralAVTransportService.class);
		avTransport = new DefaultServiceManager<CentralAVTransportService>(
				avTransportService) {
			@Override
			protected CentralAVTransportService createServiceInstance()
					throws Exception {
				return new CentralAVTransportService();
			}
		};
		avTransportService.setManager(avTransport);
		
		
		// The Rendering Control just passes the calls on to the backend players
		LocalService<CentralAudioRenderingControl> renderingControlService = binder
				.read(CentralAudioRenderingControl.class);
		renderingControl = new DefaultServiceManager<CentralAudioRenderingControl>(
				renderingControlService) {
			@Override
			protected CentralAudioRenderingControl createServiceInstance()
					throws Exception {
				return new CentralAudioRenderingControl();
			}
		};
		renderingControlService.setManager(renderingControl);

		try {
			device = new LocalDevice(
					new DeviceIdentity(UDN
							.uniqueSystemIdentifier("SKEIS Central")),
					new UDADeviceType("MediaRenderer", 1),
					new DeviceDetails(
							"SKEIS Central on "
									+ ModelUtil.getLocalHostName(false),
							new ManufacturerDetails("Cling",
									"http://github.com/Runar0/tdt4856"),
							new ModelDetails("SKEIS Central MediaRenderer", "Test",
									"1",
									"http://github.com/Runar0/tdt4856")),
					new LocalService[] { avTransportService,
							renderingControlService, connectionManagerService });
		} catch (ValidationException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public LocalDevice getDevice() {
		return device;
	}

}
