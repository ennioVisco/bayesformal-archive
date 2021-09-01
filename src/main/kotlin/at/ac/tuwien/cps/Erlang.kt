package at.ac.tuwien.cps

import at.ac.tuwien.cps.spec.*
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.statistics.StatisticalModelChecker
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils.createLocServiceStatic

private const val RESULT = "_smc_grid_21x21_T_144.csv"



fun main(args: Array<String>) {
    logger.info{"Running Erlang class"}
    selectModel(args)
    logger.info{"The Network size is: ${network.size()}"}

    val locSvc = createLocServiceStatic(0.0, 1.0,
                                        multiTrace.timePoints.toDouble(),
                                        network
                                        )
    val trajectories = loadTrajectories(network.size(), TRACES)

    // Fake property to plot real data
    smc(phi0(), "real", trajectories, locSvc)

    smc(phi1(SATISFACTION), "s_p1", trajectories, locSvc)
    smc(phi1(ROBUSTNESS), "r_p1", trajectories, locSvc)
    smc(phi2(SATISFACTION), "s_p2", trajectories, locSvc)
    smc(phi2(ROBUSTNESS), "r_p2", trajectories, locSvc)
    smc(phi3(SATISFACTION), "s_p3", trajectories, locSvc)
    smc(phi3(ROBUSTNESS), "r_p3", trajectories, locSvc)
    smc(phi4(SATISFACTION), "s_p4", trajectories, locSvc)
    smc(phi4(ROBUSTNESS), "r_p4", trajectories, locSvc)

    logger.info{"Operations completed. Exiting."}
}

private fun <D> smc(
    p: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>,
    id: String, trajectories: Collection<MultiValuedTrace>,
    locService: LocationService<Double, Double>
    )
{
    //Statistical Model Checking
    val smc = StatisticalModelChecker(p, trajectories, locService)
    smc.compute()
    val stats = smc.stats
    val avg = filterAverage(stats)
    val `var` = filterVariance(stats)
    val str = RawTrajectoryExtractor(network.size())
    DataWriter(outputFile(id, "avg"), FileType.CSV, str).write(avg)
    DataWriter(outputFile(id, "var"), FileType.CSV, str).write(`var`)
    logger.info("SMC results computed.")
}


private fun outputFile(ext1: String, ext2: String): String {
    val trace = "output/$DATA_DIR${ext1}_${ext2}_K${K.toInt()}$RESULT"
    logger.info { "Saving output in: $trace" }
    return trace
}



