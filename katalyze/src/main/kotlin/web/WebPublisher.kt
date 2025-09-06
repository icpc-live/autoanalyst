package web

import java.util.concurrent.ConcurrentHashMap

class WebPublisher(useCompression: Boolean) : Publisher {
    var documents: ConcurrentHashMap<String, WebDocument> = ConcurrentHashMap<String, WebDocument>()
    var useCompression: Boolean = false

    init {
        this.useCompression = useCompression
    }

    override fun publish(url: String, doc: WebDocument) {
        var doc = doc
        if (useCompression && !doc.isGzipCompressed) {
            doc = CompressedWebDocument.Compress(doc)
        }
        documents.put(url, doc)
    }

    fun get(url: String?): WebDocument? {
        return documents.get(url)
    }
}