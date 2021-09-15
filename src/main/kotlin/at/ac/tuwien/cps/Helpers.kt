package at.ac.tuwien.cps

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.BooleanDomain
import eu.quanticol.moonlight.domain.DoubleDomain
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.io.DataReader
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.MultiRawTrajectoryExtractor
import eu.quanticol.moonlight.signal.SpatialTemporalSignal
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.space.SpatialModel
import eu.quanticol.moonlight.statistics.SignalStatistics.Statistics
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils
import mu.KotlinLogging
import java.io.File
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
private val MODELS = listOf("ar_Normal",
                            "ar_rhoS0_Normal",
                            "ar_rhoS0_rhoT0_Normal",
                            "ar_rhoS05_Normal",
                            "ar_BNP")
const val DEFAULT_MODEL = "ar_BNP"
const val REAL_DATA = "data_matrix_20131111.csv"
const val NETWORK_FILE = "adjacent_matrix_milan_grid_21x21.txt"
const val TRACES = 1

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

fun runModels(models: List<String>,
              f: (LocationService<Double, Double>, String) -> Unit)
{
    logger.info { "The Network size is: " + network.size() }

    val locSvc = TestUtils.createLocServiceStatic(
        0.0, 1.0,
        multiTrace.timePoints.toDouble(),
        network
    )

    for(model in models) {
        logger.info{"Running on model $model"}
        Thread.sleep(5_000)
        f(locSvc, model)
    }

    logger.info{"Operations completed. Exiting."}
}

fun selectModels(names: Array<String>): List<String> {
    return when {
        names.isNotEmpty() -> {
            val models = names.toList()
            logger.info{"Received the following models: $models"}
            models
        }
        else -> MODELS
    }
}

fun prepareDestination(path: String) {
    val folders  = path.split("/").dropLast(1)
    var base = ""
    for(f in folders) {
        val file = File(base, f)

        if(!file.exists())
            File(base, f).mkdir()

        base += "$f/"
    }
}

fun loadTraces(spaceSize: Int, last: Int, model: String = DEFAULT_MODEL):
        MutableList<MultiValuedTrace>
{
    val trajectories: MutableList<MultiValuedTrace> = ArrayList()
    for (i in 1..last) {
        val t = i.padString()
        trajectories.add(loadFileAsTrace(t, spaceSize, model))
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

fun loadFileAsTrace(i: String, networkSize: Int, model: String): MultiValuedTrace
{
    val processor = ErlangSignal(4)
    val extractor = MultiRawTrajectoryExtractor(networkSize, processor)
    DataReader(path(REAL_DATA), FileType.CSV, extractor).read()
    for (p in 1..3) {
        val path = path("$model/$i$TRACE_FILE_PART$p$TRACE_FILE_EXT")
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
