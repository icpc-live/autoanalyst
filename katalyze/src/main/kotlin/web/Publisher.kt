package web

interface Publisher {
    fun publish(url: String, doc: WebDocument)
}
