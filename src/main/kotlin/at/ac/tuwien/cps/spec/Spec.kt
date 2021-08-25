package at.ac.tuwien.cps.spec

import at.ac.tuwien.cps.ErlangSignal.Companion.CROWDEDNESS_1_STEP
import at.ac.tuwien.cps.ErlangSignal.Companion.CROWDEDNESS_2_STEP
import at.ac.tuwien.cps.ErlangSignal.Companion.CROWDEDNESS_3_STEP
import at.ac.tuwien.cps.ErlangSignal.Companion.IS_HOSPITAL
import at.ac.tuwien.cps.ErlangSignal.Companion.LOC_CROWDEDNESS
import at.ac.tuwien.cps.grid.Grid
import eu.quanticol.moonlight.domain.BooleanDomain
import eu.quanticol.moonlight.domain.DoubleDomain
import eu.quanticol.moonlight.domain.SignalDomain
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor
import eu.quanticol.moonlight.monitoring.spatialtemporal.SpatialTemporalMonitor.*
import eu.quanticol.moonlight.space.DistanceStructure
import eu.quanticol.moonlight.space.SpatialModel
import java.util.function.Function


const val INVALID_DOMAIN = "Unsupported Signal Domain!"

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
 * For debugging
 */
fun phi0(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return crowdedness()
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

fun crowdedness(): SpatialTemporalMonitor<Double, List<Comparable<*>>, Double>
{
    return atomicMonitor{ s: List<Comparable<*>> ->
        (s[LOC_CROWDEDNESS] as Float).toDouble()
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
