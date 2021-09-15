package at.ac.tuwien.cps

import eu.quanticol.moonlight.io.parsing.SignalProcessor
import eu.quanticol.moonlight.util.MultiValuedTrace

class ErlangSignal(private val dimensions: Int) : SignalProcessor<Float> {
    private var size = 0
    private var length = 0
    private var currDim = 0
    private var trace: MultiValuedTrace? = null
    private val data: Array<Array<Array<Float>>?> = arrayOfNulls(4)

    override fun initializeSpaceTime(space: Int, time: Int) {
        size = space
        length = time
        trace = MultiValuedTrace(space, time)
    }

    /**
     * Factory method that generates signals.
     *
     * @param input the data used to generate the signal
     * @return a signal generated from the input data
     */
    override fun generateSignal(input: Array<Array<Float>>): MultiValuedTrace {
        if (currDim < dimensions) {
            data[currDim] = input
            currDim++
        } else throw UnsupportedOperationException("Exceeding max inputs")
        return trace!!
    }

    /**
     * Gathers the values to generate a multi-valued signal of the dimensions
     * of interest from the input.
     *
     * @return a list of signals representing the 5-tuple:
     * (devConnected, devDirection, locRouter, locCrowdedness, outRouter)
     */
    fun generateSignal(): MultiValuedTrace? {
        val isHospital = Array(size) {
            arrayOfNulls<Boolean>(
                length
            )
        }
        for (l in 0 until size) {
            for (t in 0 until length) {
                isHospital[l][t] = isHospital(l)
            }
        }
        for (i in data.indices) {
            trace!!.setDimension(data[i], i)
        }
        trace!!.setDimension(isHospital, IS_HOSPITAL) // POI ID boolean

        /*
        trace.setDimension(crowd, LOC_CROWDEDNESS)    // Location Crowdedness
             .setDimension(pred1, CROWDEDNESS_1_STEP) // 1-Step Predictor
             .setDimension(pred2, CROWDEDNESS_2_STEP) // 2-Step Predictor
             .setDimension(pred3, CROWDEDNESS_3_STEP) // 3-Step Predictor
             .setDimension(at.ac.tuwien.cps.isHospital, IS_HOSPITAL)   // POI ID boolean
             .initialize();
        */trace!!.initialize()
        return trace
    }

    // ------------- SIGNAL EXTRACTORS ------------- ////
    private fun isHospital(l: Int): Boolean {
        // Starting from (0,0)
        // Hospitals: (4,10) | (12,8) | (10,17)
        return l == 4 + 10 * 21  || l == 12 + 8 * 21 ||
               l == 10 + 17 * 21 || l == 19 + 11 * 21
    }

    companion object {
        /**
         * State Signals
         */
        const val LOC_CROWDEDNESS = 0

        /**
         * Predictors Signals
         */
        const val CROWDEDNESS_1_STEP = 1
        const val CROWDEDNESS_2_STEP = 2
        const val CROWDEDNESS_3_STEP = 3

        /**
         * POI Signals
         */
        const val IS_HOSPITAL = 4
    }

}

