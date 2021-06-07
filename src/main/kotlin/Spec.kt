import ErlangSignal.Companion.CROWDEDNESS_1_STEP
import ErlangSignal.Companion.CROWDEDNESS_2_STEP
import ErlangSignal.Companion.CROWDEDNESS_3_STEP
import ErlangSignal.Companion.IS_HOSPITAL
import ErlangSignal.Companion.LOC_CROWDEDNESS
import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.BooleanDomain
import eu.quanticol.moonlight.domain.DoubleDomain
import eu.quanticol.moonlight.domain.Interval
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*
import eu.quanticol.moonlight.space.DistanceStructure
import eu.quanticol.moonlight.space.SpatialModel
import java.util.function.Function

/**
 * Numeric constants of the problem
 */
const val K = 500.0 // crowdedness threshold

const val TH = 3.0 // properties time horizon

const val d1: Double = 0.0
const val d2: Double = 1.0
const val d3: Double = 4.0

fun distances():
HashMap<String, Function<SpatialModel<Double>, DistanceStructure<Double, *>>>
{
    val map: HashMap<String, Function<SpatialModel<Double>,
                                      DistanceStructure<Double, *>>>
            = HashMap()

    map["dist_from_d1_to_d2"] = Grid.distance(d1, d2)
    map["dist_to_d1"] = Grid.distance(0.0, d1)
    map["dist_to_d3"] = Grid.distance(0.0, d3)

    return map
}

/*fun <T> atoms():
HashMap<String, Function<Parameters, Function<List<Comparable<T>>, Boolean>>>
{
    val atoms: java.util.HashMap<String, Function<Parameters, Function<List<Comparable<T>>, Boolean>>> = HashMap()
    atoms["isH"] = Function { Function { s: List<Comparable<T>> ->
        when(s[IS_HOSPITAL]) {
            is Boolean ->  s[IS_HOSPITAL] == true
            else -> throw IllegalArgumentException("Wrong comparison")
        }}}

    return atoms
    TODO("WIP")
}*/

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

/**
 * real-data P1 paper
 */
fun <D> real_phi1(d: SignalDomain<D>):
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


/**
 * P1 paper, crowdedness goes down at some point
 */
fun <D> phi1(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
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
        //        isNotCrowded(d),
        //        new Interval(0, TH), d
        //    )
    )
}

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

fun <D> real_phi2(d: SignalDomain<D>):
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

/**
 * P3 paper
 */
fun <D> phi3(d: SignalDomain<D>):
        SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return impliesMonitor(
        andMonitor(isNotCrowded(d), d,
            andMonitor(fstStep(d), d, andMonitor(sndStep(d), d, trdStep(d)))
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

fun <D> real_phi3(d: SignalDomain<D>):
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

/**
 * For debugging
 */
fun <D> phi0(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return notMonitor(isNotCrowded(d), d)
}

// --------- ATOMIC PREDICATES --------- //
fun <D> isNotCrowded(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return when (d) {
        is DoubleDomain -> {
            lcDouble()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        is BooleanDomain -> {
            lcBoolean()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        else -> throw UnsupportedOperationException(INVALID_DOMAIN)
    }
}

fun <D> fstStep(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return when (d) {
        is DoubleDomain -> {
            fStepCDouble()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        is BooleanDomain -> {
            fStepCBoolean()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        else -> throw UnsupportedOperationException(INVALID_DOMAIN)
    }
}

fun <D> sndStep(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return when (d) {
        is DoubleDomain -> {
            sStepCDouble()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        is BooleanDomain -> {
            sStepCBoolean()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        else -> throw UnsupportedOperationException(INVALID_DOMAIN)
    }
}

fun <D> trdStep(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
{
    return when (d) {
        is DoubleDomain -> {
            tStepCDouble()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        is BooleanDomain -> {
            tStepCBoolean()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        else -> throw UnsupportedOperationException(INVALID_DOMAIN)
    }
}

fun <D> isHospital(d: SignalDomain<D>): SpatialTemporalMonitor<Double, List<Comparable<*>>, D> {
    return when (d) {
        is DoubleDomain -> {
            isHDouble()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        is BooleanDomain -> {
            isHBoolean()
                    as SpatialTemporalMonitor<Double, List<Comparable<*>>, D>
        }
        else -> throw UnsupportedOperationException(INVALID_DOMAIN)
    }
}

// --------- TYPED ATOMS --------- //
private fun lcBoolean(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Boolean>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
        (s[LOC_CROWDEDNESS] as Float) < K
    }
}

private fun lcDouble(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
            K - (s[LOC_CROWDEDNESS] as Float)
    }
}

private fun fStepCBoolean(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Boolean>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
        (s[CROWDEDNESS_1_STEP] as Float) < K
    }
}

private fun fStepCDouble(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor { s: List<Comparable<*>> ->
            K - s[CROWDEDNESS_1_STEP] as Float
    }
}

private fun sStepCBoolean(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Boolean>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
        (s[CROWDEDNESS_2_STEP] as Float) < K
    }
}

private fun sStepCDouble(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor { s: List<Comparable<*>> ->
        K - s[CROWDEDNESS_2_STEP] as Float
    }
}

private fun tStepCBoolean(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Boolean>
{
    return atomicMonitor { s: List<Comparable<*>> ->
        (s[CROWDEDNESS_3_STEP] as Float) < K
    }
}

private fun tStepCDouble(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
        K - s[CROWDEDNESS_3_STEP] as Float
    }
}

private fun isHBoolean(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Boolean>
{
    return atomicMonitor {
            s: List<Comparable<*>> -> s[IS_HOSPITAL] == java.lang.Boolean.TRUE
    }
}

private fun isHDouble(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor { s: List<Comparable<*>> ->
        if (s[IS_HOSPITAL] == java.lang.Boolean.TRUE)
            Double.POSITIVE_INFINITY
        else
            Double.NEGATIVE_INFINITY
    }
}
