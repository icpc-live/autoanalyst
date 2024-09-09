package config

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked

sealed interface CdsConfig {
    val contestId: String

    data class ClicsServer (
        val baseurl: String,
        override val contestId: String,
        @ConfigAlias("user") val username: String,
        @ConfigAlias("pass") val password: Masked,
        @ConfigAlias("userfull") val privilegedUsername: String,
        @ConfigAlias("passfull") val privilegedPassword: Masked,
    ) : CdsConfig
    data class LocalPath(
        val path: String,
        override val contestId: String,
    ) : CdsConfig
}
