package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.Config;
import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.StreamingSource;

import org.fourthline.cling.binding.LocalServiceBinder;
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.fourthline.cling.model.DefaultServiceManager;
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
import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.fourthline.cling.support.lastchange.LastChange;
import org.fourthline.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

/**
 * Implements the media renderer endpoint which allows users to stream audio from any
 * DLNA capable device (e.g. a android phone) to the central.
 * 
 * NOTE: I cannot figure out how to get a android phone to use a instance id other than
 *       0, so in an attempt to get a prototype up an running I've made it so that the 
 *       central will export multiple media renderer instances instead. This is by no means
 *       the final solution.
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 *
 */
public class CentralMediaRenderer {

	private final Logger log;
	
	final protected ServiceManager<CentralConnectionManagerService>[] connectionManagers;
	final protected ServiceManager<CentralAVTransportService>[] avTransports;
	final protected ServiceManager<CentralAudioRenderingControl>[] renderingControls;
	final protected StreamingSource[] streamingServers;	
	final protected LocalDevice[] devices;

	final protected LastChange avTransportLastChange = new LastChange(
			new AVTransportLastChangeParser());
	final protected LastChange renderingControlLastChange = new LastChange(
			new RenderingControlLastChangeParser());

	final protected Central central;

	@SuppressWarnings("unchecked")
	public CentralMediaRenderer(final Central central, int num_endpoints) {
		log = Logger.getLogger(getClass().getName());
		this.central = central;
		connectionManagers = new ServiceManager[num_endpoints];
		avTransports = new ServiceManager[num_endpoints];
		renderingControls = new ServiceManager[num_endpoints];
		devices = new LocalDevice[num_endpoints];
		streamingServers = new StreamingSource[num_endpoints];
		
		buildDevices(num_endpoints);
		//runLastChangePushThread();
	}

	/**
	 * 
	 */
	protected void runLastChangePushThread() {
		// TODO: We should only run this if we actually have event subscribers
		new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						// These operations will NOT block and wait for network
						// responses
						
						for(int i = 0; i < avTransports.length; i++) {
							ServiceManager<CentralAVTransportService> avTransport = avTransports[i];
							avTransport.getImplementation().getLastChange().fire(avTransport.getImplementation().getPropertyChangeSupport());
							ServiceManager<CentralAudioRenderingControl> renderingControl = renderingControls[i];
							renderingControl.getImplementation().getLastChange().fire(renderingControl.getImplementation().getPropertyChangeSupport());
						}
						Thread.sleep(500);
					}
				} catch (Exception ex) {
					//throw new RuntimeException(ex);
				}
			}
		}.start();
	}
	
	public Central getCentral() {
		return central;
	}
	
	/**
	 * Build a set of media renderer devices
	 * 
	 * @param num_devices
	 */
	@SuppressWarnings("unchecked")
	private void buildDevices(int num_devices) {
		for(int i = 0; i < num_devices; i++) {
			final int instance = i;
			final ServiceManager<CentralConnectionManagerService> connectionManager;
			final ServiceManager<CentralAVTransportService> avTransport;
			final ServiceManager<CentralAudioRenderingControl> renderingControl;
			final CentralMediaRenderer that = this;
			
			LocalServiceBinder binder = new AnnotationLocalServiceBinder();
			// The connection manager doesn't have to do much, HTTP is stateless
			LocalService<CentralConnectionManagerService> connectionManagerService = binder
					.read(CentralConnectionManagerService.class);
			connectionManager = new DefaultServiceManager<CentralConnectionManagerService>(connectionManagerService) {
				@Override
				protected CentralConnectionManagerService createServiceInstance()
						throws Exception {
					return new CentralConnectionManagerService(that, instance);
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
					return new CentralAVTransportService(that, instance, avTransportLastChange);
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
					return new CentralAudioRenderingControl(that, instance, renderingControlLastChange);
				}
			};
			renderingControlService.setManager(renderingControl);

			LocalDevice device;
			try {
				device = new LocalDevice(new DeviceIdentity(
					UDN.uniqueSystemIdentifier("SKEIS Central "+i)),
					new UDADeviceType("MediaRenderer", 1), 
					new DeviceDetails(
						"SKEIS Central "+i,
						new ManufacturerDetails("SKEIS", "http://github.com/Runar0/tdt4856"),
						new ModelDetails("SKEIS Central MediaRenderer")
					),
					new LocalService[] { 
						avTransportService,
						renderingControlService, 
						connectionManagerService 
					}
				);
			} catch (ValidationException ex) {
				throw new RuntimeException(ex);
			}
			
			connectionManagers[i] = connectionManager;
			avTransports[i] = avTransport;
			renderingControls[i] = renderingControl;
			devices[i] = device;			
		}
	}

	public LocalDevice[] getDevices() {
		return devices;
	}
	
	/**
	 * Get the device instance that is associated with the given ip address
	 * 
	 * @param ip
	 * @return
	 */
	public Device getDevice(String ip) {
		String mac = Config.ipBMacMapping.get(ip);
		if (mac == null) {
			log.info("Could not find mapping for device with ip "+ip);
			return null;
		}
		return getCentral().getDeviceTracker().getDevice(mac);
	}
	
	public void setStreamingServer(int instance, StreamingSource server) {
		streamingServers[instance] = server;
	}
	
	public StreamingSource getStreamingServer(int instance) {
		return streamingServers[instance];
	}

}
