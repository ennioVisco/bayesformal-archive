package at.ac.tuwien.cps

import at.ac.tuwien.cps.spec.*
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.statistics.StatisticalModelChecker
import eu.quanticol.moonlight.util.MultiValuedTrace

private const val RESULT = "_smc_grid_21x21_T_144.csv"

fun main(args: Array<String>) {
    logger.info{"Running Erlang class"}
    val models = selectModels(args)
    runModels(models, ::smcAll)
}

private fun smcAll(locSvc: LocationService<Double, Double>, model: String) {
    val trajectories = loadTraces(network.size(), TRACES, model)

    // Fake property to plot real data
    smc(phi0(), "real", trajectories, locSvc, model)

    // P1
    smc(phi1(SATISFACTION), "s_p1", trajectories, locSvc, model)
    smc(phi1(ROBUSTNESS), "r_p1", trajectories, locSvc, model)
    smc(gp1(SATISFACTION), "s_gp1", trajectories, locSvc, model)
    smc(gp1(ROBUSTNESS), "r_gp1", trajectories, locSvc, model)

    // P2
    smc(phi2(SATISFACTION), "s_p2", trajectories, locSvc, model)
    smc(phi2(ROBUSTNESS), "r_p2", trajectories, locSvc, model)

    // P3
    smc(phi3(SATISFACTION), "s_p3", trajectories, locSvc, model)
    smc(phi3(ROBUSTNESS), "r_p3", trajectories, locSvc, model)

    // P4
    smc(phi4(SATISFACTION), "s_p4", trajectories, locSvc, model)
    smc(phi4(ROBUSTNESS), "r_p4", trajectories, locSvc, model)
}

private fun <D> smc(
    p: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>,
    id: String, trajectories: Collection<MultiValuedTrace>,
    locSvc: LocationService<Double, Double>,
    model: String
    )
{
    //Statistical Model Checking
    val smc = StatisticalModelChecker(p, trajectories, locSvc)
    smc.compute()
    val stats = smc.stats
    val avg = filterAverage(stats)
    val `var` = filterVariance(stats)
    val str = RawTrajectoryExtractor(network.size())
    DataWriter(outputFile(id, "avg", model), FileType.CSV, str).write(avg)
    DataWriter(outputFile(id, "var", model), FileType.CSV, str).write(`var`)
    logger.info("SMC results computed.")
}


private fun outputFile(ext1: String, ext2: String, model: String): String {
    val trace = "output/$model/${ext1}_${ext2}_K${K.toInt()}$RESULT"
    logger.info { "Saving output in: $trace" }
    return trace
}



