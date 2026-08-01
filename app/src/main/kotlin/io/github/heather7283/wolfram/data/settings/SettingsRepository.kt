package io.github.heather7283.wolfram.data.settings

import android.app.Application
import arrow.core.Either
import io.github.heather7283.wolfram.data.WolframDatabase
import io.github.heather7283.wolfram.utils.CIDR
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.net.InetAddress

@Singleton
class SettingsRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).settingsDao()

    // TODO: is there a better way to do this?
    private fun toSettings(e: SettingsEntity) = Settings(
        vpnAddresses = toCidrList(e.vpnAddressList),
        vpnRoutes = toCidrList(e.vpnRouteList),
        dnsAddresses = toIpList(e.dnsAddressList),
        selectedApps = toStringList(e.selectedAppsList),
        selectedAppsIsWhitelist = e.selectedAppsIsWhitelist,
        activeConfigId = e.activeConfigId,
        statsEnabled = e.statsEnabled,
        statsEndpoint = e.statsEndpoint,
        statsPollInterval = e.statsPollInterval,
    )

    val settingsFlow = dao.observeAll().map(::toSettings)
    fun getSettings() = toSettings(dao.getAll())

    private fun toIpList(value: String): List<InetAddress> = Either.catch {
        Json.decodeFromString<List<String>>(value).mapNotNull {
            InetAddress.getByName(it)
        }
    }.fold(
        ifLeft = { Timber.e(it, "Could not parse ${value} as list of IPs"); emptyList() },
        ifRight = { it },
    )

    private fun toCidrList(value: String): List<CIDR> {
        // stored as [ "0.0.0.0/0", "192.168.0.1/24" ]
        return Either.catch { Json.decodeFromString<List<String>>(value) }.fold(
            ifLeft = { Timber.e(it, "Could not parse ${value} as list of strings"); emptyList() },
            ifRight = {
                it.mapNotNull { cidrStr ->
                    CIDR.fromString(cidrStr).fold(
                        ifLeft = { Timber.e(it, "Could not parse ${cidrStr} as CIDR"); null },
                        ifRight = { it }
                    )
                }
            }
        )
    }

    private fun fromIpList(list: List<InetAddress>): String {
        return Json.encodeToString(list.map { it.hostAddress })
    }

    private fun fromCidrList(list: List<CIDR>): String {
        return Json.encodeToString(list.map { "${it.ip.hostAddress}/${it.prefix}" })
    }

    private fun toStringList(value: String): List<String> {
        // stored as [ "a", "b" ]
        return Either.catch { Json.decodeFromString<List<String>>(value) }.fold(
            ifLeft = { Timber.e(it, "Could not parse ${value} as list of strings"); emptyList() },
            ifRight = { it }
        )
    }

    private fun fromStringList(list: List<String>): String {
        return Json.encodeToString(list)
    }

    suspend fun addVpnAddress(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnAddressList())
            val new = old + listOf(cidr)
            dao.setVpnAddressList(fromCidrList(new))
        }
    }
    suspend fun removeVpnAddress(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnAddressList())
            val new = old.filterNot { it == cidr }
            dao.setVpnAddressList(fromCidrList(new))
        }
    }

    suspend fun addVpnRoute(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnRouteList())
            val new = old + listOf(cidr)
            dao.setVpnRouteList(fromCidrList(new))
        }
    }
    suspend fun removeVpnRoute(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnRouteList())
            val new = old.filterNot { it == cidr }
            dao.setVpnRouteList(fromCidrList(new))
        }
    }

    suspend fun addDnsAddress(address: InetAddress) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toIpList(dao.getDnsAddressList())
            val new = old + listOf(address)
            dao.setDnsAddressList(fromIpList(new))
        }
    }
    suspend fun removeDnsAddress(address: InetAddress) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toIpList(dao.getDnsAddressList())
            val new = old.filterNot { it == address }
            dao.setDnsAddressList(fromIpList(new))
        }
    }

    suspend fun addSelectedApp(app: String) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toStringList(dao.getSelectedAppsList())
            val new = old + listOf(app)
            dao.setSelectedAppsList(fromStringList(new))
        }
    }
    suspend fun removeSelectedApp(app: String) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toStringList(dao.getSelectedAppsList())
            val new = old.filterNot { it == app }
            dao.setSelectedAppsList(fromStringList(new))
        }
    }

    suspend fun setSelectedAppsIsWhitelist(value: Boolean) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.setSelectedAppsIsWhitelist(value)
        }
    }

    suspend fun setActiveConfigId(id: Long) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.setActiveConfigId(id)
        }
    }

    suspend fun setStatsEnabled(enabled: Boolean) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.setStatsEnabled(enabled)
        }
    }
    suspend fun setStatsEndpoint(endpoint: String) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.setStatsEndpoint(endpoint)
        }
    }
    suspend fun setStatsPollInterval(seconds: Int) = Either.catch {
        withContext(Dispatchers.IO) {
            dao.setStatsPollInterval(seconds)
        }
    }
}
