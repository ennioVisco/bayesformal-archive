import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.signal.SpatialTemporalSignal
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils
import java.util.function.ToDoubleFunction
import java.util.stream.IntStream
import kotlinx.coroutines.*

private const val RESULT = "_grid_21x21_T_144.csv"


fun main() {
    logger.info { "The network size is: " + network.size() }
    val locService = TestUtils.createLocServiceStatic(
        0.0, 1.0,
        multiTrace.timePoints.toDouble(),
        network
    )
    val trajectories = loadTrajectories(network.size(), TRACES)
    val booleans = ToDoubleFunction { x: Boolean -> if (x) 1.0 else 0.0 }
    val doubles = ToDoubleFunction { obj: Double -> obj }

    run("p1/s", booleans, trajectories, locService, phi1(SATISFACTION))
    run("p1/r", doubles, trajectories, locService, phi1(ROBUSTNESS))
    run("p2/s", booleans, trajectories, locService, phi2(SATISFACTION))
    run("p2/r", doubles, trajectories, locService, phi2(ROBUSTNESS))
    run("p3/s", booleans, trajectories, locService, phi3(SATISFACTION))
    run("p3/r", doubles, trajectories, locService, phi3(ROBUSTNESS))
    run("p4/s", booleans, trajectories, locService, phi4(SATISFACTION))
    run("p4/r", doubles, trajectories, locService, phi4(ROBUSTNESS))
}

private fun <D> run(
    id: String,
    f: ToDoubleFunction<D>,
    trajectories: MutableList<MultiValuedTrace>,
    locService: LocationService<Double, Double>,
    m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
) {

    runBlocking {
        doMonitoring(id, trajectories, f, locService, m)
    }

/*    var i = 1
    for (t in trajectories) {
        Thread {
            val s = m.monitor(locService, t)
            val r = toArray(s, f)
            val strategy = RawTrajectoryExtractor(network.size())
            val dest = outputFile(id, i.padString(TRACES))
            DataWriter(dest, FileType.CSV, strategy).write(r)
            logger.info("The Monitoring of a trajectory has been completed!")
        }.start()
        i++
    }*/
}

suspend fun <D> doMonitoring(id: String,
             trajectories: MutableList<MultiValuedTrace>,
             f: ToDoubleFunction<D>, locSvc: LocationService<Double, Double>,
             m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>) =
    withContext(Dispatchers.Default) {
        trajectories.forEachIndexed { i: Int, t: MultiValuedTrace ->
            logger.info("Starting monitor for ${i + 1}!")
            launch { // launch a new coroutine and continue
                val s = m.monitor(locSvc, t)
                val r = toArray(s, f)
                val strategy = RawTrajectoryExtractor(network.size())
                val dest = outputFile(id, (i + 1).padString())
                DataWriter(dest, FileType.CSV, strategy).write(r)
                logger.info("The Monitoring of a trajectory has been completed!")
            }
            logger.info("From ${i + 1}, going on...")
        }

}

fun <D> toArray(signal: SpatialTemporalSignal<D>, f: ToDoubleFunction<D>): Array<DoubleArray>
{
    val times = signal.signals[0].end().toInt()
    val toReturn = Array(signal.size()) {
        DoubleArray(
            times
        )
    }
    IntStream.range(0, signal.size())
        .forEach { i: Int ->
            val s = signal.signals[i]
            IntStream.range(0, times)
                .forEach { j: Int -> toReturn[i][j] =
                                    f.applyAsDouble(s.getValueAt(j.toDouble()))}
        }
    return toReturn
}

private fun outputFile(ext1: String, ext2: String): String {
    val trace = "output/$DATA_DIR${ext1}_${ext2}_K${K.toInt()}$RESULT"
    logger.info { "Saving output in :$trace" }
    return trace
}
