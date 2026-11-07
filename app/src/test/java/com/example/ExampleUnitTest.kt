package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun weatherCodeMapper_returnsCorrectCategories() {
    val clearDay = com.example.data.model.WeatherCodeMapper.getWeatherInfo(0, isDay = true)
    assertEquals("Ciel dégagé", clearDay.titleFr)
    assertEquals(com.example.data.model.WeatherConditionCategory.CLEAR, clearDay.category)

    val rain = com.example.data.model.WeatherCodeMapper.getWeatherInfo(61, isDay = true)
    assertEquals("Pluie", rain.titleFr)
    assertEquals(com.example.data.model.WeatherConditionCategory.RAIN, rain.category)
  }

  @Test
  fun alertSettings_defaultsAreCorrect() {
    val settings = com.example.data.model.AlertSettings()
    assertTrue(settings.heavyRainEnabled)
    assertTrue(settings.thunderstormEnabled)
    assertTrue(settings.highWindEnabled)
    assertEquals(50, settings.windThresholdKmh)
    assertEquals(70, settings.rainProbThreshold)
  }
}
