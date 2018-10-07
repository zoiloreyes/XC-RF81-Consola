package ESPReaderConsola;
import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import invengo.javaapi.protocol.IRP1.ReadTag;

public class Lector implements invengo.javaapi.handle.IMessageNotificationReceivedHandle, invengo.javaapi.handle.IApiExceptionHandle  {
	private Timer checkConnTimer = new Timer();
	private TimerTask timerCheckConn;
	private Timer timerCambioTag;
	private String nombreLector;
	private String ip;
	private String puerto;
	private boolean isConnected;
	private Reader lector;
	private ReadTag mensajeEscaneo = new ReadTag(ReadTag.ReadMemoryBank.EPC_TID_UserData_6C_ID_UserData_6B);	
	private List<FoundTagListener> TagFoundListeners = new ArrayList<FoundTagListener>();
	private List<IAntennaListener> AntennaListeners = new ArrayList<IAntennaListener>();
	private boolean es6C = false; 
	public Lector(String nombreLector, String ip, String puerto) {
		this.nombreLector = nombreLector;
		this.ip = ip;
		this.puerto = puerto;
		this.isConnected = false;
		this.lector = new Reader(nombreLector, "TCPIP_Client", this.ip + ":" + this.puerto);
	}
	

	public boolean conectar() {
		boolean intentandoConectar = true;
	
		try {
			System.out.println("Intentando conectar");
			while(!lector.connect()) {
				Thread.sleep(2000);	
				System.out.println("Conexión fallida, intentando de nuevo..");
			}
			int delay = 0;   // delay for 5 sec.
			  int interval = 500;  // iterate every sec.
			  timerCambioTag = new Timer();
			  timerCambioTag.scheduleAtFixedRate(new TimerTask() {
			          public void run() {
			        	
			  			try {
			  				if(!lector.isConnected()) {
			  					return;
			  				}
			  				
			  				lector.send(new invengo.javaapi.protocol.IRP1.PowerOff());
				  			lector.send(new invengo.javaapi.protocol.IRP1.PowerOn((byte) 143));
				  			if(es6C) {
				  				lector.send(new ReadTag(ReadTag.ReadMemoryBank.ID_6B));
				  				es6C = false;
				  			}else {
				  				lector.send(new ReadTag(ReadTag.ReadMemoryBank.EPC_6C));
				  				es6C = true;
				  			}
			  			}catch(Exception e) {
			  				
			  			}
			  			
			          }
			  }, delay, interval);
			
			
			//this.mensajeEscaneo.setAntenna((byte) 143);
			System.out.println("Lector conectado!");
			lector.send(this.mensajeEscaneo);
			lector.onMessageNotificationReceived.add(this);
			lector.onApiException.add(this);
			System.out.println("Iniciando Lectura");
			this.isConnected = true;
			CheckConnTask();
		}catch(InterruptedException e)
		{
			
			System.out.println("Error al intentar conectar");
		}
		return false;
	}
	
	
	public void CheckConnTask() {

		timerCheckConn = new TimerTask() {

			@Override
			public void run() {
				while (lector.isConnected()) {

					//Sustituir por nueva ubicacion
					if (!Utilidades.pingTest(ip + ":" + puerto)) {
						desconectar();
						conectar();
						checkConnTimer.cancel();
					}

					try {
						Thread.sleep(15000);
					} catch (InterruptedException e) {
					}
				}
			}
		};

		checkConnTimer.schedule(timerCheckConn, 2000);

	}
	
	public void desconectar() {
		if (lector != null && lector.isConnected()) {
			lector.onMessageNotificationReceived.remove(this);
			this.timerCambioTag.cancel();
			lector.disConnect();
			System.out.println("Lector desconectado");
			//this.checkConnTimer.cancel();
		}
	}
	
	
	public void addListener(FoundTagListener toAdd) {
		TagFoundListeners.add(toAdd);
	}
	
	public void addAntennaListener(IAntennaListener toAdd) {
		AntennaListeners.add(toAdd);
	}
	
	
	public String getPuerto() {
		return puerto;
	}
	public void setPuerto(String puerto) {
		if(!isConnected) {
			this.puerto = puerto;	
		}
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		if(!isConnected) {
			this.ip = ip;	
		}
	}


	@Override
	public void messageNotificationReceivedHandle(BaseReader reader, IMessageNotification msg) {
		if(msg.getStatusCode() != 0) {
			String errorInfo = msg.getErrInfo();
			System.out.println("Ha ocurrido un error al leer el tag, codigo de error: " + errorInfo);
			return;
		}
		System.out.println("Mensaje de antena recibido");
		
		for(IAntennaListener listener : AntennaListeners) {
			listener.MessageSent();
		}
		
		
		String tipoMensaje = msg.getMessageType();
		System.out.println(tipoMensaje);
		tipoMensaje = tipoMensaje.substring(tipoMensaje.lastIndexOf('.') + 1);
		switch(tipoMensaje){
			case "RXD_TagData":
				System.out.println("Tag detectado");
				invengo.javaapi.protocol.IRP1.RXD_TagData datosTag = (invengo.javaapi.protocol.IRP1.RXD_TagData) msg;
				FoundTagListenerEventArgs args = new FoundTagListenerEventArgs(Util.convertByteArrayToHexString(datosTag.getReceivedMessage().getTID()),
													Util.convertByteArrayToHexString(datosTag.getReceivedMessage().getEPC()), 
													datosTag.getReceivedMessage().getAntenna());
				for(FoundTagListener listener : TagFoundListeners) {
					listener.TagFound(args);
				}
				
				break;
			case "RXD_IOTriggerSignal_800":
				System.out.println("Señal de disparador recibida");
			break;
			
		}
	}


	@Override
	public void apiExceptionHandle(String arg0) {
		System.out.println("Ocurrio un error");
		System.out.println(arg0);
	}
	
}
