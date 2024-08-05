package messageHandlers

import legacyfeed.SimpleMessage
import model.Contest

abstract class SingleMessageHandler(var tag: String) : MessageHandler {
    var contest: Contest? = null

    /* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#connectTo(model.Contest)
	 */
    override fun connectTo(contest: Contest) {
        this.contest = contest
    }

    /* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#supports(legacyfeed.SimpleMessage)
	 */
    override fun supports(message: SimpleMessage): Boolean {
        return tag == message.name
    }

    /* (non-Javadoc)
	 * @see messageHandlers.MessageHandler#process(legacyfeed.SimpleMessage)
	 */
    abstract override fun process(message: SimpleMessage)

    fun error(errorMessage: String?) {
        println(errorMessage)
    }

    override fun close() {
        // do nothing
    }
}