package ESPReaderConsola;

import invengo.javaapi.protocol.IRP1.ReadTag;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;


import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

public class ESPReaderConsola {

	private ReadTag mensajeEscaaneo = new ReadTag(ReadTag.ReadMemoryBank.TID_6C_ID_6B);
	private String[] argumentos;
	public static void main(String[] args) {
		ESPReaderConsola espc = new ESPReaderConsola();
		if(args.length > 3) {
			espc.argumentos = args;
			String direccion = args[0];
			Reader espReader = new Reader("EspReader","TCPIP_Client",direccion);
			if(espReader.connect()) {
				
				System.out.println("Lector conectado");
				espReader.onMessageNotificationReceived.add(espc.new OnMessageNotificationReceived());
				if (espReader != null  && espReader.isConnected())
                {
					espReader.send(espc.mensajeEscaaneo);
					System.out.println("Enviando");
                }
			}else {
				System.out.println("No se pudo conectar");
			}
		}else {
			System.out.println("Numero incorrecto de parametros");
		}
		
	}
	private class OnMessageNotificationReceived implements invengo.javaapi.handle.IMessageNotificationReceivedHandle{

		@Override
		public void messageNotificationReceivedHandle(BaseReader reader, IMessageNotification msg) {
			if (msg.getStatusCode() != 0)// Si != 0  es un error
	        {   
				String errorAsd = msg.getErrInfo();
				System.out.println(errorAsd);
	            return;
	        }
	        String msgType = msg.getMessageType();
	        msgType = msgType.substring(msgType.lastIndexOf('.') + 1);
	        switch (msgType)
	        {
	            
	            case "RXD_TagData":
	                {
	                	invengo.javaapi.protocol.IRP1.RXD_TagData m = (invengo.javaapi.protocol.IRP1.RXD_TagData) msg;
	                    String tagType = m.getReceivedMessage().getTagType();
	                    send(m);                        
	                }
	                break;
	            case "RXD_IOTriggerSignal_800":
	                {
	                	invengo.javaapi.protocol.IRP1.RXD_IOTriggerSignal_800 m = (invengo.javaapi.protocol.IRP1.RXD_IOTriggerSignal_800)msg;
	                    if (m.getReceivedMessage().isStart())
	                    {
	                    	System.out.println("Trigger signal");
	                        //changeCtrlEnable("scan");
	                        //showMsg(" I/O trigger,scaning...");
	                    }
	                    else
	                    {
	                        //changeCtrlEnable("conn");
	                        //showMsg(" I/O trigger,stop");
	                    }
	                }
	                break;          
	        }
			
		}
		
	}
	private void send(invengo.javaapi.protocol.IRP1.RXD_TagData msg)
    {
		
		String nombreReader = msg.getReceivedMessage().getReaderName();
		String epc = Util.convertByteArrayToHexString(msg.getReceivedMessage().getEPC());
        String tid = Util.convertByteArrayToHexString(msg.getReceivedMessage().getTID());
        String userdata = Util.convertByteArrayToHexString(msg.getReceivedMessage().getUserData());
        
        
        //Parametros del programa
        String rutaPrograma = argumentos[1];
        String h = argumentos[2];
        String t = argumentos[3];
        
        ArrayList<String> comandoList = new ArrayList<String>();
        
        comandoList.add(rutaPrograma);
        comandoList.add("-m");
        comandoList.add(tid);
        comandoList.add("-h");
        comandoList.add(h);
        comandoList.add("-t");
        comandoList.add(t);
        
        String [] comandosArray = comandoList.toArray(new String[comandoList.size()]);
        try {
        	
        	Runtime.getRuntime().exec(comandosArray);
        }catch(Exception e) {
        	
        }
    }


}
