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
