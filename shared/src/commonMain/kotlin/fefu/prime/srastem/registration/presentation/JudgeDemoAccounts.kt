package fefu.prime.srastem.registration.presentation

/** Public dev-backend fixtures used for the hackathon judge sign-in flow. */
internal object JudgeDemoAccounts {
    val emails: List<String> = (1..5).map { "person$it@example.test" }
    const val otp = "0427"

    fun contains(email: String): Boolean = email.trim().lowercase() in emails
}
