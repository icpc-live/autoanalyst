package katalyzeapp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addFileSource
import config.ApplicationConfig
import config.KatalyzerConfig
import kotlinx.coroutines.runBlocking

class KatalyzeAppV2 : CliktCommand() {

    private val configPath: String by option("-c", "--config", help = "Path to a YAML configuration file").required()

    override fun run() {
        val config = ConfigLoaderBuilder.default().addDecoders(
            listOf(
                KatalyzerConfig.RuleInterfaceDecoder(),
                KatalyzerConfig.NoArgumentDecoder(),
                KatalyzerConfig.AdvancedPropertiesDecoder(),
            )
        ).addFileSource(configPath).build().loadConfigOrThrow<ApplicationConfig>()

        runBlocking {  KatalyzerV2(config).run() }
    }

}

fun main(args: Array<String>) = KatalyzeAppV2().main(args)
