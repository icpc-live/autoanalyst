package messageHandlers

import legacyfeed.SimpleMessage
import org.apache.logging.log4j.kotlin.logger

class NullHandler(tag: String) : SingleMessageHandler(tag) {
    override fun process(message: SimpleMessage) {
        logger.debug(String.format("Ignoring message %s", message))
    }

    companion object {
        private val logger = logger()
    }
}