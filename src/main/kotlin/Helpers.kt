import eu.quanticol.moonlight.domain.BooleanDomain
import eu.quanticol.moonlight.domain.DoubleDomain
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.io.DataReader
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor
import eu.quanticol.moonlight.statistics.SignalStatistics.Statistics
import eu.quanticol.moonlight.util.MultiValuedTrace
import mu.KotlinLogging
import java.io.InputStream
import java.lang.IllegalArgumentException

/**
 * Source files location
 */
const val DATA_DIR = "CARar_3_steps_ahead/"
const val REAL_DATA = "data_matrix_20131111.csv"
const val NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt"
const val TRACES = 100

/**
 * Internals
 */
private val logger = KotlinLogging.logger {}
const val INVALID_DOMAIN = "Unsupported Signal Domain!"

private const val TRACE_FILE_PART = "_trajectories_grid_21x21_T_144_h_"
private const val TRACE_FILE_EXT = ".csv"

/**
 * Semantics domains
 */
val ROBUSTNESS: SignalDomain<Double> = DoubleDomain()
val SATISFACTION: SignalDomain<Boolean> = BooleanDomain()


fun loadTrajectories(spaceSize: Int, last: Int): Collection<MultiValuedTrace> {
    val trajectories: MutableCollection<MultiValuedTrace> = ArrayList()
    for (i in 1..last) {
        val t = i.toString().padStart(3, '0')
        trajectories.add(loadTrajectory(t, spaceSize))
        logger.info("Trajectory $i loaded successfully!")
    }
    return trajectories
}

fun loadTrajectory(i: String, networkSize: Int): MultiValuedTrace {
    val processor = ErlangSignal(4)
    val extractor = MultiRawTrajectoryExtractor(networkSize, processor)
    DataReader(path(REAL_DATA), FileType.CSV, extractor).read()
    for (p in 1..3) {
        val path = path("$DATA_DIR$i$TRACE_FILE_PART$p$TRACE_FILE_EXT")
        DataReader(path, FileType.CSV, extractor).read()
    }
    return processor.generateSignal()
            ?: throw IllegalArgumentException("Empty signal")
}

fun filterAverage(stats: Array<Array<Statistics>>): Array<DoubleArray> {
    return stats.map{it.map{v -> v.average.toDouble()}.toDoubleArray()}
                .toTypedArray()
}

fun filterVariance(stats: Array<Array<Statistics>>): Array<DoubleArray> {
    return stats.map{it.map{v -> v.variance.toDouble()}.toDoubleArray()}
        .toTypedArray()
}

fun path(p: String): InputStream {
    return object {}.javaClass.classLoader
                    .getResourceAsStream(p) ?:
                        throw IllegalArgumentException("Unable to open $p")
}
