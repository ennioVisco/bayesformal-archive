package at.ac.tuwien.cps.spec

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*

/**
 * P2 paper
 */
fun <D> phi2(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        notMonitor(fstStep(d), d),  /// Crowd > K
        d,
        somewhereMonitor(
            fstStep(d),
            Grid.distance(0.0, 1.0), d
        )
    )
}

fun <D> realPhi2(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        notMonitor(isNotCrowded(d), d),  /// Crowd > K
        d,
        somewhereMonitor(
            isNotCrowded(d),
            Grid.distance(0.0, 1.0), d
        )
    )
}