package katalyzeapp

import config.YAMLConfiguration
import io.InputStreamConfigurator
import jsonfeed.JsonEvent
import jsonfeed.JsonEventReader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.ConfigurationException
import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.apache.log4j.xml.DOMConfigurator
import org.icpclive.cds.plugins.clics.ClicsFeed
import org.icpclive.cds.plugins.clics.ClicsSettingsBuilder
import org.icpclive.cds.settings.toFlow
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Katalyze {
    var logger: Logger = LogManager.getLogger(Katalyze::class.java)


    /**
     * @param args
     * @throws Exception
     */
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        System.setProperty("StartTime", (Instant.now().toEpochMilli() / 1000L).toString())
        DOMConfigurator.configure("log4j.xml")

        logger.info("Katalyzer started")

        var fileName: String? = null
        var configFileName: String? = null
        var config: Configuration? = null

        var i = 0
        while (i < args.size) {
            if ("-input" == args[i] && i < args.size - 1) {
                fileName = args[i + 1]
                i++
            } else if ("-config" == args[i] && i < args.size - 1) {
                configFileName = args[i + 1]
                i++
            } else {
                logger.warn("I don't understand argument " + args[i])
            }
            ++i
        }

        if (configFileName == null) {
            configFileName = "config.yaml"
        }

        try {
            config = YAMLConfiguration(configFileName)
        } catch (e: ConfigurationException) {
            logger.error(String.format("Error while parsing %s: %s.\nCause: %s", configFileName, e.message, e.cause))
        }

        val katalyzer = Katalyzer(config)
        katalyzer.start()

        val configSource = InputStreamConfigurator(config!!)
        val isp = if (fileName == null) {
            configSource.inputFromConfig
        } else {
            configSource.createFileReader(fileName)
        }

        try {
            val contestFlow = ClicsSettingsBuilder(listOf(isp!!.clicsFeed)).build().toFlow();

            launch {
                while (true) {
                    katalyzer.updateScoreboards(false);
                    delay(10.toDuration(DurationUnit.SECONDS));
                }
            }
            contestFlow.buffer().collect { contestUpdate -> katalyzer.processContestUpdate(contestUpdate) }
            katalyzer.updateScoreboards(true);

            logger.info("Katalyzer stream finished (press <Enter> to exit)")

            System.`in`.read()
        } catch (e: Exception) {
            logger.error("Katalyzer fatal error, terminating", e)
        } finally {
            katalyzer.stop()
        }
    }
}


