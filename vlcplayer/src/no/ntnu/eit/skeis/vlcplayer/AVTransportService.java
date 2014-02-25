package no.ntnu.eit.skeis.vlcplayer;

import java.util.logging.Logger;

import org.fourthline.cling.binding.annotations.UpnpAction;
import org.fourthline.cling.binding.annotations.UpnpInputArgument;
import org.fourthline.cling.binding.annotations.UpnpService;
import org.fourthline.cling.binding.annotations.UpnpServiceId;
import org.fourthline.cling.binding.annotations.UpnpServiceType;
import org.fourthline.cling.binding.annotations.UpnpStateVariable;
import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

@UpnpService(
	serviceId = @UpnpServiceId("AVTransport"),
	serviceType = @UpnpServiceType(value = "AVTransport", version = 1)
)
public class AVTransportService {
		
	@UpnpStateVariable(defaultValue = "0", sendEvents = false)
	private UnsignedIntegerFourBytes InstanceID;
	

	@UpnpStateVariable(defaultValue = "1", sendEvents = false)
	private String Speed;

	@UpnpStateVariable(defaultValue = "", sendEvents = false)
	private String CurrentURI;
	
	@UpnpAction
	public void Play(
		@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes InstanceID,
		@UpnpInputArgument(name = "Speed") String Speed
	) {
		Logger.getLogger(getClass().getName()).info("UPNP Play");
		VLCPlayer.vlc.start();
	}
	
	@UpnpAction
	public void Stop(
		@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes InstanceID
	) {
		Logger.getLogger(getClass().getName()).info("UPNP Stop");
		VLCPlayer.vlc.stop();
	}
	
	@UpnpAction
	public void SetAVTransportURI(
		@UpnpInputArgument(name = "InstanceID") UnsignedIntegerFourBytes InstanceID,
		@UpnpInputArgument(name = "CurrentURI") String CurrentURI
	) {
		Logger.getLogger(getClass().getName()).info("UPNP set URI: "+CurrentURI);
		VLCPlayer.vlc.setUrl(CurrentURI);
	}
	
}
