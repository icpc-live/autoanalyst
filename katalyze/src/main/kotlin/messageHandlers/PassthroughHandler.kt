package messageHandlers

import legacyfeed.EventFeedFile
import legacyfeed.MessageXmlSerializer
import legacyfeed.SimpleMessage
import model.Contest
import model.EventImportance
import model.LoggableEvent
import model.NotificationTarget
import org.apache.logging.log4j.kotlin.logger
import java.io.IOException
import java.io.PrintStream
import javax.xml.stream.XMLStreamException

class PassthroughHandler(var eventFeedFile: EventFeedFile) : MessageHandler, NotificationTarget {
    var output: PrintStream = eventFeedFile.stream
    var serializer: MessageXmlSerializer = MessageXmlSerializer(output)

    override fun connectTo(contest: Contest) {
        // do nothing.
    }

    override fun supports(message: SimpleMessage): Boolean {
        return true
    }


    @Throws(XMLStreamException::class, IOException::class)
    override fun close() {
        serializer.close()
        eventFeedFile.close()
    }

    @Throws(XMLStreamException::class, IOException::class)
    override fun process(message: SimpleMessage) {
        val messageName = message.name
        if ("!endStream" == messageName) {
            close()
        } else if ("!beginStream" == messageName) {
            serializer.init()
        } else {
            serializer.serialize(message)
        }
    }

    private fun getEventImportanceNumber(src: EventImportance): Int {
        return when (src) {
            EventImportance.Breaking -> 0
            EventImportance.AnalystMessage -> 1
            EventImportance.Normal -> 2
            EventImportance.Whatever -> 3
            else ->                 // As of this writing, we should not end up here, but in case more
                // items are added to the enumeration, let them just have 'normal' priority until
                // we know...
                2
        }
    }

    private fun createFeedMessageFromEvent(event: LoggableEvent): SimpleMessage {
        val eventMessage = SimpleMessage("analystmsg")
        eventMessage.put("id", event.id.toString())
        if (event.team != null) {
            eventMessage.put("team", event.team.id)
        }
        eventMessage.put("time", event.contestTimeMinutes().toString())
        eventMessage.put("priority", getEventImportanceNumber(event.importance).toString())
        if (event.submission != null) {
            val submission = event.submission
            eventMessage.put("problem", submission.problem.id)
            eventMessage.put("run_id", submission.getId())
            eventMessage.put("judgement", submission.getId())
        } else {
            eventMessage.put("judgement", "-1")
        }

        var category: String? = "auto"
        if (event.supplements != null) {
            val categorySupplement = event.supplements["category"]
            if (categorySupplement != null) {
                category = categorySupplement
            }
        }
        eventMessage.put("category", category)
        eventMessage.put("message", event.message)
        return eventMessage
    }

    override fun notify(event: LoggableEvent) {
        try {
            serializer.serialize(createFeedMessageFromEvent(event))
        } catch (e: Exception) {
            logger.error(String.format("Error while posting event to xml feed: %s", e))
            // If there is an error, there isn't much more we can do than log it and hope that someone notices.
            // The show must go on.
        }
    }

    companion object {
        private val logger = logger()
    }
}