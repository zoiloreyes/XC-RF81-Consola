package ESPReaderConsola;

import invengo.javaapi.protocol.IRP1.ReadTag;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import invengo.javaapi.core.*;
import invengo.javaapi.protocol.IRP1.*;
import invengo.javaapi.protocol.IRP1.Reader;

public class ESPReaderConsola implements FoundTagListener, IAntennaListener {

	private ReadTag mensajeEscaaneo = new ReadTag(ReadTag.ReadMemoryBank.EPC_TID_UserData_6C_2);
	public String[] argumentos;
	public static void main(String[] args) {
		String ip = args[0].split(":")[0];
		String puerto = args[0].split(":")[1];
		ESPReaderConsola app = new ESPReaderConsola();
		Lector nuevoLector = new Lector("LectorESP",ip,puerto);
		nuevoLector.conectar();
		
		
		int delay = 0;   // delay for 5 sec.
		  int interval = 1000;  // iterate every sec.
		  Timer timer = new Timer();
		   
		  timer.scheduleAtFixedRate(new TimerTask() {
		          public void run() {
		        	  String mensaje = "Alive ";
		      		String rutaPrograma = args[1];
		              String h = args[2];
		              String t = args[4];
		              
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
		              	Runtime.getRuntime().exec(comandosArray);
		              }catch(Exception e) {
		              	
		              };
		          }
		      }, delay, interval);
		app.argumentos = args;
		nuevoLector.addListener(app);
		//nuevoLector.addAntennaListener(app);
	}
	
	@Override
	public void TagFound(FoundTagListenerEventArgs ev){
		if(argumentos.length < 5) {
			System.out.println("Parametros insuficientes");
			
			return;
		}
		String mensaje = ev.getAntenna() + "_" +( isNullOrWhiteSpace(ev.getTagId()) ? ev.getEpc() : ev.getTagId());
		System.out.println(mensaje);	
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
        	Runtime.getRuntime().exec(comandosArray);
        }catch(Exception e) {
        	
        }
	}
	
	public static boolean isNullOrWhiteSpace(String cadena) {
		return cadena == null || cadena.isEmpty() || cadena.trim().length() == 0; 
	}

	@Override
	public void MessageSent() {
		if(argumentos.length < 5) {
			System.out.println("parametros insuficientes");
			return;
		}
		String mensaje = "Mensaje de antena recibido";
		String rutaPrograma = argumentos[1];
        String h = argumentos[2];
        String t = argumentos[4];
        
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
        	Runtime.getRuntime().exec(comandosArray);
        }catch(Exception e) {
        	
        }
	}



}
