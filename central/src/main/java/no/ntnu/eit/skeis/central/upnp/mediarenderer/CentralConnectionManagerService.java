package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

public class CentralConnectionManagerService extends ConnectionManagerService {

	private final int instance;
	private final CentralMediaRenderer mediarenderer;
	
	public CentralConnectionManagerService(CentralMediaRenderer mediarenderer, int instance) {
		this.mediarenderer = mediarenderer;
		this.instance = instance;
		
		sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/mpeg")));
	}
	
}
