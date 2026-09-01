package com.notzyvex.fastprint.state

enum class AuthMode { GOOGLE, GUEST }

/** The signed-in identity, populated from the real Google credential (never mocked). */
data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
) {
    val firstName: String? get() = name?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }

    /** Two-letter fallback shown when there is no profile photo to load. */
    val initials: String
        get() {
            val parts = name?.trim()?.split(Regex("\s+")).orEmpty().filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> email?.take(2)?.uppercase() ?: "?"
            }
        }
}
