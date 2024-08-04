package messageHandlers

import legacyfeed.SimpleMessage
import model.Contest

interface MessageHandler {
    @Throws(Exception::class)
    fun connectTo(contest: Contest)

    fun supports(message: SimpleMessage): Boolean

    @Throws(Exception::class)
    fun close()

    @Throws(Exception::class)
    fun process(message: SimpleMessage)
}