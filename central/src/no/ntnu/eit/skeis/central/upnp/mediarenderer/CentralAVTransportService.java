package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Central;
import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.StreamingTest;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;
import org.fourthline.cling.support.avtransport.AVTransportException;
import org.fourthline.cling.support.avtransport.AbstractAVTransportService;
import org.fourthline.cling.support.avtransport.lastchange.AVTransportVariable;
import org.fourthline.cling.support.lastchange.LastChange;
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
	
	private final Logger log;
	
	private final int instance;
	private final CentralMediaRenderer mediarenderer;
	
	private URI currentURI;
	private String currentURIMeta;	

	private MediaInfo mediaInfo = new MediaInfo();
	private PositionInfo positionInfo = new PositionInfo();
	private TransportInfo transportInfo = new TransportInfo(TransportState.STOPPED);
		
	public CentralAVTransportService(CentralMediaRenderer mediarenderer, int instance, LastChange lastChange) {
		super(lastChange);
		log = Logger.getLogger(getClass().getName());
		this.mediarenderer = mediarenderer;
		this.instance = instance;
	}
	
	/**
	 * Get device related to this media renderer, or null
	 * @return
	 */
	private Device getDevice() {
		if(currentURI == null) return null;
		
		return mediarenderer.getDevice(currentURI.getHost());
	}
	
	/**
	 * Create stream server 
	 */
	private void createStreamServer() {
		killStreamServer();
		
		if (currentURI == null || getDevice() == null) {
			log.info("Did not start streaming server, either no URI or (probably) no device found!");
			return;
		}
		
		try {
			// Do intial http connection
			Socket socket = new Socket(currentURI.getHost(), currentURI.getPort());
			socket.getOutputStream().write(
				("GET "+currentURI.getPath()+" HTTP/1.1\r\n" +
				"Host: "+currentURI.getHost()+":"+currentURI.getPort()+"\r\n\r\n").getBytes()
			);
			socket.getOutputStream().flush();
			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
			
			// Read up until \r\n\r\n
			int state = 0;
			int b = 0;
			String header = "";
			while(state < 4 && (b = in.read()) != -1) {
				header += new String(new byte[]{(byte)(b&0xFF)});
				if((state == 0 || state == 2) && b == '\r') {
					state++;
				} else if ((state == 1 || state == 3) && b == '\n') {
					state++;
				} else {
					//System.out.println("Initial scan reset");
					state = 0;
				}
			}
			if (b == -1) {
				throw new IOException("EOF");
			}
			
			// Hand the remainder of the stream to the streaming server and let the device know
			mediarenderer.setStreamingServer(instance, new StreamingTest(in, getLastChange(), getDevice()));			
			getDevice().setAudioSource(mediarenderer.getStreamingServer(instance));		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Kill stream server if it is running
	 */
	private void killStreamServer() {
		StreamingTest streamingServer = mediarenderer.getStreamingServer(instance);
		if(streamingServer != null) {
			if (getDevice() != null) {
				// Deactivate device
				getDevice().setAudioSource(null);
			}
			streamingServer.stop();
		}
	}
	
	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[] { new UnsignedIntegerFourBytes(0) };
	}

	@Override
	public void setAVTransportURI(final UnsignedIntegerFourBytes instanceId,
			final String currentURI, final String currentURIMetaData)
			throws AVTransportException {
		try {
			this.currentURI = new URI(currentURI);
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
		currentURIMeta = currentURIMetaData;
		
		mediaInfo = new MediaInfo(currentURI, currentURIMetaData);
		positionInfo = new PositionInfo(1, currentURIMetaData, currentURI);
		getLastChange().setEventedValue(
				instanceId, 
				new AVTransportVariable.CurrentTrackURI(this.currentURI),
				new AVTransportVariable.AVTransportURI(this.currentURI)
		);
		
		System.out.println(instanceId.getValue()+": setAVTransportURI "+currentURI + " meta: "+currentURIMetaData);		
	}
	
	@Override
	public void setNextAVTransportURI(UnsignedIntegerFourBytes instanceId,
			String nextURI, String nextURIMetaData) throws AVTransportException {
		// NO-OP
	}
	
	@Override
	public MediaInfo getMediaInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		return mediaInfo;
	}

	@Override
	public TransportInfo getTransportInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		return transportInfo;
	}
	
	@Override
	public PositionInfo getPositionInfo(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
		return positionInfo;
	}

	@Override
	public DeviceCapabilities getDeviceCapabilities(
			UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return new DeviceCapabilities(new StorageMedium[]{StorageMedium.NETWORK});
	}

	@Override
	public TransportSettings getTransportSettings(
			UnsignedIntegerFourBytes instanceId) throws AVTransportException {
		return new TransportSettings();
	}

	@Override
	public void stop(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {

		System.out.println("Stop");		
		killStreamServer();
		
		transportInfo = new TransportInfo(TransportState.STOPPED);		
		getLastChange().setEventedValue(
			instanceId.getValue().intValue(), 
			new AVTransportVariable.TransportState(TransportState.STOPPED)
		);
	}

	@Override
	public void play(UnsignedIntegerFourBytes instanceId, String speed)
			throws AVTransportException {
		System.out.println("Play");
		
		// TODO Start streaming server, associate with device
		createStreamServer();

		transportInfo = new TransportInfo(TransportState.PLAYING);	
		getLastChange().setEventedValue(
			instanceId.getValue().intValue(), 
			new AVTransportVariable.TransportState(TransportState.PLAYING)
		);
	}

	@Override
	public void pause(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
	}

	@Override
	public void record(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
	}

	@Override
	public void seek(UnsignedIntegerFourBytes instanceId, String unit,
			String target) throws AVTransportException {
	}

	@Override
	public void next(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
	}

	@Override
	public void previous(UnsignedIntegerFourBytes instanceId)
			throws AVTransportException {
	}

	@Override
	public void setPlayMode(UnsignedIntegerFourBytes instanceId,
			String newPlayMode) throws AVTransportException {
	}

	@Override
	public void setRecordQualityMode(UnsignedIntegerFourBytes instanceId,
			String newRecordQualityMode) throws AVTransportException {
	}

	@Override
	protected TransportAction[] getCurrentTransportActions(
			UnsignedIntegerFourBytes instanceId) throws Exception {
		return new TransportAction[] { 
			TransportAction.Play, 
			TransportAction.Stop 
		};
	}

}
