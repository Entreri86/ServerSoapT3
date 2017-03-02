package com.foc.psp.t3;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by Fernando Otal Barja.
 */
public class MyLog_2_0 {
	/**
	 * Atributos necesarios para la Clase.
	 */
	private Date baseDate;   
    private String name;
    private FileWriter file;
    /**
     * Variables necesarias para el Formato de la Fecha.
     */
    private final String forHour = "HH:mm:ss";
    private final String forDate = "dd/MM/yyyy";
    private final String NAME_LOG = "log.txt";
    private DateFormat currentHour = new SimpleDateFormat (forHour);
    private DateFormat currentDate = new SimpleDateFormat(forDate);   
    /**
     * Constructor por defecto.
     */
    public MyLog_2_0(){
    	this.name = NAME_LOG;
    }
    /**
     * Constructor adicional con nombre del fichero por parametro.
     * @param nombre nombre del fichero.
     */
    public MyLog_2_0(String nombre){
        setNombre(nombre);
    }
    /**
     * Getter del atributo nombre.
     * @return nombre del fichero.
     */
    public String getNombre() {
        return name;
    }
    /**
     * Setter del atributo nombre.
     * @param nombre nombre del fichero.
     */
    public void setNombre(String nombre) {
        this.name = nombre;
    }

    /**
     * Metodo encargado de escribir en un fichero el mensaje a modo de log.
     * @param mensaje del fallo.
     */
    public void saveLog (String mensaje){
    	baseDate = new Date();//Iniciamos variable para utilizarla en metodos posteriores.
        if (!new File(name).exists()){
        	//Si no existe... lo creamos.
            createLog(mensaje);
        } else {
        	//Si existe lo actualizamos
            updateLog(mensaje);
        }

    }

    /**
     * Metodo encargado de crear el fichero en caso de no existir.
     * @param mensaje mensaje a guardar.
     */
    private void createLog (String mensaje){
        try {
            file = new FileWriter(new File(name));//Creamos fichero mediante canal FileWriter
            //Formatamos la cadena con la ayuda de DateFormat y SimpleDateFormat para mostrar los datos del instante del error.
            file.write("Fecha: "+currentDate.format(baseDate)+"\n Hora: "+currentHour.format(baseDate)+" "+mensaje+" ");
        } catch (IOException e) {
            //No se deberia de llegar nunca... Para que el compilador no se queje.
            e.printStackTrace();
        } finally {
            tryClose(file);//Intentamos cerrar el fichero utilizando el metodo try/close.            
        }
    }

    /**
     * Metodo encargado de actualizar el contenido del fichero log.txt.
     * @param mensaje mensaje a guardar.
     */
    private void updateLog (String mensaje){
        try {
            file = new FileWriter(new File (name), true);//Segundo parametro a true para añadir al final del fichero.
            //Formatamos la cadena con la ayuda de DateFormat y SimpleDateFormat para mostrar los datos del instante del mensaje/error.
            file.write("Fecha: "+currentDate.format(baseDate)+"\n Hora: "+currentHour.format(baseDate)+" "+mensaje+" ");
        } catch (IOException e) {
            //No se deberia de llegar nunca... Para que el compilador no se queje.
            e.printStackTrace();
        } finally {            
            tryClose(file);//Intentamos cerrar el fichero utilizando el metodo try/close.           
        }
    }
    
    /**
     * Metodo encargado de intentar cerrar el canal introducido por parametro.
     * @param cl Objecto Closeable para cerrar canal.     
     */
    public void tryClose (Closeable cl){
        try {
            if ( cl !=null){
                cl.close();
            }
        } catch (IOException ex){
        	saveLog (ex.getMessage());//Guardamos el mensaje de error de E/S.
            ex.printStackTrace();//Mostramos traza del error.
        }
    } 
          

}
