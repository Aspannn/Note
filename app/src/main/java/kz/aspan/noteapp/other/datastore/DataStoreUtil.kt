package kz.aspan.noteapp.other.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreUtil
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {
    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"

    fun getSecuredData(preferencesKey: String) = dataStore.data
        .secureMap<String> { pref ->
            pref[stringPreferencesKey(preferencesKey)].orEmpty()
        }

    suspend fun setSecuredData(preferencesKey: String, value: String?) {
        dataStore.secureEdit(value) { prefs, encryptedValue ->
            prefs[stringPreferencesKey(preferencesKey)] = encryptedValue
        }
    }

    suspend fun hasKey(key: Preferences.Key<*>) = dataStore.edit { it.contains(key) }

    suspend fun clearDataStore() {
        dataStore.edit {
            it.clear()
        }
    }

    private inline fun <reified T> Flow<Preferences>.secureMap(crossinline fetchValue: (value: Preferences) -> String): Flow<T> {
        return map {
            val decryptedValue = security.decryptData(
                securityKeyAlias,
                fetchValue(it).split(bytesToStringSeparator).map { str ->
                    str.toByte()
                }.toByteArray()
            )
            Gson().fromJson(decryptedValue, object : TypeToken<T>() {}.type)
        }
    }


    private suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        value: T,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValue = security.encryptData(securityKeyAlias, Gson().toJson(value))
            editStore.invoke(it, encryptedValue.joinToString(bytesToStringSeparator))
        }
    }
}