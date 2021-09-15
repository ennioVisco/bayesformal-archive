package at.ac.tuwien.cps.spec

import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.Interval
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*


/**
 * P3 paper
 */
fun <D> phi3(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return andMonitor(
            somewhereMonitor(
                isNotCrowded(d),
                Grid.distance(0.0, 1.0), d
            ), d,
            andMonitor(
                somewhereMonitor(
                    fstStep(d),
                    Grid.distance(0.0, 1.0),
                    d
                ), d,
                andMonitor(
                    somewhereMonitor(
                        sndStep(d),
                        Grid.distance(0.0, 1.0),
                        d
                    ), d, somewhereMonitor(
                        trdStep(d),
                        Grid.distance(0.0, 1.0),
                        d
                    )
                )
            )
        )
}

fun <D> oldPhi3(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        andMonitor(
            isNotCrowded(d), d,
            andMonitor(
                fstStep(d),
                d,
                andMonitor(sndStep(d), d, trdStep(d))
            )
        ), d,
        andMonitor(
            somewhereMonitor(
                isNotCrowded(d),
                Grid.distance(0.0, 1.0), d
            ), d,
            andMonitor(
                somewhereMonitor(
                    fstStep(d),
                    Grid.distance(0.0, 1.0),
                    d
                ), d,
                andMonitor(
                    somewhereMonitor(
                        sndStep(d),
                        Grid.distance(0.0, 1.0),
                        d
                    ), d, somewhereMonitor(
                        fstStep(d),
                        Grid.distance(0.0, 1.0),
                        d
                    )
                )
            )
        )
    )
}

fun <D> realPhi3(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        globallyMonitor(
            notMonitor(isNotCrowded(d), d),
            Interval(0, 3), d
        ), d,
        somewhereMonitor(
            globallyMonitor(
                isNotCrowded(d),
                Interval(0, 3), d
            ),
            Grid.distance(0.0, 1.0), d
        )
    )
}