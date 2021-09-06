package at.ac.tuwien.cps

import at.ac.tuwien.cps.spec.*
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils
import java.util.function.ToDoubleFunction
import kotlinx.coroutines.*
import java.io.File

private const val RESULT = "_grid_21x21_T_142.csv"
private val booleans: (Boolean) -> Double = { x -> if (x) 1.0 else 0.0 }
private val doubles: (Double) -> Double = { it }


fun main(args: Array<String>) {
    logger.info{"Running SingleTrace class"}
    //selectModel(args)
    logger.info { "The Network size is: " + network.size() }

    val locService = TestUtils.createLocServiceStatic(
        0.0, 1.0,
        multiTrace.timePoints.toDouble(),
        network
    )

//    val trajectories = loadTrajectories(network.size(), TRACES, MODELS[0])
//    val booleans = ToDoubleFunction { x: Boolean -> if (x) 1.0 else 0.0 }
//    val doubles = ToDoubleFunction { obj: Double -> obj }
//    run("p1/s", booleans, trajectories, locService, phi1(SATISFACTION))
//    run("p1/r", doubles, trajectories, locService, phi1(ROBUSTNESS))
//    run("p2/s", booleans, trajectories, locService, phi2(SATISFACTION))
//    run("p2/r", doubles, trajectories, locService, phi2(ROBUSTNESS))
//    run("p3/s", booleans, trajectories, locService, phi3(SATISFACTION))
//    run("p3/r", doubles, trajectories, locService, phi3(ROBUSTNESS))
//    run("p4/s", booleans, trajectories, locService, phi4(SATISFACTION))
//    run("p4/r", doubles, trajectories, locService, phi4(ROBUSTNESS))

    for(model in MODELS) {
        logger.info{"Running on model $model"}
        runAll(locService, model)
    }

}

fun runAll(locService: LocationService<Double, Double>, model: String) {
    val trajectories = loadTrajectories(network.size(), TRACES, model)

    run("p1/s", booleans, trajectories, locService, phi1(SATISFACTION), model)
    run("p1/r", doubles, trajectories, locService, phi1(ROBUSTNESS), model)
    run("p2/s", booleans, trajectories, locService, phi2(SATISFACTION), model)
    run("p2/r", doubles, trajectories, locService, phi2(ROBUSTNESS), model)
    run("p3/s", booleans, trajectories, locService, phi3(SATISFACTION), model)
    run("p3/r", doubles, trajectories, locService, phi3(ROBUSTNESS), model)
    run("p4/s", booleans, trajectories, locService, phi4(SATISFACTION), model)
    run("p4/r", doubles, trajectories, locService, phi4(ROBUSTNESS), model)
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

private fun prepareDestination(path: String) {
    val folders  = path.split("/").dropLast(1)
    var base = ""
    for(f in folders) {
        val file = File(base, f)

        if(!file.exists())
            File(base, f).mkdir()

        base += "$f/"
    }
}


private fun outputFile(ext1: String, ext2: String, model: String): String {
    val trace = "output/$model/${ext1}_${ext2}_K${K.toInt()}$RESULT"
    logger.info { "Saving output in :$trace" }
    return trace
}
