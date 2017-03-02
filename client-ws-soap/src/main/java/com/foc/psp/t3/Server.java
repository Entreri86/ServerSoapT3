package com.foc.psp.t3;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	/**
	 * Atributos necesarios para el funcionamiento de la clase.
	 */
	private int port = 8008;
	private static final int MAX_CLIENT = 1000;
	private boolean isActive = true;
	private MyLog_2_0 runLog;
	private MyLog_2_0 crashLog;
	/**
	 * 
	 * @param iPort
	 * @param iLog
	 */
	public Server (int iPort,MyLog_2_0 iLog){
		this.port = iPort;
		this.runLog = iLog;
		crashLog = new MyLog_2_0();
	}
	/**
	 * 
	 * @throws IOException
	 */
	public void sListen () throws IOException{
		int count = 0;		
		ServerSocket serverSocket = new ServerSocket(port);// Dispara IOException, recogida en MainClass.
		Socket socketCli = null;
		if (count == 0) runLog.saveLog("Iniciando escuchas en servidor con puerto: "+serverSocket.getLocalPort()+".");
		while (isActive == true) {
			count ++;//Aumentamos el contador para llevar un control y que no se devalue el rendimiento 
					 //al tener demasiadas peticiones.					 
			if (count <= MAX_CLIENT){
				socketCli = serverSocket.accept();// Dispara IOException, recogida en MainClass.
				ServerTask task = new ServerTask(socketCli, runLog);//Iniciamos tarea en un nuevo hilo
				task.start();
			} else {
				try {
					Thread.sleep(5000);//Esperamos 5 segundos, para que el server libere un socket.
				} catch (InterruptedException ie) {
					crashLog.saveLog("Error por interrupción,\nmensaje: "+ie.getMessage());
					ie.printStackTrace();
				}
			}
		}//Al llegar al final del metodo, cerramos los Streams.		
		if (socketCli != null) crashLog.tryClose(socketCli);				
		if (serverSocket != null) crashLog.tryClose(serverSocket);				
	}
	
	/**
	 * 
	 */
	public void serStop (){
		if (isActive == true) isActive = false;
	}	
	/**
	 * 
	 */
	public void serPlay (){
		if (isActive ==false) isActive = true;
	}
}
