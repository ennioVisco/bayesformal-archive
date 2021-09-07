package at.ac.tuwien.cps

import at.ac.tuwien.cps.spec.*
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.util.MultiValuedTrace
import java.util.function.ToDoubleFunction
import kotlinx.coroutines.*

private const val RESULT = "_grid_21x21_T_142.csv"
private val booleans: (Boolean) -> Double = { x -> if (x) 1.0 else 0.0 }
private val doubles: (Double) -> Double = { it }


fun main(args: Array<String>) {
    logger.info{"Running SingleTrace class"}
    val models = selectModels(args)
    runModels(models, ::runAll)
}

fun runAll(locSvc: LocationService<Double, Double>, model: String) {
    val trajectories = loadTraces(network.size(), TRACES, model)

    // P1
    run("p1/s", booleans, trajectories, locSvc, phi1(SATISFACTION), model)
    run("p1/r", doubles, trajectories, locSvc, phi1(ROBUSTNESS), model)
    run("gp1/s", booleans, trajectories, locSvc, gp1(SATISFACTION), model)
    run("gp1/r", doubles, trajectories, locSvc, gp1(ROBUSTNESS), model)

    // P2
    run("p2/s", booleans, trajectories, locSvc, phi2(SATISFACTION), model)
    run("p2/r", doubles, trajectories, locSvc, phi2(ROBUSTNESS), model)

    // P3
    run("p3/s", booleans, trajectories, locSvc, phi3(SATISFACTION), model)
    run("p3/r", doubles, trajectories, locSvc, phi3(ROBUSTNESS), model)

    // P4
    run("p4/s", booleans, trajectories, locSvc, phi4(SATISFACTION), model)
    run("p4/r", doubles, trajectories, locSvc, phi4(ROBUSTNESS), model)
}

private fun <D> run(
    id: String,
    f: ToDoubleFunction<D>,
    trajectories: MutableList<MultiValuedTrace>,
    locService: LocationService<Double, Double>,
    m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>, model: String
) {
    runBlocking {
        doMonitor(id, trajectories, f, locService, m, model)
    }
}

suspend fun <D> doMonitor(
    id: String,
    trajectories: MutableList<MultiValuedTrace>,
    f: ToDoubleFunction<D>, locSvc: LocationService<Double, Double>,
    m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>, model: String
) {
    withContext(Dispatchers.Default) {
        trajectories.forEachIndexed { i: Int, t: MultiValuedTrace ->
            logger.info("Starting monitor for ${i + 1}!")
            launch { // launch a new coroutine and continue
                execTrace(id, f, locSvc, m, i, t, model)
            }
            logger.info("From ${i + 1}, going on...")
        }
    }
}

private fun <D> execTrace(
    id: String,
    f: ToDoubleFunction<D>,
    locSvc: LocationService<Double, Double>,
    m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>,
    i: Int, t: MultiValuedTrace, model: String
) {
    val s = m.monitor(locSvc, t)
    val r = toArray(s, f)
    val strategy = RawTrajectoryExtractor(network.size())
    val dest = outputFile(id, (i + 1).padString(), model)
    prepareDestination(dest)
    DataWriter(dest, FileType.CSV, strategy).write(r)
    logger.info("The Monitoring of a trajectory has been completed!")
}


private fun outputFile(ext1: String, ext2: String, model: String): String {
    val trace = "output/$model/${ext1}_${ext2}_K${K.toInt()}$RESULT"
    logger.info { "Saving output in :$trace" }
    return trace
}
