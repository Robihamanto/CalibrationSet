package com.mteam.calibrationset.Model

class RawData (val pitch: Double, val roll: Double,
               val azimuth: Double, val rawX: Double,
               val rawY: Double, val touchPreassure: Double,
               val touchSize: Double) {

    override fun toString(): String {
        return "Pitch ${pitch} Roll ${roll} Azimuth ${azimuth} Raw X ${rawX} Raw Y Touch Preassure ${touchPreassure} Touch Size ${touchSize}"
    }

}