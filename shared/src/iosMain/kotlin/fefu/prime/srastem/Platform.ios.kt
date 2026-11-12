package fefu.prime.srastem

import platform.UIKit.UIDevice
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun currentDateParts(): List<Int> {
    val components = NSCalendar.currentCalendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = NSDate(),
    )
    return listOf(components.year.toInt(), components.month.toInt(), components.day.toInt())
}

actual fun backendBaseUrl(): String = "http://127.0.0.1:8080"
