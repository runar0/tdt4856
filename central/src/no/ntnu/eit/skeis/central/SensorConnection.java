package no.ntnu.eit.skeis.central;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.SensorProtos.SensorRegisterRequest;
import no.ntnu.eit.skeis.protocol.SensorProtos.SensorRegisterResponse;
import no.ntnu.eit.skeis.protocol.SensorProtos.SensorUpdate;

/**
 * SensorConnection
 * 
 * Central<->Sensor connection object
 * 
 * @author Runar B. Olsen <runar.b.olsen@gmail.com>
 */
public class SensorConnection {

	private final Logger log;
	private final SensorManager manager;
	
	private Socket s;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	
	private String alias;
	
	private Thread readThread;
	
	
	public SensorConnection(SensorManager manager, Socket s) {
		log = Logger.getLogger("SensorConnection["+s.getInetAddress()+"]");
		this.manager = manager;
		try {
			log.info("Sensor connection initializing");
			in = new BufferedInputStream(s.getInputStream());
			out = new BufferedOutputStream(s.getOutputStream());
			
			SensorRegisterRequest request = SensorRegisterRequest.parseDelimitedFrom(in);
			
			if(request.getClientVersion() == Central.VERSION) {
				Logger.getLogger("Central").info("Client "+request.getClientAlias()+" connected from "+s.getInetAddress());
				SensorRegisterResponse.newBuilder()
					.setStatus(SensorRegisterResponse.StatusCodes.OK)
					.setServerVersion(Central.VERSION)
					.setStatusMessage("Welcome")
					.build().writeDelimitedTo(out);
				out.flush();			
				
				alias = request.getClientAlias();
				manager.addSensor(request.getClientAlias(), this);
				startReadLoop();
			} else {
				System.err.println("Invalid version");
				System.exit(1);
			}
			
		} catch(IOException e) {
			log.warning("Unable to open sensor connection");
			log.warning(e.toString());
		}
	}
	
	public String getAlias() {
		return alias;
	}
	
	/**
	 * Start a read thread that continues to read sensor updates until the stream is
	 * closed. On every update read a message is sent to the central for futher 
	 * processing.
	 * 
	 */
	private void startReadLoop() {
		final SensorConnection sensor = this;
		readThread = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						SensorUpdate update = SensorUpdate.parseDelimitedFrom(in);
						
						if (update == null) {
							throw new IOException("Sensor update is null");
						}
						
						manager.onSensorUpdate(sensor, update);						
					} catch(IOException ioe) {
						log.info("Excpetion in read loop, dropping sensor");
						manager.removeSensor(alias);
						return;
					}
				}
			}
		};
		readThread.start();
	}
	
	public String toString() {
		return "Sensor["+alias+"]";
	}
	
}
