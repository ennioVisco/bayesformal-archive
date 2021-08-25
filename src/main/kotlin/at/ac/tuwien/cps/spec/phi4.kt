package at.ac.tuwien.cps.spec

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*

/**
 * PoiReach -> P4 paper, hospital reachability
 */
fun <D> phi4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return reachMonitor(
        fstStep(d), Grid.distance(0.0, 4.0),
        isHospital(d), d
    )
}

fun <D> real_phi4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return reachMonitor(
        isNotCrowded(d), Grid.distance(0.0, 4.0),
        isHospital(d), d
    )
}
