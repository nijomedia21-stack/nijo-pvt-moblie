package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month

data class MalayalamDayInfo(
  val gregorianDate: LocalDate,
  val kollamYear: Int,
  val malayalamMonthName: String,
  val malayalamMonthEnglish: String,
  val malayalamDayNumber: Int,
  val nakshatraMalayalam: String,
  val nakshatraEnglish: String,
  val festival: String? = null,
  val isSpecialDay: Boolean = false,
  val thithiMalayalam: String = "ശുക്ലപക്ഷ പഞ്ചമി",
  val thithiEnglish: String = "Shukla Panchami",
  val rahuKalam: String = "07:30 AM - 09:00 AM",
  val yamakandakam: String = "10:30 AM - 12:00 PM",
  val gulikakalam: String = "01:30 PM - 03:00 PM",
  val sunrise: String = "06:18 AM",
  val sunset: String = "06:14 PM"
)

object MalayalamCalendarHelper {

  // 12 Malayalam months with Malayalam script and typical Gregorian start day/month
  private val MONTH_DATA = listOf(
    MonthDefinition("ചിങ്ങം", "Chingam", Month.AUGUST, 17),
    MonthDefinition("കന്നി", "Kanni", Month.SEPTEMBER, 17),
    MonthDefinition("തുലാം", "Thulam", Month.OCTOBER, 17),
    MonthDefinition("വൃശ്ചികം", "Vrischikam", Month.NOVEMBER, 17),
    MonthDefinition("ധനു", "Dhanu", Month.DECEMBER, 16),
    MonthDefinition("മകരം", "Makaram", Month.JANUARY, 15),
    MonthDefinition("കുംഭം", "Kumbham", Month.FEBRUARY, 14),
    MonthDefinition("മീനം", "Meenam", Month.MARCH, 15),
    MonthDefinition("മേടം", "Medam", Month.APRIL, 14),
    MonthDefinition("ഇടവം", "Edavam", Month.MAY, 15),
    MonthDefinition("മിഥുനം", "Mithunam", Month.JUNE, 16),
    MonthDefinition("കർക്കടകം", "Karkidakam", Month.JULY, 17)
  )

  private val NAKSHATRAS = listOf(
    Pair("അശ്വതി", "Aswathi"),
    Pair("ഭരണി", "Bharani"),
    Pair("കാർത്തിക", "Karthika"),
    Pair("രോഹിണി", "Rohini"),
    Pair("മകയിരം", "Makayiram"),
    Pair("തിരുവാതിര", "Thiruvathira"),
    Pair("പുണർതം", "Punartham"),
    Pair("പൂയം", "Pooyam"),
    Pair("ആയില്യം", "Ayilyam"),
    Pair("മകം", "Makam"),
    Pair("പൂരം", "Pooram"),
    Pair("ഉത്രം", "Uthram"),
    Pair("അത്തം", "Atham"),
    Pair("ചിത്തിര", "Chithira"),
    Pair("ചോതി", "Chothi"),
    Pair("വിശാഖം", "Visakham"),
    Pair("അനിഴം", "Anizham"),
    Pair("തൃക്കേട്ട", "Thrikketta"),
    Pair("മൂലം", "Moolam"),
    Pair("പൂരാടം", "Pooradam"),
    Pair("ഉത്രാടം", "Uthradam"),
    Pair("തിരുവോണം", "Thiruvonam"),
    Pair("അവിട്ടം", "Avittam"),
    Pair("ചതയം", "Chathayam"),
    Pair("പൂരുരുട്ടാതി", "Pooruruttathi"),
    Pair("ഉത്രട്ടാതി", "Uthrattathi"),
    Pair("രേവതി", "Revathi")
  )

  private val THITHIS = listOf(
    Pair("ശുക്ല പ്രഥമ", "Shukla Prathama"),
    Pair("ശുക്ല ദ്വിതീയ", "Shukla Dwitiya"),
    Pair("ശുക്ല തൃതീയ", "Shukla Tritiya"),
    Pair("ശുക്ല ചതുർത്ഥി", "Shukla Chaturthi"),
    Pair("ശുക്ല പഞ്ചമി", "Shukla Panchami"),
    Pair("ശുക്ല ഷഷ്ഠി", "Shukla Shashti"),
    Pair("ശുക്ല സപ്തമി", "Shukla Saptami"),
    Pair("ശുക്ല അഷ്ടമി", "Shukla Ashtami"),
    Pair("ശുക്ല നവമി", "Shukla Navami"),
    Pair("ശുക്ല ദശമി", "Shukla Dashami"),
    Pair("ശുക്ല ഏകാദശി", "Shukla Ekadashi"),
    Pair("ശുക്ല ദ്വാദശി", "Shukla Dwadashi"),
    Pair("ശുക്ല ത്രയോദശി", "Shukla Trayodashi"),
    Pair("ശുക്ല ചതുർദ്ദശി", "Shukla Chaturdashi"),
    Pair("പൗർണ്ണമി", "Pournami (Full Moon)"),
    Pair("കൃഷ്ണ പ്രഥമ", "Krishna Prathama"),
    Pair("കൃഷ്ണ ദ്വിതീയ", "Krishna Dwitiya"),
    Pair("കൃഷ്ണ തൃതീയ", "Krishna Tritiya"),
    Pair("കൃഷ്ണ ചതുർത്ഥി", "Krishna Chaturthi"),
    Pair("കൃഷ്ണ പഞ്ചമി", "Krishna Panchami"),
    Pair("കൃഷ്ണ ഷഷ്ഠി", "Krishna Shashti"),
    Pair("കൃഷ്ണ സപ്തമി", "Krishna Saptami"),
    Pair("കൃഷ്ണ അഷ്ടമി", "Krishna Ashtami"),
    Pair("കൃഷ്ണ നവമി", "Krishna Navami"),
    Pair("കൃഷ്ണ ദശമി", "Krishna Dashami"),
    Pair("കൃഷ്ണ ഏകാദശി", "Krishna Ekadashi"),
    Pair("കൃഷ്ണ ദ്വാദശി", "Krishna Dwadashi"),
    Pair("കൃഷ്ണ ത്രയോദശി", "Krishna Trayodashi"),
    Pair("കൃഷ്ണ ചതുർദ്ദശി", "Krishna Chaturdashi"),
    Pair("അമാവാസി", "Amavasi (New Moon)")
  )

  private val FESTIVALS = mapOf(
    Pair(Month.JANUARY, 1) to "New Year (പുതുവത്സരം)",
    Pair(Month.JANUARY, 6) to "Epiphany / ദനഹാ പെരുന്നാൾ",
    Pair(Month.JANUARY, 15) to "മകരവിളക്ക് / Makara Sankranti",
    Pair(Month.JANUARY, 26) to "Republic Day",
    Pair(Month.MARCH, 8) to "Maha Shivaratri",
    Pair(Month.APRIL, 14) to "വിഷു / Vishu (മേടം 1)",
    Pair(Month.MAY, 1) to "May Day",
    Pair(Month.JULY, 3) to "St. Thomas Day (ദുക്റാന / തോമാശ്ലീഹായുടെ ഓർമ്മദിനം)",
    Pair(Month.AUGUST, 15) to "Independence Day / Assumption of Mary",
    Pair(Month.AUGUST, 17) to "Chingam 1 (ചിങ്ങം 1)",
    Pair(Month.AUGUST, 28) to "Atham (അത്തം)",
    Pair(Month.SEPTEMBER, 5) to "Uthradam (ഒന്നാം ഓണം)",
    Pair(Month.SEPTEMBER, 6) to "തിരുവോണം / Thiruvonam",
    Pair(Month.SEPTEMBER, 8) to "Nativity of Blessed Virgin Mary (എട്ടുനോമ്പ് പെരുന്നാൾ)",
    Pair(Month.OCTOBER, 2) to "Gandhi Jayanti",
    Pair(Month.OCTOBER, 23) to "Mahanavami",
    Pair(Month.OCTOBER, 24) to "Vijayadashami",
    Pair(Month.NOVEMBER, 1) to "All Saints' Day (വിശുദ്ധരുടെ ഓർമ്മദിനം) / Kerala Piravi",
    Pair(Month.NOVEMBER, 2) to "All Souls' Day (മരിച്ച വിശ്വാസികളുടെ അനുസ്മരണം)",
    Pair(Month.NOVEMBER, 12) to "Deepavali",
    Pair(Month.DECEMBER, 25) to "ക്രിസ്മസ് / Christmas"
  )

  private data class MonthDefinition(
    val malayalamName: String,
    val englishName: String,
    val startMonth: Month,
    val startDay: Int
  )

  fun calculateKollamYear(date: LocalDate): Int {
    val chingamStartDate = LocalDate.of(date.year, Month.AUGUST, 17)
    return if (!date.isBefore(chingamStartDate)) {
      date.year - 824
    } else {
      date.year - 825
    }
  }

  fun getRahuKalam(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
      DayOfWeek.SUNDAY -> "04:30 PM - 06:00 PM"
      DayOfWeek.MONDAY -> "07:30 AM - 09:00 AM"
      DayOfWeek.TUESDAY -> "03:00 PM - 04:30 PM"
      DayOfWeek.WEDNESDAY -> "12:00 PM - 01:30 PM"
      DayOfWeek.THURSDAY -> "01:30 PM - 03:00 PM"
      DayOfWeek.FRIDAY -> "10:30 AM - 12:00 PM"
      DayOfWeek.SATURDAY -> "09:00 AM - 10:30 AM"
    }
  }

  fun getYamakandakam(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
      DayOfWeek.SUNDAY -> "12:00 PM - 01:30 PM"
      DayOfWeek.MONDAY -> "10:30 AM - 12:00 PM"
      DayOfWeek.TUESDAY -> "09:00 AM - 10:30 AM"
      DayOfWeek.WEDNESDAY -> "07:30 AM - 09:00 AM"
      DayOfWeek.THURSDAY -> "06:00 AM - 07:30 AM"
      DayOfWeek.FRIDAY -> "03:00 PM - 04:30 PM"
      DayOfWeek.SATURDAY -> "01:30 PM - 03:00 PM"
    }
  }

  fun getGulikakalam(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
      DayOfWeek.SUNDAY -> "03:00 PM - 04:30 PM"
      DayOfWeek.MONDAY -> "01:30 PM - 03:00 PM"
      DayOfWeek.TUESDAY -> "12:00 PM - 01:30 PM"
      DayOfWeek.WEDNESDAY -> "10:30 AM - 12:00 PM"
      DayOfWeek.THURSDAY -> "09:00 AM - 10:30 AM"
      DayOfWeek.FRIDAY -> "07:30 AM - 09:00 AM"
      DayOfWeek.SATURDAY -> "06:00 AM - 07:30 AM"
    }
  }

  fun getMalayalamDayInfo(date: LocalDate): MalayalamDayInfo {
    val kollamYear = calculateKollamYear(date)

    // Determine current Malayalam month and day
    var matchedMonth = MONTH_DATA.first()
    var malayalamDay = 1

    for (i in MONTH_DATA.indices) {
      val def = MONTH_DATA[i]
      val monthStartThisYear = LocalDate.of(date.year, def.startMonth, def.startDay)
      val nextDef = MONTH_DATA[(i + 1) % MONTH_DATA.size]
      val nextMonthStart = if (nextDef.startMonth == Month.JANUARY && def.startMonth == Month.DECEMBER) {
        LocalDate.of(date.year + 1, nextDef.startMonth, nextDef.startDay)
      } else if (def.startMonth == Month.JANUARY && date.isBefore(monthStartThisYear)) {
        LocalDate.of(date.year, Month.JANUARY, 15)
      } else {
        val nextYear = if (nextDef.startMonth.value < def.startMonth.value) date.year + 1 else date.year
        LocalDate.of(nextYear, nextDef.startMonth, nextDef.startDay)
      }

      if (!date.isBefore(monthStartThisYear) && date.isBefore(nextMonthStart)) {
        matchedMonth = def
        malayalamDay = (date.toEpochDay() - monthStartThisYear.toEpochDay()).toInt() + 1
        break
      }
    }

    if (date.month == Month.JANUARY && date.dayOfMonth < 15) {
      val dhanuStart = LocalDate.of(date.year - 1, Month.DECEMBER, 16)
      matchedMonth = MONTH_DATA.first { it.englishName == "Dhanu" }
      malayalamDay = (date.toEpochDay() - dhanuStart.toEpochDay()).toInt() + 1
    }

    // Cyclic Nakshatra mapping using epoch day
    val nakshatraIndex = (Math.floorMod(date.toEpochDay() + 7, NAKSHATRAS.size)).toInt()
    val nakshatra = NAKSHATRAS[nakshatraIndex]

    // Cyclic Thithi mapping using epoch day
    val thithiIndex = (Math.floorMod(date.toEpochDay() + 11, THITHIS.size)).toInt()
    val thithi = THITHIS[thithiIndex]

    val festival = FESTIVALS[Pair(date.month, date.dayOfMonth)]

    val dow = date.dayOfWeek
    val rahu = getRahuKalam(dow)
    val yama = getYamakandakam(dow)
    val gulika = getGulikakalam(dow)

    return MalayalamDayInfo(
      gregorianDate = date,
      kollamYear = kollamYear,
      malayalamMonthName = matchedMonth.malayalamName,
      malayalamMonthEnglish = matchedMonth.englishName,
      malayalamDayNumber = malayalamDay,
      nakshatraMalayalam = nakshatra.first,
      nakshatraEnglish = nakshatra.second,
      festival = festival,
      isSpecialDay = festival != null,
      thithiMalayalam = thithi.first,
      thithiEnglish = thithi.second,
      rahuKalam = rahu,
      yamakandakam = yama,
      gulikakalam = gulika,
      sunrise = "06:18 AM",
      sunset = "06:15 PM"
    )
  }

  fun getDaysInMonth(year: Int, month: Month): List<MalayalamDayInfo> {
    val firstDay = LocalDate.of(year, month, 1)
    val length = month.length(firstDay.isLeapYear)
    return (1..length).map { day ->
      getMalayalamDayInfo(LocalDate.of(year, month, day))
    }
  }
}
