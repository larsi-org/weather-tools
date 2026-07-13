package org.larsi.util

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow

/**
 * PsychrometricsUtil contains methods, for the relationship of dew point temperature, dry bulb
 * temperature, relative humidity<br></br>
 * copied from open-source project SimCon, originally in package simcon.util
 */
object PsychrometricsUtil
{
	// constants (in IP, with temp in F):
	const val C1 = -1.0214165e4f
	const val C2 = -4.8932428f
	const val C3 = -5.3765794e-3f
	const val C4 = 1.9202377e-7f
	const val C5 = 3.5575832e-10f
	const val C6 = -9.0344688e-14f
	const val C7 = 4.1635019f
	const val C8 = -1.0440397e4f
	const val C9 = -1.1294650e1f
	const val C10 = -2.7022355e-2f
	const val C11 = 1.2890360e-5f
	const val C12 = -2.4780681e-9f
	const val C13 = 6.5459673f
	const val C14 = 100.45f
	const val C15 = 33.193f
	const val C16 = 2.319f
	const val C17 = 0.17074f
	const val C18 = 1.2063f
	const val C19 = 90.12f
	const val C20 = 26.142f
	const val C21 = 0.8927f

	/**
	 * calculates dew point temperature given dry bulb temperature and relative humidity<br></br>
	 * uses equations in ASHRAE Handbook of Fundamentals (IP edition), ch 6, eqs 5, 24, 39, 40
	 * @param Tamb dry bulb temperature in C
	 * @param RH relative humidity in percent
	 */
	@JvmStatic
	fun calcTdpGivenTambRH(Tamb: Float, RH: Float): Float
	{
		// conversions
		val rh = RH / 100.0f // RH from percent to ratio
		val TdbF = celsiusToFahrenheit(Tamb) // C to F
		val TdbR = fahrenheitToRankine(TdbF) // F to Rankin

		// equations to calculate TdpF, HOF 2005, ch 6, eqs 5, 24, 39, 40
		val pws: Float
		if (TdbF < 32f) {
			pws = exp(C1/TdbR + C2 + C3*TdbR + C4*TdbR.pow(2) + C5*TdbR.pow(3) + C6*TdbR.pow(4) + C7*ln(TdbR))
		} else {
			pws = exp(C8/TdbR + C9 + C10*TdbR + C11*TdbR.pow(2) + C12*TdbR.pow(3) + C13*ln(TdbR))
		}
		val pw = rh * pws
		val alpha = ln(pw)
		var TdpF = C14 + C15*alpha + C16*alpha.pow(2) + C17*alpha.pow(3) + C18*pw.pow(0.1984f)
		if (TdpF < 32) {
			TdpF = C19 + C20*alpha + C21*alpha.pow(2)
		}

		// conversion back to C
		var Tdp = fahrenheitToCelsius(TdpF)

		// safety check since numerical noise can sometimes produce impossible conditions
		if (Tdp > Tamb) { Tdp = Tamb }

		return Tdp
	}

	@JvmStatic
	fun getRH(tempC: Float, dewC: Float): Int
	{
		return (0.5 + 100.0 * ((112.0 - 0.1 * tempC + dewC) / (112.0 + 0.9 * tempC)).pow(8)).toInt()
	}

	/** °F to °R */
	@JvmStatic
	fun fahrenheitToRankine(fahrenheit: Float): Float {
		return fahrenheit + 459.67f
	}

	/** °R to °F */
	@JvmStatic
	fun rankineToFahrenheit(rankine: Float): Float {
		return rankine - 459.67f
	}

	/** K to °R */
	@JvmStatic
	fun kelvinToRankine(kelvin: Float): Float {
		return kelvin * 1.8f
	}

	/** °R to K */
	@JvmStatic
	fun rankineToKelvin(rankine: Float): Float {
		return rankine / 1.8f
	}

	/** °C to °F */
	@JvmStatic
	fun celsiusToFahrenheit(celsius: Float): Float {
		return celsius * 1.8f + 32.0f
	}

	/** °F to °C */
	@JvmStatic
	fun fahrenheitToCelsius(fahrenheit: Float): Float {
		return (fahrenheit - 32.0f) / 1.8f
	}

}
