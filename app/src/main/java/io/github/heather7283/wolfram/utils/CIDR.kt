package io.github.heather7283.wolfram.utils

import arrow.core.Either
import arrow.core.flatMap
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

data class CIDR(
    val ip: InetAddress,
    val prefix: Int,
) {
    companion object {
        fun fromString(str: String) = Either.catch {
            require(str.count { it == '/' } == 1) { "A single / is expected" }

            val (ipStr, prefixStr) = str.split("/")
            val prefix = prefixStr.toInt()

            Pair(ipStr, prefix)
        }.flatMap {
            fromIpAndPrefix(it.first, it.second)
        }

        fun fromIpAndPrefix(ip: String, prefix: Int) = Either.catch {
            require(ip.isNotBlank()) { "Empty IP string" }

            val ip = InetAddress.getByName(ip)

            require(prefix >= 0) { "Prefix must be positive" }
            require((ip is Inet4Address) implies (prefix <= 32)) { "Prefix must be <= 32" }
            require((ip is Inet6Address) implies (prefix <= 128)) { "Prefix must be <= 128" }

            CIDR(ip, prefix)
        }
    }
}
