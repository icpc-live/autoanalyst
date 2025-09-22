package web

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.GZIPOutputStream

class CompressedWebDocument(override val contentType: String, val compressedContent: ByteArray) : WebDocument {
    @Throws(IOException::class)
    override fun writeContents(target: OutputStream) {
        target.write(compressedContent)
    }

    override val isGzipCompressed: Boolean = true

    companion object {
        fun Compress(source: WebDocument): WebDocument {
            if (source.isGzipCompressed) {
                return source
            }

            return CompressedWebDocument(source.contentType, compress(source))
        }

        private fun compress(source: WebDocument): ByteArray {
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                val gzipOutputStream = GZIPOutputStream(byteArrayOutputStream)
                source.writeContents(gzipOutputStream)
                gzipOutputStream.close()
                byteArrayOutputStream.flush()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            return byteArrayOutputStream.toByteArray()
        }
    }
}