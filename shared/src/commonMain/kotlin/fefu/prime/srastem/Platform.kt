package fefu.prime.srastem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentDateParts(): List<Int>
