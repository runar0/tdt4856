package no.ntnu.eit.skeis.central;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import no.ntnu.eit.skeis.protocol.SensorProtos.SensorRegisterRequest;
import no.ntnu.eit.skeis.protocol.SensorProtos.SensorRegisterResponse;
import no.ntnu.eit.skeis.protocol.SensorProtos.SensorUpdate;

public class Central {

	public static final long VERSION = 1;
	
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(12354);
		Socket s;
		
		while((s = ss.accept()) != null) {
			BufferedInputStream in = new BufferedInputStream(s.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());
						
			SensorRegisterRequest request = SensorRegisterRequest.parseDelimitedFrom(s.getInputStream());
						
			if(request.getClientVersion() == VERSION) {
				Logger.getLogger("Central").info("Client "+request.getClientAlias()+" connected from "+s.getInetAddress());
				SensorRegisterResponse.newBuilder()
					.setStatus(SensorRegisterResponse.StatusCodes.OK)
					.setServerVersion(VERSION)
					.setStatusMessage("Welcome")
					.build().writeDelimitedTo(out);
				out.flush();				
			} else {
				System.err.println("Invalid version");
				System.exit(1);
			}
			
			while(true) {
				SensorUpdate update = SensorUpdate.parseDelimitedFrom(in);
				Logger.getLogger("Central").info("Sensor update "+update.getUnitMac()+" - "+update.getRssi());
			}
		}
	}
	
}
