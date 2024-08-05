package messageHandlers

import legacyfeed.SimpleMessage
import model.Contest
import org.apache.logging.log4j.kotlin.logger
import java.sql.Connection

class ContestMessages(var contest: Contest) {
    var handlers: ArrayList<MessageHandler> = ArrayList()
    var standardHandlersAdded: Boolean = false

    @Throws(Exception::class)
    fun add(handler: MessageHandler) {
        handler.connectTo(contest)
        handlers.add(handler)
    }

    @Throws(Exception::class)
    fun addStandardHandlers(connection: Connection?) {
        check(!standardHandlersAdded) { "Standard handlers may not be added twice!" }

        add(ProblemHandler())
        add(RunHandler())
        add(TeamHandler(connection))
        add(TestCaseHandler())
        add(LanguageHandler())
        add(NullHandler("clar"))
        standardHandlersAdded = true
    }

    fun process(message: SimpleMessage) {
        for (handler in handlers) {
            if (handler.supports(message)) {
                try {
                    handler.process(message)
                } catch (e: Exception) {
                    logger.error(String.format("Message hander %s raised an error processing %s", handler, message))
                    e.printStackTrace()
                }
            }
        }
    }

    fun close() {
        for (handler in handlers) {
            try {
                handler.close()
            } catch (e: Exception) {
                logger.error(String.format("Message hander %s raised an error during close(): %s", handler, e))
                e.printStackTrace()
            }
        }
    }


    companion object {
        private val logger = logger()
    }
}