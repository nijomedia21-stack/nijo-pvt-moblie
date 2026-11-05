package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class MalayalamCalendarHelperTest {

  @Test
  fun testKollamYearCalculation() {
    // Before Chingam 1 (Aug 17): Year - 825
    val earlyDate = LocalDate.of(2026, Month.AUGUST, 16)
    assertEquals(1201, MalayalamCalendarHelper.calculateKollamYear(earlyDate))

    val janDate = LocalDate.of(2026, Month.JANUARY, 10)
    assertEquals(1201, MalayalamCalendarHelper.calculateKollamYear(janDate))

    // On or after Chingam 1 (Aug 17): Year - 824
    val chingamDate = LocalDate.of(2026, Month.AUGUST, 17)
    assertEquals(1202, MalayalamCalendarHelper.calculateKollamYear(chingamDate))

    val novDate = LocalDate.of(2026, Month.NOVEMBER, 1)
    assertEquals(1202, MalayalamCalendarHelper.calculateKollamYear(novDate))
  }

  @Test
  fun testMalayalamMonthAndDayInfo() {
    val date = LocalDate.of(2026, Month.APRIL, 14)
    val info = MalayalamCalendarHelper.getMalayalamDayInfo(date)

    assertEquals(1201, info.kollamYear)
    assertEquals("മേടം", info.malayalamMonthName)
    assertEquals("Medam", info.malayalamMonthEnglish)
    assertNotNull(info.festival)
    assertTrue(info.festival!!.contains("വിഷു"))
  }

  @Test
  fun testThiruvonamFestival() {
    val date = LocalDate.of(2026, Month.SEPTEMBER, 6)
    val info = MalayalamCalendarHelper.getMalayalamDayInfo(date)

    assertEquals(1202, info.kollamYear)
    assertEquals("ചിങ്ങം", info.malayalamMonthName)
    assertNotNull(info.festival)
    assertTrue(info.festival!!.contains("തിരുവോണം"))
  }

  @Test
  fun testNakshatraSequence() {
    val date = LocalDate.of(2026, Month.NOVEMBER, 2)
    val info = MalayalamCalendarHelper.getMalayalamDayInfo(date)

    assertTrue(info.nakshatraMalayalam.isNotEmpty())
    assertTrue(info.nakshatraEnglish.isNotEmpty())
  }

  @Test
  fun testMathrubhumiPanchangamDetails() {
    val date = LocalDate.of(2026, Month.NOVEMBER, 2) // Monday
    val info = MalayalamCalendarHelper.getMalayalamDayInfo(date)

    // Thithi
    assertTrue(info.thithiMalayalam.isNotEmpty())
    assertTrue(info.thithiEnglish.isNotEmpty())

    // Monday Rahu Kalam: 07:30 AM - 09:00 AM
    assertEquals("07:30 AM - 09:00 AM", info.rahuKalam)

    // Monday Yamakandakam: 10:30 AM - 12:00 PM
    assertEquals("10:30 AM - 12:00 PM", info.yamakandakam)

    // Monday Gulikakalam: 01:30 PM - 03:00 PM
    assertEquals("01:30 PM - 03:00 PM", info.gulikakalam)

    // Sunrise & Sunset
    assertEquals("06:18 AM", info.sunrise)
    assertEquals("06:15 PM", info.sunset)
  }
}
