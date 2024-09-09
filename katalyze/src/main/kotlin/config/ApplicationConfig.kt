package config

import com.sksamuel.hoplite.ConfigAlias

data class ApplicationConfig(
    val database: DatabaseConfig,
    val katalyzer: KatalyzerConfig,
    // The fields below are for compatibility reasons
    val source: SourceType,
    @ConfigAlias("CDS") val cds: CdsConfig,
    @ConfigAlias("CCS") val ccs: Map<String, String>,
    val timezone: String,
    val codeActivity: Map<String, String>,
    @ConfigAlias("teambackup") val teamBackup: Map<String, String>,
    val judgements: Map<String, Map<String, String>>
) {
    enum class SourceType {
        CDS,
        FILE,
    }
}