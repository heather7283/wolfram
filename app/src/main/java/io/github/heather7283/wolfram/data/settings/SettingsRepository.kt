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

@Singleton
class SettingsRepository @Inject constructor(app: Application) {
    private val dao = WolframDatabase.getInstance(app.applicationContext).settingsDao()

    // TODO: is there a better way to do this?
    private fun toSettings(e: SettingsEntity) = Settings(
        vpnAddresses = toCidrList(e.vpnAddressList),
        vpnRoutes = toCidrList(e.vpnRouteList),
        activeConfigId = e.activeConfigId,
        statsEnabled = e.statsEnabled,
        statsEndpoint = e.statsEndpoint,
    )

    val settingsFlow = dao.observeAll().map(::toSettings)
    fun getSettings() = toSettings(dao.getAll())

    private fun toCidrList(value: String): List<CIDR> {
        // stored as [ "0.0.0.0/0", "192.168.0.1/24" ]
        return Either.catch { Json.decodeFromString<List<String>>(value) }.fold(
            ifLeft = { Timber.e(it, "Could not parse ${value} as list of strings"); emptyList() },
            ifRight = {
                it.mapNotNull { cidrStr ->
                    CIDR.parse(cidrStr).fold(
                        ifLeft = { Timber.e(it, "Could not parse ${cidrStr} as CIDR"); null },
                        ifRight = { it }
                    )
                }
            }
        )
    }

    private fun fromCidrList(list: List<CIDR>): String {
        return Json.encodeToString(list.map { "${it.ip.hostAddress}/${it.prefix}" })
    }

    suspend fun addVpnAddress(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnAddressList())
            val new = old + listOf(cidr)
            dao.updateVpnAddressList(fromCidrList(new))
        }
    }

    suspend fun removeVpnAddress(cidr: CIDR) = Either.catch {
        withContext(Dispatchers.IO) {
            val old = toCidrList(dao.getVpnAddressList())
            val new = old.filterNot { it == cidr }
            dao.updateVpnAddressList(fromCidrList(new))
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
}
