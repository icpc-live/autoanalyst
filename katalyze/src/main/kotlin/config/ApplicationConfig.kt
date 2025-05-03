package config

import com.sksamuel.hoplite.ConfigAlias

data class ApplicationConfig(
    val database: DatabaseConfig,
    val katalyzer: KatalyzerConfig,
    @ConfigAlias("CDS") val cds: CdsConfig,
    // The fields below are for compatibility reasons
    val source: SourceType = SourceType.CDS,
    @ConfigAlias("CCS") val ccs: Map<String, String> = emptyMap(),
    val timezone: String = "Etc/UTC",
    val codeActivity: Map<String, String> = emptyMap(),
    @ConfigAlias("teambackup") val teamBackup: Map<String, String> = emptyMap(),
    val judgements: Map<String, Map<String, String>> = emptyMap()
) {
    enum class SourceType {
        CDS,
        FILE,
    }
}