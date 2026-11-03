package com.niga.vpn

import android.util.Base64
import java.net.HttpURLConnection
import java.net.URL

data class SubscriptionServer(val name: String, val protocol: String)

object SubscriptionParser {
    /**
     * Fetches a V2Ray/Xray subscription response and identifies common URI formats.
     * This is only an importer/parser prototype; it does not establish VPN connections.
     */
    fun fetch(subscriptionUrl: String): List<SubscriptionServer> {
        require(subscriptionUrl.startsWith("https://")) { "فقط لینک HTTPS پذیرفته می‌شود." }
        val connection = (URL(subscriptionUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 20000
            setRequestProperty("User-Agent", "NigaVPN/0.1 Android")
            instanceFollowRedirects = true
        }
        try {
            val status = connection.responseCode
            if (status !in 200..299) throw IllegalStateException("HTTP $status")
            val raw = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            val decoded = decodeIfBase64(raw)
            return decoded.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .mapNotNull { line ->
                    val scheme = line.substringBefore("://", "").lowercase()
                    val protocol = when (scheme) {
                        "vless" -> "VLESS / Xray"
                        "vmess" -> "VMess"
                        "trojan" -> "Trojan"
                        "ss" -> "Shadowsocks"
                        "socks" -> "SOCKS"
                        else -> null
                    } ?: return@mapNotNull null
                    val fragment = line.substringAfter("#", "")
                    val displayName = fragment.takeIf { it.isNotBlank() }
                        ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                        ?: protocol
                    SubscriptionServer(displayName, protocol)
                }
                .distinctBy { it.name to it.protocol }
                .toList()
        } finally {
            connection.disconnect()
        }
    }

    private fun decodeIfBase64(value: String): String {
        val trimmed = value.trim()
        if (trimmed.contains("://")) return trimmed
        val compact = trimmed.filterNot { it.isWhitespace() }
        val candidates = listOf(compact, compact.replace('-', '+').replace('_', '/'))
        for (candidate in candidates) {
            try {
                val padded = candidate + "=".repeat((4 - candidate.length % 4) % 4)
                val decoded = String(Base64.decode(padded, Base64.DEFAULT), Charsets.UTF_8)
                if (decoded.contains("://")) return decoded
            } catch (_: Exception) { }
        }
        return trimmed
    }
}
