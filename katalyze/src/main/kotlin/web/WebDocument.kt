package web

import java.io.IOException
import java.io.OutputStream

interface WebDocument {
    val contentType: String

    @Throws(IOException::class)
    fun writeContents(target: OutputStream)
    val isGzipCompressed: Boolean
}