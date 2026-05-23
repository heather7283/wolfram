package io.github.heather7283.wolfram.utils

import arrow.core.Either
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

data class CIDR(
    val ip: InetAddress,
    val prefix: Int,
) {
    companion object {
        fun parse(str: String) = Either.catch {
            require(str.count { it == '/' } == 1) { "A single / is expected" }

            val (ipStr, prefixStr) = str.split("/")
            val ip = InetAddress.getByName(ipStr)
            val prefix = prefixStr.toInt()

            require(prefix >= 0) { "Prefix must be positive" }
            require((ip is Inet4Address) implies (prefix <= 32)) { "Prefix must be <= 32" }
            require((ip is Inet6Address) implies (prefix <= 128)) { "Prefix must be <= 128" }

            CIDR(ip, prefix)
        }
    }
}
