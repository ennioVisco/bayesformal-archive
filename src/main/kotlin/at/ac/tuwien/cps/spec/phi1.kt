package at.ac.tuwien.cps.spec

import eu.quanticol.moonlight.domain.Interval
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*

/**
 * real-data P1 paper
 */
fun <D> realPhi1(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        notMonitor(isNotCrowded(d), d),
        d,
        eventuallyMonitor(
            isNotCrowded(d),
            Interval(0, TH), d
        )
    )
}

fun <D> gp1(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    return globallyMonitor(phi1(d), d)
}

/**
 * P1 paper, crowdedness goes down at some point
 */
fun <D> phi1(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    return impliesMonitor(
        notMonitor(isNotCrowded(d), d),
        d,
        orMonitor(
            isNotCrowded(d), d,
            orMonitor(
                fstStep(d), d,
                orMonitor(sndStep(d), d, trdStep(d))
            )
        ) //eventuallyMonitor(
        //        at.ac.tuwien.cps.isNotCrowded(d),
        //        new Interval(0, at.ac.tuwien.cps.TH), d
        //    )
    )
}