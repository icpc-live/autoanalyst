package config

import com.sksamuel.hoplite.ConfigAlias
import com.sksamuel.hoplite.Masked

sealed interface DatabaseConfig {
    data class TestDBConfig(
        val useFakeDb: Boolean,
        val createTables: Boolean = true,
    ): DatabaseConfig
    data class MySQLConfig(
        val host: String,
        @ConfigAlias("name") val databaseName: String,
        val user: String? = null,
        val password: Masked? = null
    ): DatabaseConfig
}