package ESPReaderConsola;
import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

import java.util.ArrayList;
import java.util.List;
import invengo.javaapi.protocol.IRP1.ReadTag;

public class Lector implements invengo.javaapi.handle.IMessageNotificationReceivedHandle, invengo.javaapi.handle.IApiExceptionHandle  {

	private String nombreLector;
	private String ip;
	private String puerto;
	private boolean isConnected;
	private Reader lector;
	private ReadTag mensajeEscaneo = new ReadTag(ReadTag.ReadMemoryBank.EPC_TID_UserData_6C_ID_UserData_6B);	
	private List<FoundTagListener> TagFoundListeners = new ArrayList<FoundTagListener>();
	private List<IAntennaListener> AntennaListeners = new ArrayList<IAntennaListener>();
	
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
			
			System.out.println("Lector conectado!");
			lector.send(this.mensajeEscaneo);
			lector.onMessageNotificationReceived.add(this);
			lector.onApiException.add(this);
			System.out.println("Iniciando Lectura");
			this.isConnected = true;
		}catch(InterruptedException e)
		{
			
			System.out.println("Error al intentar conectar");
		}
		return false;
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
