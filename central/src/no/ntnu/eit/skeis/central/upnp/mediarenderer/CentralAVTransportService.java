package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import java.beans.PropertyChangeSupport;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.model.DeviceCapabilities;
import org.fourthline.cling.support.model.MediaInfo;
import org.fourthline.cling.support.model.PositionInfo;
import org.fourthline.cling.support.model.StorageMedium;
import org.fourthline.cling.support.model.TransportAction;
import org.fourthline.cling.support.model.TransportInfo;
import org.fourthline.cling.support.model.TransportSettings;
import org.fourthline.cling.support.model.TransportState;
import org.fourthline.cling.support.model.TransportStatus;

public class CentralAVTransportService extends AbstractAVTransportService {

	private final PropertyChangeSupport changeSupport;
	
	public CentralAVTransportService() {
		changeSupport = new PropertyChangeSupport(this);
	}
	
	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		System.out.println("AVT getCurrentInstanceIds");
		return new UnsignedIntegerFourBytes[] {};
	}
	
	URI currentUri;
	String currentMeta;

	@Override
	public void setAVTransportURI(UnsignedIntegerFourBytes instanceId,
			String currentURI, String currentURIMetaData)
			throws AVTransportException {
		System.out.println(instanceId.getValue()+": setAVTransportURI "+currentURI + " meta: "+currentURIMetaData);
		
		try {
			URI uri = new URI(currentURI);
			this.currentUri = uri;
			currentMeta = currentURIMetaData;
			
			System.out.println(uri.getHost());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
			String nextURI, String nextURIMetaData) throws AVTransportException {
		System.out.println("setNextAVTransportURL");
	}

	@Override
	public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("getMediaInfo");
		return new MediaInfo();
	}

	@Override
	public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("AVT transportInfo");
		return new TransportInfo(TransportState.STOPPED, TransportStatus.OK, "1");
	}

	@Override
	public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		return new PositionInfo();
	}

	@Override
	public DeviceCapabilities getDeviceCapabilities(
			UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		System.out.println("getDeviceCapabilities");
		return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
	}

	@Override
	public TransportSettings getTransportSettings(
			UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		System.out.println("getTransportSettings");
		return new TransportSettings();
	}

	@Override
	public void stop(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("stop");
	}

	@Override
	public void play(UnsignedIntegerFourBytes instanceId, String speed)
			throws AVTransportException {
		System.out.println("Play");
		
		/*getLastChange().setEventedValue(
			instanceId.getValue().intValue(), 
			new AVTransportVariable.CurrentTrackURI(currentUri),
			new AVTransportVariable.CurrentMediaDuration("00:04:54"),
			new AVTransportVariable.AVTransportURIMetaData(currentMeta),
			new AVTransportVariable.TransportStatus(TransportStatus.OK),
			new AVTransportVariable.TransportPlaySpeed("1")
		);
		getLastChange().fire(changeSupport);*/
	}

	@Override
	public void pause(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("pause");
	}

	@Override
	public void record(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("record");
	}

	@Override
	public void seek(UnsignedIntegerFourBytes instanceId, String unit,
			String target) throws AVTransportException {
		System.out.println("seek");
	}

	@Override
	public void next(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("next");
	}

	@Override
	public void previous(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		System.out.println("previous");
	}

	@Override
	public void setPlayMode(UnsignedIntegerFourBytes instanceId,
			String newPlayMode) throws AVTransportException {
		System.out.println("setPlayMode");
	}

	@Override
	public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId,
			String newRecordQualityMode) throws AVTransportException {
		System.out.println("setRecordQualityMode");
	}

	@Override
	protected TransportAction[] getCurrentTransportActions(
			UnsignedIntegerFourBytes instanceId) throws Exception {
		System.out.println("getCurrentTransportActions");
		return new TransportAction[] { TransportAction.Play, TransportAction.Stop, TransportAction.Pause,
				TransportAction.Seek,
				TransportAction.Next,
				TransportAction.Previous };
	}

}
