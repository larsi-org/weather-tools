package org.larsi.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PsychrometricsUtilTest
{
	@Test
	fun `celsius to fahrenheit and back round-trips`()
	{
		assertEquals(32.0f, PsychrometricsUtil.celsiusToFahrenheit(0f), 0.001f)
		assertEquals(212.0f, PsychrometricsUtil.celsiusToFahrenheit(100f), 0.001f)
		assertEquals(0.0f, PsychrometricsUtil.fahrenheitToCelsius(32f), 0.001f)
		assertEquals(100.0f, PsychrometricsUtil.fahrenheitToCelsius(212f), 0.001f)
	}

	@Test
	fun `fahrenheit to rankine and back round-trips`()
	{
		assertEquals(491.67f, PsychrometricsUtil.fahrenheitToRankine(32f), 0.001f)
		assertEquals(32.0f, PsychrometricsUtil.rankineToFahrenheit(491.67f), 0.001f)
	}

	@Test
	fun `kelvin to rankine and back round-trips`()
	{
		assertEquals(491.67f, PsychrometricsUtil.kelvinToRankine(273.15f), 0.01f)
		assertEquals(273.15f, PsychrometricsUtil.rankineToKelvin(491.67f), 0.01f)
	}

	@Test
	fun `getRH returns 100 when dew point equals dry bulb temperature`()
	{
		// saturated air: dew point == ambient temp implies 100% relative humidity
		assertEquals(100, PsychrometricsUtil.getRH(20f, 20f))
	}

	@Test
	fun `getRH matches known values from the ASHRAE-derived formula`()
	{
		assertEquals(53, PsychrometricsUtil.getRH(20f, 10f))
		assertEquals(40, PsychrometricsUtil.getRH(30f, 15f))
	}

	@Test
	fun `calcTdpGivenTambRH matches known values`()
	{
		assertEquals(9.31f, PsychrometricsUtil.calcTdpGivenTambRH(20f, 50f), 0.05f)
		assertEquals(26.17f, PsychrometricsUtil.calcTdpGivenTambRH(30f, 80f), 0.05f)
		assertEquals(-8.15f, PsychrometricsUtil.calcTdpGivenTambRH(0f, 50f), 0.05f)
	}

	@Test
	fun `calcTdpGivenTambRH never returns a dew point above the ambient temperature`()
	{
		// safety clamp in the implementation: numerical noise can otherwise push Tdp above Tamb
		val tamb = 15f
		val tdp = PsychrometricsUtil.calcTdpGivenTambRH(tamb, 100f)
		assert(tdp <= tamb) { "dew point $tdp should never exceed ambient temperature $tamb" }
	}
}
