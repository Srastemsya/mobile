package fefu.prime.srastem

import android.os.Build
import java.util.Calendar

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentDateParts(): List<Int> = Calendar.getInstance().let {
    listOf(it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH))
}

actual fun backendBaseUrl(): String = "https://srastemsya.nixbi.ru"

private fun isAndroidEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
        Build.MODEL.contains("Emulator", ignoreCase = true) ||
        Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
        Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
        Build.HARDWARE.lowercase() in setOf("goldfish", "ranchu", "vbox86")
