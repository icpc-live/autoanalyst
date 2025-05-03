package io

import jsonfeed.ContestEntry
import org.apache.commons.configuration.Configuration
import org.apache.commons.configuration.ConfigurationException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.icpclive.cds.plugins.clics.ClicsFeed
import org.icpclive.cds.settings.Authorization
import org.icpclive.cds.settings.Credential
import org.icpclive.cds.settings.UrlOrLocalPath
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate

class InputStreamConfigurator(var config: Configuration) {
    fun createFeedClient(): HttpFeedClient {
        val user = config.getString("CDS.user")
        val pass = config.getString("CDS.pass")

        val bypassCert = config.getBoolean("CDS.bypasscert", false)

        val client = HttpFeedClient()
        if (bypassCert) {
            client.bypassCertErrors_DANGER(true)
        }

        client.setCredentials(user, pass)

        return client
    }


    val apiBase: String
        get() = config.getString("CDS.baseurl")


    private fun filterByPrefix(prefix: String?): Predicate<ContestEntry> {
        if (prefix == null) {
            return Predicate { x: ContestEntry? -> true }
        } else {
            val lowercasePrefix = prefix.lowercase(Locale.getDefault())
            return Predicate { x: ContestEntry -> x.getLowercaseId().startsWith(lowercasePrefix) }
        }
    }


    @Throws(IOException::class)
    fun createHttpReader(): InputStreamProvider? {
        val contestIdPrefix = config.getString("CDS.contestIdPrefix")

        val feedClient = createFeedClient()


        val entry = feedClient.guessContest(apiBase, filterByPrefix(contestIdPrefix))
        if (entry == null) {
            log.error(String.format("Unable to locate a contest whose feed to read at %s", apiBase))
            return null
        } else {
            log.info(String.format("Will read from contest %s", entry))
        }

        val loginCred = Credential("CDS.user", config.getString("CDS.user"))
        val passwordCred = Credential("CDS.pass", config.getString("CDS.pass"))
        val auth = Authorization(
            Authorization.BasicAuth(loginCred, passwordCred)
        )

        //        return (resumePoint, isStreamToken) -> {
//            String url = entry.url+"/event-feed";
//            String argumentName = (isStreamToken) ? "since_token" : "since_id";
//
//            String resumeQuery = (resumePoint != null) ? "?"+argumentName+"="+URLEncoder.encode(resumePoint, StandardCharsets.UTF_8.name()) : "";
//            return feedClient.getInputStream(url+resumeQuery);
//        };
        return InputStreamProvider {
            ClicsFeed(
                source = UrlOrLocalPath.Url(apiBase, auth),
                contestId = entry.contest.id,
            )
        }
    }


    @Throws(FileNotFoundException::class)
    fun createFileReader(file: String): InputStreamProvider {
        //val inputFile = FileInputStream(file)

        return InputStreamProvider {
            ClicsFeed(
                source = UrlOrLocalPath.Local(Path.of(file)),
                contestId = "TODO"
            )
        }
        //return InputStreamProvider { resumePoint, isStreamToken -> inputFile }
    }

    fun createConsoleReader(): InputStreamProvider {
        return InputStreamProvider {
            ClicsFeed(
                source = UrlOrLocalPath.Local(Path.of("/dev/stdin")),
                contestId = "TODO",
            )
        }
        //return InputStreamProvider { resumePoint, isStreamToken -> System.`in` }
    }

    @get:Throws(ConfigurationException::class, IOException::class)
    val inputFromConfig: InputStreamProvider?
        get() {
            try {
                val source = config.getString("source", "stdin").lowercase(Locale.getDefault())
                if (source == "cds") {
                    return createHttpReader()
                }
                if (source == "stdin") {
                    return createConsoleReader()
                } else {
                    throw ConfigurationException(String.format("%s is not a recognized data source", source))
                }
            } catch (e: MalformedURLException) {
                throw ConfigurationException(String.format("The given url was not in a valid format. Error %s", e))
            }
        }

    companion object {
        private val log: Logger = LogManager.getLogger(
            InputStreamConfigurator::class.java
        )
    }
}
