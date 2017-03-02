package com.foc.psp.t3;

import java.io.IOException;

public class MainClass {
	/**
	 * Atributos necesarios para la classe.
	 */
	private static final int PORT = 8008;
	private static final String RUN_LOG = "run.txt";
	private MyLog_2_0 runLog;
	private MyLog_2_0 crashLog;
	
	/**
	 * Metodo encargado de iniciar la aplicacion.
	 * @param args Argumentos de la aplicacion.
	 */
	public static void main(String[] args) {		
		MainClass mainClass = new MainClass();
		mainClass.init();	
	}
	/**
	 * Metodo encargado de encender el Server y aceptar escuchas.
	 */
	public void init (){
		/**
		 * Iniciamos Log de errores y de ejecucion.
		 */
		crashLog = new MyLog_2_0();
		runLog = new MyLog_2_0(RUN_LOG);
		Server server = new Server(PORT,runLog);
		runLog.saveLog("Iniciamos el servidor...");
		while (true) {
			try {
				runLog.saveLog("Iniciamos escuchas...");
				server.sListen();//Aceptamos escuchas.
			} catch (IOException ioe) {
				crashLog.saveLog("Error con mensaje: "+ioe.getMessage());
				ioe.printStackTrace();
			}
			
		}
	}

}
