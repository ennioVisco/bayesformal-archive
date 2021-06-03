import at.ac.tuwien.cps.*
import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor
import eu.quanticol.moonlight.io.parsing.PrintingStrategy
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.statistics.StatisticalModelChecker
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils.createLocServiceStatic

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}


private const val TRAJECTORIES = 1
private const val RESULT = "_smc_grid_21x21_T_144.csv"


/**
 * We initialize the domains and the spatial network
 * @see Grid for a description of the spatial model.
 */
private val network = Grid().getModel(path(NETWORK_FILE))

/**
 * Signal Dimensions (i.e. signal domain)
 */
private val processor = ErlangSignal(4)
private val multiTrace = MultiRawTrajectoryExtractor(network.size(), processor)



fun main() {
    logger.info{"The network size is: ${network.size()}"}

    val locSvc = createLocServiceStatic(0.0, 1.0,
                                        multiTrace.timePoints.toDouble(),
                                        network
                                        )
    val trajectories = loadTrajectories(network.size(), TRAJECTORIES)


//    smc(Formulae.phi1(SATISFACTION), "s_p1", trajectories, locSvc)
//    smc(Formulae.phi1(ROBUSTNESS), "r_p1", trajectories, locSvc)
//    //smc(real_phi1(SATISFACTION), "real_s_p1", trajectories, locService);
//    //smc(real_phi1(ROBUSTNESS), "real_r_p1", trajectories, locService);
//    smc(Formulae.phi2(SATISFACTION), "s_p2", trajectories, locSvc)
//    smc(Formulae.phi2(ROBUSTNESS), "r_p2", trajectories, locSvc)
//    //smc(real_phi3(SATISFACTION), "real_s_p3", trajectories, locService);
//    //smc(real_phi3(ROBUSTNESS), "real_r_p3", trajectories, locService);
    //smc(phi3(SATISFACTION), "s_p3", trajectories, locService);
    //smc(phi3(ROBUSTNESS), "r_p3", trajectories, locService);
    smc(phi4(SATISFACTION), "s_p4", trajectories, locSvc)
    smc(phi4(ROBUSTNESS), "r_p4", trajectories, locSvc)

    logger.info{"Operations completed. Exiting."}
}

private fun <D> smc(
    p: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>,
    id: String, trajectories: Collection<MultiValuedTrace>,
    locService: LocationService<Double, Double>
) {
    //Statistical Model Checking
    val smc = StatisticalModelChecker(p, trajectories, locService)
    smc.compute()
    val stats = smc.stats
    val avg = filterAverage(stats)
    val `var` = filterVariance(stats)
    val str: PrintingStrategy<Array<DoubleArray>> = RawTrajectoryExtractor(network.size())
    DataWriter(outputFile(RESULT, id, "avg"), FileType.CSV, str).write(avg)
    DataWriter(outputFile(RESULT, id, "var"), FileType.CSV, str).write(`var`)
    logger.info("SMC results computed.")
}


private fun outputFile(path: String, ext1: String, ext2: String): String {
    val trace = DATA_DIR + ext1 + "_" + ext2 + "_K" + K.toInt() + path
    logger.info { "Saving output in: $trace" }
    return trace
}



