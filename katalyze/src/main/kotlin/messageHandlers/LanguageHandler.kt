package messageHandlers

import legacyfeed.SimpleMessage
import model.Language
import org.apache.logging.log4j.kotlin.logger

class LanguageHandler : SingleMessageHandler("language") {
    override fun process(message: SimpleMessage) {
        val id = message.getInt("id")
        val languageName = message["name"].trim { it <= ' ' }
        val language = Language(id.toString(), languageName)
        contest!!.addLanguage(language)
    }

    companion object {
        private val logger = logger()
    }
}