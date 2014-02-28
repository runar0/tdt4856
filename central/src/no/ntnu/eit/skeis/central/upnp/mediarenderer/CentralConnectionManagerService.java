package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import org.fourthline.cling.support.connectionmanager.ConnectionManagerService;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

public class CentralConnectionManagerService extends ConnectionManagerService {

	public CentralConnectionManagerService() {
		super();
		
		sinkProtocolInfo.add(new ProtocolInfo(MimeType.valueOf("audio/mpeg")));
	}
	
}
