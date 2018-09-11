package ESPReaderConsola;

import java.util.ArrayList;
import java.util.List;

import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

public class Lector implements invengo.javaapi.handle.IMessageNotificationReceivedHandle{
	private String nombre;
	private String ip;
	private String puerto;
	private boolean conectado;
	private Reader reader;
	private ReadTag mensajeEscaneo = new ReadTag(ReadTag.ReadMemoryBank.EPC_TID_UserData_6C_ID_UserData_6B);
	private List<IFoundTagListener> listeners = new ArrayList<IFoundTagListener>();
	
	
	public Lector(String nombre, String ip, String puerto) {
		this.nombre = nombre;
		this.puerto = puerto;
		this.ip = ip;
		this.reader = new Reader(nombre,"TCPIP_Client", ip + ":" + puerto);	
		this.reader.onMessageNotificationReceived.add(this);
	}

	public void conectar() {
		System.out.println("Conectando...");
		
		while(!reader.connect()) {
			try {
				System.out.println("No se puedo conectar. Intentando de nuevo...");
				Thread.sleep(2000);	
			}catch(Exception e) {
				System.out.println("Ocurrió un error en sleep");
			}
		}
		System.out.println("Conectado");
		
		System.out.println("Enviando mensaje...");
		this.reader.send(mensajeEscaneo);
		conectado = true;
		
	}
	public void addListener(IFoundTagListener toAdd) {
        listeners.add(toAdd);
    }

	@Override
	public void messageNotificationReceivedHandle(BaseReader readr, IMessageNotification msg) {
		if (msg.getStatusCode() != 0)// Si != 0  es un error
        {   
			String errorAsd = msg.getErrInfo();
			System.out.println("Ocurrio un error en la notificacion de mensaje: ");
			System.out.println(errorAsd);
            return;
        }
		String msgType = msg.getMessageType();
        msgType = msgType.substring(msgType.lastIndexOf('.') + 1);
        switch (msgType)
        {
            
            case "RXD_TagData":
                {
                	invengo.javaapi.protocol.IRP1.RXD_TagData ms = (invengo.javaapi.protocol.IRP1.RXD_TagData) msg;
                    String tagType = ms.getReceivedMessage().getTagType();
                    FoundTagListenerEventArgs eventArgs = new FoundTagListenerEventArgs(
                    		Util.convertByteArrayToHexString(ms.getReceivedMessage().getTID()),
                    		Util.convertByteArrayToHexString(ms.getReceivedMessage().getEPC()),
                    		ms.getReceivedMessage().getAntenna());
                    
                    for(IFoundTagListener listener : this.listeners) {
                    	listener.TagFound(eventArgs);
                    }                     
                }
                break;
            case "RXD_IOTriggerSignal_800":
                {
                	invengo.javaapi.protocol.IRP1.RXD_IOTriggerSignal_800 m = (invengo.javaapi.protocol.IRP1.RXD_IOTriggerSignal_800)msg;
                    if (m.getReceivedMessage().isStart())
                    {
                    	System.out.println("Trigger signal");
                                         
                    }
                    else
                    {                     
                    
                    }
                }
                break;          
        }
	}
}
