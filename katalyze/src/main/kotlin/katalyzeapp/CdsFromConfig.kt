package katalyzeapp

import config.CdsConfig
import org.icpclive.cds.ktor.NetworkSettings
import kotlin.time.Clock
import org.icpclive.cds.plugins.clics.ClicsFeed
import org.icpclive.cds.plugins.clics.ClicsSettings
import org.icpclive.cds.plugins.clics.FeedVersion
import org.icpclive.cds.settings.*
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

fun CdsConfig.toCDSSettings(): CDSSettings = when (this) {
    is CdsConfig.ClicsServer -> ClicsSettings(
        feeds = listOf(
            ClicsFeed(
                source = UrlOrLocalPath.Url(
                    baseurl,
                    auth = Authorization(
                        basic = Authorization.BasicAuth(
                            login = Credential("login", username),
                            password = Credential("password", password.value),
                        ),
                    ),
                ),
                contestId = contestId,
            )
        )
    ) {
        network = NetworkSettings(
            allowUnsecureConnections = true,
            checkedServerName = checkedServerName,
        )
    }

    is CdsConfig.LocalPath -> ClicsSettings(
        feeds = listOf(
            ClicsFeed(
                source = UrlOrLocalPath.Local(Path(path)),
                eventFeedPath = "",
                eventFeedName = "",
                contestId = "",
                feedVersion = FeedVersion.`2023_06`,
            )
        )
    ) {
        emulation = EmulationSettings(speed = 5 * 60.0 * 60.0, startTime = Clock.System.now() + 1.seconds)
    }
}