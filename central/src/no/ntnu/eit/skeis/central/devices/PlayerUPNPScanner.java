package no.ntnu.eit.skeis.central.devices;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.types.ServiceId;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

public class PlayerUPNPScanner extends Thread {

	final ServiceId serviceId = new UDAServiceId("AVTransport");
	
	class SetUrlActionInvocation extends ActionInvocation<Service> {
		SetUrlActionInvocation(Service service) {
			super(service.getAction("SetAVTransportURI"));
			try {
				setInput("InstanceID", new UnsignedIntegerFourBytes(0));
				setInput("CurrentURI", "http://lyd.nrk.no/nrk_radio_p1_sogn_og_fjordane_mp3_h.m3u");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class PlayActionInvocation extends ActionInvocation<Service> {
		PlayActionInvocation(Service service) {
			super(service.getAction("Play"));
			try {
				setInput("InstanceID", new UnsignedIntegerFourBytes(0));
				setInput("Speed", "1");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public PlayerUPNPScanner() {
		setDaemon(false);
		start();
	}
	
	public void run() {
		try {
			final UpnpService upnp = new UpnpServiceImpl();
			
			upnp.getRegistry().addListener(
				new RegistryListener() {
					
					@Override
					public void remoteDeviceUpdated(Registry arg0, RemoteDevice arg1) {}
					
					@Override
					public void remoteDeviceRemoved(Registry arg0, RemoteDevice arg1) {}
					
					@Override
					public void remoteDeviceDiscoveryStarted(Registry arg0, RemoteDevice arg1) {}
					
					@Override
					public void remoteDeviceDiscoveryFailed(Registry arg0, RemoteDevice arg1,
							Exception arg2) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
						if(device.findService(serviceId) != null) {
							final Service avtransport = device.findService(serviceId);
							System.out.println("Found AVTransport in "+device);
							upnp.getControlPoint().execute(
								new ActionCallback(new SetUrlActionInvocation(avtransport)) {

									@Override
									public void failure(ActionInvocation arg0,
											UpnpResponse response, String something) {
										System.out.println("Failed");
										System.out.println(something);
										System.out.println(response);
									}

									@Override
									public void success(ActionInvocation arg0) {
										System.out.println("First success");
										upnp.getControlPoint().execute(
												new ActionCallback(new PlayActionInvocation(avtransport)) {

													@Override
													public void failure(ActionInvocation arg0,
															UpnpResponse response, String something) {
														System.out.println("Failed play");
														System.out.println(something);
														System.out.println(response);
													}

													@Override
													public void success(ActionInvocation arg0) {
														System.out.println("Success play!");
														
													}
													
												}
											);
										
									}
									
								}
							);
						}						
					}
					
					@Override
					public void localDeviceRemoved(Registry arg0, LocalDevice arg1) {}
					
					@Override
					public void localDeviceAdded(Registry arg0, LocalDevice arg1) {}
					
					@Override
					public void beforeShutdown(Registry arg0) {}
					
					@Override
					public void afterShutdown() {}
				}
			);
			
			upnp.getControlPoint().search(
                    new STAllHeader()
            );
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
