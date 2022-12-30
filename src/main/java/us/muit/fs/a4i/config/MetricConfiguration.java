package us.muit.fs.a4i.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Logger;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.Json;
import javax.json.JsonArray;
import java.io.InputStreamReader;

public class MetricConfiguration implements MetricConfigurationI{
  
	private static Logger log = Logger.getLogger(Checker.class.getName());

	private HashMap<String,String> isDefinedMetric(String metricName, String metricType, InputStreamReader isr)
			throws FileNotFoundException {
		
		HashMap<String,String> metricDefinition=null;

	
		JsonReader reader = Json.createReader(isr);
		log.info("Creo el JsonReader");

		JsonObject confObject = reader.readObject();
		log.info("Leo el objeto");
		reader.close();

		log.info("Muestro la configuraci�n le�da " + confObject);
		JsonArray metrics = confObject.getJsonArray("metrics");
		log.info("El n�mero de m�tricas es " + metrics.size());
		for (int i = 0; i < metrics.size(); i++) {
			log.info("nombre: " + metrics.get(i).asJsonObject().getString("name"));
			if (metrics.get(i).asJsonObject().getString("name").equals(metricName)) {
				log.info("Localizada la m�trica");
				log.info("tipo: " + metrics.get(i).asJsonObject().getString("type"));
				if (metrics.get(i).asJsonObject().getString("type").equals(metricType)) {
					metricDefinition=new HashMap<String,String>();
					metricDefinition.put("description", metrics.get(i).asJsonObject().getString("description"));
					metricDefinition.put("unit", metrics.get(i).asJsonObject().getString("unit"));
				}

			}
		}

		return metricDefinition;
	}
   
    @Override	
	public HashMap<String,String> definedMetric(String name, String type) throws FileNotFoundException {
		log.info("Checker solicitud de b�squeda m�trica " + name);
		
		HashMap<String,String> metricDefinition=null;
		
		String filePath="/"+Context.getDefaultRI();
		log.info("Buscando el archivo " + filePath);
		InputStream is=this.getClass().getResourceAsStream(filePath);
		log.info("InputStream "+is+" para "+filePath);
		InputStreamReader isr = new InputStreamReader(is);
		
	/**
	 * Busca primero en el fichero de configuraci�n de m�tricas por defecto
	 */
		metricDefinition = isDefinedMetric(name, type, isr);
		/**
		 * En caso de que no estuviera ah� la m�trica busco en el fichero de configuraci�n de la aplicaci�n
		 */
		if ((metricDefinition==null) && Context.getAppRI() != null) {
			is=new FileInputStream(Context.getAppRI());
			isr=new InputStreamReader(is);			
			metricDefinition = isDefinedMetric(name, type, isr);
		}

		return metricDefinition;
	}

	@Override
	public List<String> listAllMetrics() throws FileNotFoundException {
        log.info("Consulta todas las m�tricas");
		
		List<String> allmetrics=new ArrayList<String>();
		
		String filePath="/"+Context.getDefaultRI();
		log.info("Buscando el archivo " + filePath);
		InputStream is=this.getClass().getResourceAsStream(filePath);
		log.info("InputStream "+is+" para "+filePath);
		InputStreamReader isr = new InputStreamReader(is);
		
		JsonReader reader = Json.createReader(isr);
		log.info("Creo el JsonReader");

		JsonObject confObject = reader.readObject();
		log.info("Leo el objeto");
		reader.close();

		log.info("Muestro la configuraci�n le�da " + confObject);
		JsonArray metrics = confObject.getJsonArray("metrics");
		log.info("El n�mero de m�tricas es " + metrics.size());
		for (int i = 0; i < metrics.size(); i++) {
			log.info("A�ado nombre: " + metrics.get(i).asJsonObject().getString("name"));
			allmetrics.add(metrics.get(i).asJsonObject().getString("name"));
		}
		if(Context.getAppRI() != null) {
			is=new FileInputStream(Context.getAppRI());
			isr=new InputStreamReader(is);	
			reader = Json.createReader(isr);
			confObject = reader.readObject();
			reader.close();

			log.info("Muestro la configuraci�n le�da " + confObject);
			metrics = confObject.getJsonArray("metrics");
			log.info("El n�mero de m�tricas es " + metrics.size());
			for (int i = 0; i < metrics.size(); i++) {
				log.info("A�ado nombre: " + metrics.get(i).asJsonObject().getString("name"));
				allmetrics.add(metrics.get(i).asJsonObject().getString("name"));
			}
		}
		
		return allmetrics;
	} 
}