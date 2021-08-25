package at.ac.tuwien.cps

import at.ac.tuwien.cps.spec.K
import at.ac.tuwien.cps.spec.phi3
import eu.quanticol.moonlight.io.DataWriter
import eu.quanticol.moonlight.io.FileType
import eu.quanticol.moonlight.io.parsing.RawTrajectoryExtractor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.space.LocationService
import eu.quanticol.moonlight.util.MultiValuedTrace
import eu.quanticol.moonlight.util.TestUtils
import java.util.function.ToDoubleFunction
import kotlinx.coroutines.*

    private val RESULT = "_grid_21x21_T_142.csv"

    fun main() {
        logger.info { "The at.ac.tuwien.cps.getNetwork size is: " + network.size() }
        val locService = TestUtils.createLocServiceStatic(
            0.0, 1.0,
            multiTrace.timePoints.toDouble(),
            network
        )
        val trajectories = loadTrajectories(network.size(), TRACES)
        val booleans = ToDoubleFunction { x: Boolean -> if (x) 1.0 else 0.0 }
        val doubles = ToDoubleFunction { obj: Double -> obj }

        //run("real", doubles, trajectories, locService, at.ac.tuwien.cps.phi0())
//    run("p1/s", booleans, trajectories, locService, at.ac.tuwien.cps.phi1(at.ac.tuwien.cps.getSATISFACTION))
//    run("p1/r", doubles, trajectories, locService, at.ac.tuwien.cps.phi1(at.ac.tuwien.cps.getROBUSTNESS))
//    run("p2/s", booleans, trajectories, locService, at.ac.tuwien.cps.phi2(at.ac.tuwien.cps.getSATISFACTION))
//    run("p2/r", doubles, trajectories, locService, at.ac.tuwien.cps.phi2(at.ac.tuwien.cps.getROBUSTNESS))
//    run("p3/s", booleans, trajectories, locService, at.ac.tuwien.cps.phi3(at.ac.tuwien.cps.getSATISFACTION))
        run("p3/r", doubles, trajectories, locService, phi3(ROBUSTNESS))
//    run("p4/s", booleans, trajectories, locService, at.ac.tuwien.cps.phi4(at.ac.tuwien.cps.getSATISFACTION))
//    run("p4/r", doubles, trajectories, locService, at.ac.tuwien.cps.phi4(at.ac.tuwien.cps.getROBUSTNESS))
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
    }

    suspend fun <D> doMonitoring(
        id: String,
        trajectories: MutableList<MultiValuedTrace>,
        f: ToDoubleFunction<D>, locSvc: LocationService<Double, Double>,
        m: SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
    ) {
        withContext(Dispatchers.Default) {
            trajectories.forEachIndexed { i: Int, t: MultiValuedTrace ->
                logger.info("Starting monitor for ${i + 1}!")
                launch { // launch a new coroutine and continue
                    execTrace(id, f, locSvc, m, i, t)
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
        i: Int, t: MultiValuedTrace
    ) {
        val s = m.monitor(locSvc, t)
        val r = toArray(s, f)
        val strategy = RawTrajectoryExtractor(network.size())
        val dest = outputFile(id, (i + 1).padString())
        DataWriter(dest, FileType.CSV, strategy).write(r)
        logger.info("The Monitoring of a trajectory has been completed!")
    }


    private fun outputFile(ext1: String, ext2: String): String {
        val trace = "output/$DATA_DIR${ext1}_${ext2}_K${K.toInt()}$RESULT"
        logger.info { "Saving output in :$trace" }
        return trace
    }
