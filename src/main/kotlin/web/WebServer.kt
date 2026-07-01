package web

import com.sun.net.httpserver.HttpServer
import config.Constants
import java.awt.Desktop
import java.net.InetSocketAddress
import java.net.URI

/**
 * Starts the local browser converter.
 *
 * The server binds to 0.0.0.0 so GitHub Codespaces can forward port 8080.
 * On a normal local computer, users can still open http://localhost:8080.
 */
object WebServer {
    fun start(port: Int = 8080) {
        val server = HttpServer.create(InetSocketAddress("0.0.0.0", port), 0)
        val routes = WebRoutes()

        server.createContext("/") { exchange ->
            routes.handle(exchange)
        }

        server.executor = null
        server.start()

        val localUrl = "http://localhost:$port"

        println("${Constants.APP_NAME} ${Constants.VERSION}")
        println("Local converter running at:")
        println(localUrl)
        println()
        println("If using GitHub Codespaces:")
        println("  Open the forwarded port 8080 from the Ports tab.")
        println()
        println("Upload your favorites.msbf file in the browser.")
        println("Press Ctrl+C in the terminal to stop the server.")

        openBrowser(localUrl)
    }

    private fun openBrowser(url: String) {
        runCatching {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI(url))
            }
        }
    }
}