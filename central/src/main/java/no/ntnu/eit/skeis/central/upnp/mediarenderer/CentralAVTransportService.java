package no.ntnu.eit.skeis.central.upnp.mediarenderer;

import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.central.Device;
import no.ntnu.eit.skeis.central.audio.StreamingSource;

import org.fourthline.cling.model.ModelUtil;
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

public class CentralAVTransportService extends AbstractAVTransportService {
	
	private final Logger log;
	
	private final int instance;
	private final CentralMediaRenderer mediarenderer;
	
	private URI currentURI;
	private String currentURIMeta;	

	private MediaInfo mediaInfo = new MediaInfo();
	private PositionInfo positionInfo = new PositionInfo();
	private TransportInfo transportInfo = new TransportInfo(TransportState.STOPPED);
	
	private boolean hasNewUrl = true;
		
	public CentralAVTransportService(CentralMediaRenderer mediarenderer, int instance) {
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
		if(hasNewUrl)
			killStreamServer();
		
		if (currentURI == null || getDevice() == null) {
			log.info(instance+ ": Did not start streaming server, either no URI or (probably) no device found!");
			return;
		}
		
		new Thread() {
			public void run() {
				try {
					// Do initial http connection
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
							state = 0;
						}
					}
					if (b == -1) {
						socket.close();
						throw new IOException("EOF");
					}
					
					log.info(instance+ ": Started stream server "+instance);
					
					// Hand the remainder of the stream and controller device to the streaming server 
					mediarenderer.setStreamingServer(instance, new StreamingSource(socket, in, getLastChange(), getDevice()));
				} catch(Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
		
	}
	
	/**
	 * Kill stream server if it is running
	 */
	private void killStreamServer() {
		hasNewUrl = true;
		StreamingSource streamingServer = mediarenderer.getStreamingServer(instance);
		if(streamingServer != null) {
			log.info(instance + ": Attempting to kill stream");
			streamingServer.stop();
			mediarenderer.setStreamingServer(instance, null);
		}
	}
	
	public PositionInfo getPositionInfo() {
		StreamingSource streamingServer = mediarenderer.getStreamingServer(instance);
		if(streamingServer != null) {
			positionInfo = new PositionInfo(positionInfo, streamingServer.getPosition(), streamingServer.getDuration());
		}
		return positionInfo;
	}
	
	@Override
	public UnsignedIntegerFourBytes[] getCurrentInstanceIds() {
		return new UnsignedIntegerFourBytes[] { new UnsignedIntegerFourBytes(0), new UnsignedIntegerFourBytes(1) };
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
		
		System.out.println(instance+": setAVTransportURI "+currentURI + " meta: "+currentURIMetaData);	
		hasNewUrl = true;
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
		return getPositionInfo();
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
		
		hasNewUrl = true;
		System.out.println(instance+ ": Stop");		
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
		System.out.println(instance+ ": Play");
		
		createStreamServer();

		transportInfo = new TransportInfo(TransportState.PLAYING);	
		getLastChange().setEventedValue(
			instanceId,
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
	public void seek(UnsignedIntegerFourBytes instanceId, String unit, String target) throws AVTransportException {
		System.out.println(instanceId + " seek "+unit + " "+target);
		StreamingSource streamingServer = mediarenderer.getStreamingServer(instance);
		if(streamingServer != null) {
			log.info(instance + ": seeking to "+target);
			streamingServer.seek(ModelUtil.fromTimeString(target));
		}
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
			TransportAction.Stop,
			TransportAction.Seek
		};
	}

}
