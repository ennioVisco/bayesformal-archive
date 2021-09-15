package at.ac.tuwien.cps.spec

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*

val D4 = Grid.distance(0.0, 4.0)!!
val D1 = Grid.distance(0.0, 1.0)!!

/**
 * PoiReach -> P4 paper, hospital reachability
 */
fun <D> phi4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    return orMonitor(isHospital(d), d,
            orMonitor(phi4Part1(d), d,
                orMonitor(phi4Part2(d), d,
                    orMonitor(phi4Part3(d), d, phi4Part4(d)))))
}

fun <D> phi4Part1(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    val m1 = somewhereMonitor(isHospital(d), D1 , d )
    return andMonitor(isNotCrowded(d), d, m1)
}

fun <D> phi4Part2(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    val m2 = andMonitor(fstStep(d), d, somewhereMonitor(isHospital(d), D1, d))
    val m1 = somewhereMonitor(m2, D1 , d)
    return andMonitor(isNotCrowded(d), d, m1)
}

fun <D> phi4Part3(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    val m3 = andMonitor(sndStep(d), d, somewhereMonitor(isHospital(d), D1, d))
    val m2 = andMonitor(fstStep(d), d, somewhereMonitor(m3, D1, d))
    val m1 = somewhereMonitor(m2, D1 , d)
    return andMonitor(isNotCrowded(d), d, m1)
}

fun <D> phi4Part4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    val m4 = andMonitor(trdStep(d), d, somewhereMonitor(isHospital(d), D1, d))
    val m3 = andMonitor(sndStep(d), d, somewhereMonitor(m4, D1, d))
    val m2 = andMonitor(fstStep(d), d, somewhereMonitor(m3, D1, d))
    val m1 = somewhereMonitor(m2, D1 , d)
    return andMonitor(isNotCrowded(d), d, m1)
}

fun <D> oldPhi4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return reachMonitor(
        fstStep(d), D4,
        isHospital(d), d
    )
}

fun <D> realPhi4(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return reachMonitor(
        isNotCrowded(d), D4,
        isHospital(d), d
    )
}
