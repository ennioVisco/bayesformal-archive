import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.signal.SpatialTemporalSignal
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils
import mu.KotlinLogging
import java.util.function.ToDoubleFunction
import java.util.stream.IntStream
import kotlin.math.log10

private val logger = KotlinLogging.logger {}

private const val RESULT = "_grid_21x21_T_144.csv"

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
    logger.info { "The network size is: " + network.size() }
    val locService = TestUtils.createLocServiceStatic(
        0.0, 1.0,
        multiTrace.timePoints.toDouble(),
        network
    )
    val trajectories = loadTrajectories(network.size(), 1)
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
    trajectories: Collection<MultiValuedTrace>,
    locService: LocationService<Double, Double>,
    m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
) {
    var i = 1
    for (t in trajectories) {
        val v = i
        Thread {
            val s = m.monitor(locService, t)
            val r = toArray(s, f)
            val strategy = RawTrajectoryExtractor(network.size())
            val dest = outputFile(id, v.toString()
                            .padStart(log10(TRACES.toDouble()).toInt(), '0'))
            DataWriter(dest, FileType.CSV, strategy).write(r)
            logger.info("The Monitoring of a trajectory has been completed!")
        }.start()
        i++
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
