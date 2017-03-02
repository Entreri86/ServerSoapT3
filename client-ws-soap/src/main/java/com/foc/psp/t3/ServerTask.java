package com.foc.psp.t3;



import java.io.*;
import net.webservicex.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.jdom2.Document;         // |
import org.jdom2.Element;          // |\ Librerías
import org.jdom2.JDOMException;    // |/ JDOM
import org.jdom2.input.SAXBuilder; // |

public class ServerTask extends Thread {
	//Atributos para guardar las rutas de los ficheros en formato .xml (para uso en otras labores)
	//y en formato .txt . 
	private static final String XML = "result.xml";
	private static final String TXT = "result.txt";
	private Socket socket;
	//Atributos para guardar el log de errores y de ejecucion.
	private MyLog_2_0 runLog;
	private MyLog_2_0 crashLog;		
    /**
     * Constructor de la clase que recibe por parametro el socket con el que conectar y el log de ejecucion.
     * @param iSocket Socket con el cual conectar.
     * @param iRunLog Log donde almacenar las ejecuciones.
     */
	public ServerTask(Socket iSocket, MyLog_2_0 iRunLog) {
		this.socket = iSocket;
		this.runLog = iRunLog;
		crashLog = new MyLog_2_0();		
	}
	/**
	 * Metodo encargado de ejecutar la tarea del hilo.
	 */
	@Override
	public void run() {
		attendClient();				
	}
	/**
	 * Metodo encargado de gestionar las tareas a realizar por el servidor. Recibe una peticion
	 * y lanza una peticion a un webService para obtener la informacion solicitada por el usuario.
	 */
	private void attendClient (){
		BufferedReader bReader= null; 
		PrintWriter pWriter = null;
		DataInputStream dis = null;
		DataOutputStream dos = null;
		try {
			runLog.saveLog("Iniciando conexion con el cliente...");
			dis = new DataInputStream(socket.getInputStream());//Para recibir datos del cliente.
			dos = new DataOutputStream(socket.getOutputStream());//Para comunicarnos con el cliente.			
			runLog.saveLog("Leyendo datos del cliente...");
			String clientString = dis.readUTF();//Leemos pais...
			runLog.saveLog("Recopilando informacion del cliente...");
			String cities = getWeather(clientString);//Recogemos la cadena con el xml a tratar.
			if (cities == null) {				
				crashLog.saveLog("La cadena de ciudades(String) es null, revisa ServerTask.");
			}
			runLog.saveLog("Guardando en XML...");
			saveInFile(cities,XML);//Guardamos el resultado en un fichero para tratarlo.			
			runLog.saveLog("Guardado!");
			ArrayList<String> citiesList = extractCities();//Recogemos tan solo los valores del XML.
			if (citiesList == null) {
				crashLog.saveLog("La lista de ciudades (ArrayList) es null, revisa ServerTask.");
			}
			runLog.saveLog("Guardando en TXT...");
			saveInFile(citiesList, TXT);//Guardamos en fichero las ciudades.
			runLog.saveLog("Guardado!");
			String [] cList = citiesList.toArray(new String [citiesList.size()]);
			runLog.saveLog("Enviando datos al cliente...");
			for (int i =0; i<cList.length;i++){
				dos.writeUTF(cList[i]);//Enviamos los datos obtenidos al cliente				
		    }
			runLog.saveLog("Datos enviados!");
		} catch (IOException ioe) {
			crashLog.saveLog("Error de E/S con mensaje: "+ioe.getMessage());
			ioe.printStackTrace();
		} finally {//Tanto si tiene exito como no... cerramos canales.
			runLog.saveLog("Cerrando Streams...");
			if (bReader != null) crashLog.tryClose(bReader);
			if (pWriter != null) crashLog.tryClose(pWriter);
		}
		
	}
	/**
	 * Metodo encargado de dada una ruta y una cadena de texto por parametro guardar 
	 * la cadena en un fichero con la ruta.
	 * @param cities cadena de texto a guardar.
	 * @param fileName ruta con el nombre del fichero.
	 */
	private void saveInFile (String cities,String fileName){		
		FileWriter file = null;//Iniciamos a null para comprobacion final.		
		try {
			file = new FileWriter(fileName);//Iniciamos fichero
			file.write(cities);//Guardamos valores.					
		} catch (FileNotFoundException e) {			
			crashLog.saveLog("Error de E/S con mensaje: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			crashLog.saveLog("Error de E/S con mensaje: "+e.getMessage());
			e.printStackTrace();
		} finally {			
			if (file != null) crashLog.tryClose(file);//Cerramos stream con fichero. 
		}
		
	}
	/**
	 * Metodo encargado de dada una ruta y una cadena de texto por parametro guardar 
	 * la cadena en un fichero con la ruta.
	 * @param list lista con los valores a guardar.
	 * @param fileName ruta con el nombre de fichero a crear/actualizar.
	 */
	private void saveInFile (ArrayList <String> list,String fileName){
		FileWriter file = null;//Iniciamos a null para comprobacion final.		
		try {
			file = new FileWriter(fileName);//Iniciamos fichero
			for (int i =0; i<list.size();i++){//Bucle para guardar cada posicion de la lista en el fichero.
				file.write(list.get(i));
			}									
		} catch (FileNotFoundException e) {
			crashLog.saveLog("Error de E/S con mensaje: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			crashLog.saveLog("Error de E/S con mensaje: "+e.getMessage());
			e.printStackTrace();
		} finally {			
			if (file != null) crashLog.tryClose(file);//Cerramos stream con fichero. 
		}
	}	
	/**
	 * Metodo encargado de extraer los valores del fichero XML y convertirlo a un <b>ArrayList</b>.
	 * @return <b>ArrayList</b> con los valores de las ciudades.
	 */
	private ArrayList<String> extractCities (){
		ArrayList<String> citiesList = null;//Iniciamos a null para el retorno.
		SAXBuilder builder = new SAXBuilder();//Builder para parsear XML de la Libreria JDOM.
		try {
			citiesList = new ArrayList<String>();//Iniciamos ArrayList y Fichero xml.
			File xml = new File(XML);
			Document document = builder.build(xml); 
			Element root = document.getRootElement();//Elemento NewDataSet, root.
			List childList = root.getChildren("Table");//Cogemos cada Table.			
			//Primer bucle para recorrer todos los elementos Table.
			for (int i = 0; i < childList.size(); i++) {
				Element childCity = (Element) childList.get(i);//Recogemos un Table individual.							
				List cityList = childCity.getChildren();//Recogemos la lista de Hijos Country y City				
				//Segundo bucle para recorrer los campos que contiene Table (Country y City).
				for (int j = 0; j < cityList.size(); j++) {
					Element element = (Element) cityList.get(j);//Recogemos un campo...
					if (element.getName().equals("City")) {//Si es city nos interesa...
						String value = element.getText();//Recogemos el valor de City, el de Country no interesa.
						citiesList.add(value);//Añadimos a la lista.						
					}										
				}
			}		
		} catch (IOException ioe) {
			crashLog.saveLog("Error de E/S con mensaje: "+ioe.getMessage());
			ioe.printStackTrace();
		} catch (JDOMException e) {
			crashLog.saveLog("Error de JDOMException con mensaje: "+e.getMessage());
			e.printStackTrace();
		}
		return citiesList;//Retornamos la lista.
	}	
	/**
	 * Metodo encargado de obtener y retornar una cadena de texto con las ciudades del pais
	 * dado por parametro.
	 * @param country Pais de donde se deben de obtener los paises. 
	 * @return cadena de texto con lista de paises.
	 */
	private String getWeather(String country) {		
		String wResult = null;
		GlobalWeatherSoap gws = new GlobalWeather().getGlobalWeatherSoap();//Iniciamos Objeto GlobalWeather referenciando a GlobalWeatherSoap.
		wResult = gws.getCitiesByCountry(country);//Recogemos la consulta en un String.
		if (wResult == null) crashLog.saveLog("La cadena "+wResult+" es null, revisa getWeather.");
		return wResult;//Retornamos la cadena.		
	}
}
