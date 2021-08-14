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
import kz.aspan.noteapp.other.Constants.DATA
import kz.aspan.noteapp.other.Constants.SECURED_DATA
import javax.inject.Inject

class DataStoreUtil
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {
    private val securityKeyAlias = "data-store"
    private val bytesToStringSeparator = "|"

    fun getData() = dataStore.data
        .map { preferences ->
            preferences[DATA].orEmpty()
        }

    suspend fun setData(value: String) {
        dataStore.edit {
            it[DATA] = value
        }
    }

    fun getSecuredData() = dataStore.data
        .secureMap<String> { pref ->
            pref[SECURED_DATA].orEmpty()
        }

    suspend fun setSecuredData(value: String) {
        dataStore.secureEdit(value) { prefs, encryptedValue ->
            prefs[SECURED_DATA] = encryptedValue
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