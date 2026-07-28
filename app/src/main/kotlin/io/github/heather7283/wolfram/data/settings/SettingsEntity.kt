package io.github.heather7283.wolfram.data.settings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Settings are stored in room db as a single row table.
// Yes, I know how retarded this is. Shut up.
@Entity(tableName = "settings")
data class SettingsEntity(
    // Lists are stored as JSON strings, Repository will convert them.
    //
    // I won't use room's TypeConverter feature because room is very stupid
    // and if it sees List<T> as dao method return type it automatically assumes
    // that the query will return multiple rows, each convertible to T,
    // and looks for type converter for T instead of for List<T>.
    //
    // I tried making a generic ListHolder and use delegation to make it more
    // ergonomic, but unfortunately this makes ksp just straight up crash lol
    val vpnAddressList: String,
    val vpnRouteList: String,
    @ColumnInfo(defaultValue = """[ "1.1.1.1", "8.8.8.8", "9.9.9.9" ]""")
    val dnsAddressList: String,

    val selectedAppsList: String,
    val selectedAppsIsWhitelist: Boolean,

    val statsEnabled: Boolean,
    val statsEndpoint: String,
    val statsPollInterval: Int,

    val activeConfigId: Long,

    // Since this is a single row table the only ID it will have is 1
    @PrimaryKey
    val id: Int = 1,
)
