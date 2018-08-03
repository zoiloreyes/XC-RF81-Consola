package ESPReaderConsola;

import invengo.javaapi.protocol.IRP1.ReadTag;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;


import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

public class ESPReaderConsola {

	private ReadTag mensajeEscaaneo = new ReadTag(ReadTag.ReadMemoryBank.EPC_TID_UserData_6C_ID_UserData_6B);
	private String[] argumentos;
	private static boolean funcional = false;
	public static void main(String[] args) {
		
		while(true) {
			try {
				if(!funcional) {
					iniciar(args);	
				}
			}catch(Exception e ) {
				
			}
		}
		
		
	}
	
	public static void iniciar(String[] args) {
		try {
			funcional = true;
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
						
						//System.out.println(espc.mensajeEscaaneo.getAntenna());
						
						//Estado de lector sin antenas en bits 1000 0000
						//Cada uno de los ultimos bits representa una antena
						//Antena #1 y antena #3 = 1000 0101
						//Antena #2 y antena #3 - 1000 0110
						//Todas = 1000 1111 = 143
						
						
						//espc.mensajeEscaaneo.setAntenna((byte) 0x00);
						
						System.out.println(espc.mensajeEscaaneo.getAntenna());
						//System.out.println(espc.mensajeEscaaneo.getAntenna());
						espReader.send(espc.mensajeEscaaneo);
						System.out.println("Enviando");
	                }
				}else {
					funcional = false;
					System.out.println("No se pudo conectar");
				}
			}else {
				
				System.out.println("Numero incorrecto de parametros"); 
			}
		}catch(Exception e) {
			funcional = false;
		}
		
	}
	
	private class OnMessageNotificationReceived implements invengo.javaapi.handle.IMessageNotificationReceivedHandle{

		@Override
		public void messageNotificationReceivedHandle(BaseReader reader, IMessageNotification msg) {
		try {
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
	       
		}catch(Exception e) {
			funcional = false;
		}
			
			
		}
		
	}
	private void send(invengo.javaapi.protocol.IRP1.RXD_TagData msg)
    {
		try {
			String nombreReader = msg.getReceivedMessage().getReaderName();
			String epc = Util.convertByteArrayToHexString(msg.getReceivedMessage().getEPC());
	        String tid = Util.convertByteArrayToHexString(msg.getReceivedMessage().getTID());
	        String userdata = Util.convertByteArrayToHexString(msg.getReceivedMessage().getUserData());
	        byte Antena = msg.getReceivedMessage().getAntenna();
	        
	        String identificador = isNullOrWhiteSpace(tid) ? epc : tid;
	        
	        
	      //Imprimir por consola
	        Boolean imprimir = false;
	        if(argumentos.length > 4 && Boolean.parseBoolean(argumentos[4])) {
	        	imprimir = Boolean.parseBoolean(argumentos[4]);
	        }
	        
	        String mensaje = Antena + "_" + identificador;
	        
	        //Parametros del programa
	        String rutaPrograma = argumentos[1];
	        String h = argumentos[2];
	        String t = argumentos[3];
	        
	        ArrayList<String> comandoList = new ArrayList<String>();
	        
	        comandoList.add(rutaPrograma);
	        comandoList.add("-m");
	        comandoList.add(mensaje);
	        comandoList.add("-h");
	        comandoList.add(h);
	        comandoList.add("-t");
	        comandoList.add(t);
	        
	        String [] comandosArray = comandoList.toArray(new String[comandoList.size()]);
	        try {
	        	if(imprimir) {
	        		System.out.println(mensaje);
	        	}
	        	Runtime.getRuntime().exec(comandosArray);
	        }catch(Exception e) {
	        	
	        }

		}catch(Exception e) {
			funcional = false;
		}
		
		
    }
	
	public static boolean isNullOrWhiteSpace(String cadena) {
		return cadena == null || cadena.isEmpty() || cadena.trim().length() == 0; 
	}



}
