/**
 * 
 */
package us.muit.fs.a4i.model.remote;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHRepositoryStatistics;
import org.kohsuke.github.GHRepositoryStatistics.CodeFrequency;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GitHub;

import us.muit.fs.a4i.exceptions.MetricException;
import us.muit.fs.a4i.exceptions.ReportItemException;
import us.muit.fs.a4i.model.entities.Report;
import us.muit.fs.a4i.model.entities.ReportI;
import us.muit.fs.a4i.model.entities.ReportItem;
import us.muit.fs.a4i.model.entities.ReportItem.ReportItemBuilder;

/**
 * @author Juan Luis Verdugo Blanco
 *
 */
public class MetricasG6 extends GitHubEnquirer {
	private static Logger log = Logger.getLogger(MetricasG6.class.getName());

	/**
	 * <p>
	 * Constructor
	 * </p>
	 */

	public MetricasG6() {
		super();
		metricNames.add("clasificacion");
		metricNames.add("correccion");
		metricNames.add("validacion");
		metricNames.add("num_errores");
		log.info("A�adidas m�tricas al IndicatorEnquirer");
	}

	@Override
	public ReportI buildReport(String repositoryId) {
		ReportI myRepo = null;
		log.info("Invocado el m�todo que construye un objeto RepositoryReport");
		/**
		 * <p>
		 * Información sobre el repositorio obtenida de GitHub
		 * </p>
		 */
		GHRepository remoteRepo;
		/**
		 * <p>
		 * En estos momentos cada vez que se invoca construyeObjeto se crea y rellena
		 * uno nuevo
		 * </p>
		 * <p>
		 * Deuda técnica: se puede optimizar consultando sólo las diferencias respecto a
		 * la fecha de la última representación local
		 * </p>
		 */

		try {
			log.info("Nombre repo = " + repositoryId);

			GitHub gb = getConnection();
			remoteRepo = gb.getRepository(repositoryId);
			log.info("El repositorio es de " + remoteRepo.getOwnerName() + " Y su descripción es "
					+ remoteRepo.getDescription());
			log.info("leído " + remoteRepo);
			myRepo = new Report(repositoryId);
			
			List<GHIssue> openIssues = remoteRepo.getIssues(GHIssueState.ALL);
			int numTotIssues = remoteRepo.getIssues(GHIssueState.ALL).size();
			log.info("Total de errores:" + numTotIssues);
			long abierto=0;
			long cerrado=0;
			long cierreMilestone=0;
			for (GHIssue issue: openIssues) {
				abierto = abierto + issue.getCreatedAt().getTime()/3600000;
				cerrado = cerrado + issue.getClosedAt().getTime()/3600000;
				cierreMilestone = cierreMilestone + issue.getMilestone().getClosedAt().getTime()/3600000;
			}
			/*GHIssue primerIssuePrueba = openIssues.get(1);
			Date abierto = primerIssuePrueba.getCreatedAt();
			Date cerrado = primerIssuePrueba.getClosedAt();
		    GHMilestone milestone = primerIssuePrueba.getMilestone();
		    Date cierreMilestone = milestone.getClosedAt();
		    */
		    
			long tiempoClasificacion = cierreMilestone - abierto;
			long tiempoIssue =  cerrado - abierto;
			log.info("Tiempo de clasificacion:" + tiempoClasificacion);
			log.info("Tiempo de correccion:" + tiempoIssue);
			
			

			/**
			 * Métricas directas de tipo conteo
			 */

			/*
			 * MetricBuilder<Integer> subscribers = new
			 * Metric.MetricBuilder<Integer>("subscribers",
			 * remoteRepo.getSubscribersCount());
			 */
			
			 
			ReportItemBuilder<Long> clasificacion = new ReportItem.ReportItemBuilder<Long>("clasificacion",
					tiempoClasificacion);
			clasificacion.source("GitHub");
			myRepo.addMetric(clasificacion.build());
			log.info("Añadida métrica clasificacion " + clasificacion);
			 
			/*
			 * MetricBuilder<Integer> forks = new Metric.MetricBuilder<Integer>("forks",
			 * remoteRepo.getForksCount()); forks.source("GitHub");
			*/ 
			ReportItemBuilder<Long> correccion = new ReportItem.ReportItemBuilder<Long>("correccion",
					tiempoIssue);
			correccion.source("GitHub");
			myRepo.addMetric(correccion.build());
			log.info("Añadida métrica correccion " + correccion);
			
			/*
			 * MetricBuilder<Integer> watchers = new
			 * Metric.MetricBuilder<Integer>("watchers", remoteRepo.getWatchersCount());
			 */
			ReportItemBuilder<Integer> num_errores = new ReportItem.ReportItemBuilder<Integer>("num_errores",
					numTotIssues);
			num_errores.source("GitHub");
			myRepo.addMetric(num_errores.build());


		} catch (Exception e) {
			log.severe("Problemas en la conexión " + e);
		}

		return myRepo;
	}

	/**
	 * Permite consultar desde fuera una métrica del repositorio indicado
	 */

	@Override
	public ReportItem getMetric(String metricName, String repositoryId) throws MetricException {
		GHRepository remoteRepo;

		GitHub gb = getConnection();
		try {
			remoteRepo = gb.getRepository(repositoryId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MetricException(
					"No se puede acceder al repositorio remoto " + repositoryId + " para recuperarlo");
		}

		return getMetric(metricName, remoteRepo);
	}

	/**
	 * <p>
	 * Crea la métrica solicitada consultando el repositorio remoto que se pasa como
	 * parámetro
	 * </p>
	 * 
	 * @param metricName Métrica solicitada
	 * @param remoteRepo Repositorio remoto
	 * @return La métrica creada
	 * @throws MetricException Si la métrica no está definida se lanzará una
	 *                         excepción
	 */
	private ReportItem getMetric(String metricName, GHRepository remoteRepo) throws MetricException {
		ReportItem metric;
		if (remoteRepo == null) {
			throw new MetricException("Intenta obtener una métrica sin haber obtenido los datos del repositorio");
		}
		switch (metricName) {
		case "totalAdditions":
			metric = getTotalAdditions(remoteRepo);
			break;
		case "totalDeletions":
			metric = getTotalDeletions(remoteRepo);
			break;
		default:
			throw new MetricException("La métrica " + metricName + " no está definida para un repositorio");
		}

		return metric;
	}

	/*
	 * A partir de aquí los algoritmos específicos para hacer las consultas de cada
	 * métrica
	 */

	/**
	 * <p>
	 * Obtención del número total de adiciones al repositorio
	 * </p>
	 * 
	 * @param remoteRepo el repositorio remoto sobre el que consultar
	 * @return la métrica con el número total de adiciones desde el inicio
	 * @throws MetricException Intenta crear una métrica no definida
	 */
	private ReportItem getTotalAdditions(GHRepository remoteRepo) throws MetricException {
		ReportItem metric = null;

		GHRepositoryStatistics data = remoteRepo.getStatistics();
		List<CodeFrequency> codeFreq;
		try {
			codeFreq = data.getCodeFrequency();

			int additions = 0;

			for (CodeFrequency freq : codeFreq) {

				if (freq.getAdditions() != 0) {
					Date fecha = new Date((long) freq.getWeekTimestamp() * 1000);
					log.info("Fecha modificaciones " + fecha);
					additions += freq.getAdditions();

				}
			}
			ReportItemBuilder<Integer> totalAdditions = new ReportItem.ReportItemBuilder<Integer>("totalAdditions",
					additions);
			totalAdditions.source("GitHub, calculada")
					.description("Suma el total de adiciones desde que el repositorio se creó");
			metric = totalAdditions.build();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReportItemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metric;

	}

	/**
	 * <p>
	 * Obtención del número total de eliminaciones del repositorio
	 * </p>
	 * 
	 * @param remoteRepo el repositorio remoto sobre el que consultar
	 * @return la métrica con el n�mero total de eliminaciones desde el inicio
	 * @throws MetricException Intenta crear una métrica no definida
	 */
	private ReportItem getTotalDeletions(GHRepository remoteRepo) throws MetricException {
		ReportItem metric = null;

		GHRepositoryStatistics data = remoteRepo.getStatistics();
		List<CodeFrequency> codeFreq;
		try {
			codeFreq = data.getCodeFrequency();

			int deletions = 0;

			for (CodeFrequency freq : codeFreq) {

				if (freq.getDeletions() != 0) {
					Date fecha = new Date((long) freq.getWeekTimestamp() * 1000);
					log.info("Fecha modificaciones " + fecha);
					deletions += freq.getAdditions();

				}
			}
			ReportItemBuilder<Integer> totalDeletions = new ReportItem.ReportItemBuilder<Integer>("totalDeletions",
					deletions);
			totalDeletions.source("GitHub, calculada")
					.description("Suma el total de eliminaciones desde que el repositorio se cre�");
			metric = totalDeletions.build();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReportItemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return metric;

	}

}