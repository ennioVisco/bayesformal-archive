package at.ac.tuwien.cps

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.BooleanDomain
import eu.quanticol.moonlight.domain.DoubleDomain
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.io.DataReader
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor
import eu.quanticol.moonlight.signal.SpatialTemporalSignal
import eu.quanticol.moonlight.space.SpatialModel
import eu.quanticol.moonlight.statistics.SignalStatistics.Statistics
import eu.quanticol.moonlight.util.MultiValuedTrace
import mu.KotlinLogging
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.function.ToDoubleFunction
import java.util.stream.IntStream
import kotlin.math.log10

/**
 * Internals
 */
val logger = KotlinLogging.logger {}

/**
 * Source files location
 */
//const val DATA_DIR = "ar_Normal/"
//const val DATA_DIR = "ar_rhoS0_Normal/"
//const val DATA_DIR = "ar_rhoS0_rhoT0_Normal/"
const val DATA_DIR = "ar_BNP/"
//const val DATA_DIR = "ar_rhoS05_Normal/"
//const val DATA_DIR = "CARar_3_steps_ahead/"
const val REAL_DATA = "data_matrix_20131111.csv"
const val NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt"
const val TRACES = 31

/**
 * We initialize the domains and the spatial at.ac.tuwien.cps.getNetwork
 * @see Grid for a description of the spatial model.
 */
val network: SpatialModel<Double> = Grid().getModel(path(NETWORK_FILE))

/**
 * Signal Dimensions (i.e. signal domain)
 */
val processor = ErlangSignal(4)
val multiTrace = MultiRawTrajectoryExtractor(network.size(), processor)



private const val TRACE_FILE_PART = "_trajectories_grid_21x21_T_141_h_"
private const val TRACE_FILE_EXT = ".csv"

/**
 * Semantics domains
 */
val ROBUSTNESS: SignalDomain<Double> = DoubleDomain()
val SATISFACTION: SignalDomain<Boolean> = BooleanDomain()


fun loadTrajectories(spaceSize: Int, last: Int): MutableList<MultiValuedTrace> {
    val trajectories: MutableList<MultiValuedTrace> = ArrayList()
    for (i in 1..last) {
        val t = i.padString()
        trajectories.add(loadTrajectory(t, spaceSize))
        logger.info("Trajectory $t loaded successfully!")
    }
    return trajectories
}

fun <D> toArray(signal: SpatialTemporalSignal<D>, f: ToDoubleFunction<D>):
        Array<DoubleArray>
{
    val times = signal.signals[0].end().toInt() + 1
    val toReturn = Array(signal.size()) {
        DoubleArray(times)
    }
    IntStream.range(0, signal.size()).forEach { i: Int ->
        val s = signal.signals[i]
        IntStream.range(0, times).forEach { j: Int -> toReturn[i][j] =
                f.applyAsDouble(s.getValueAt(j.toDouble()))}
    }
    return toReturn
}

fun Int.padString(n: Int = 100): String {
    return this.toString()
               .padStart(log10(n.toDouble()).toInt() + 1, '0')
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
