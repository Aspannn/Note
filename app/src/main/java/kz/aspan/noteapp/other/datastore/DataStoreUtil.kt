package kz.aspan.noteapp.other.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kz.aspan.noteapp.other.Constants.EMPTY
import javax.inject.Inject

class DataStoreUtil
@Inject
constructor(
    private val dataStore: DataStore<Preferences>,
    private val security: SecurityUtil
) {
    private val bytesToStringSeparator = "|"

    fun getData(key: String) = dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey(key)].orEmpty()
        }

    suspend fun setData(key: String, value: String) {
        dataStore.edit {
            it[stringPreferencesKey(key)] = value
        }
    }

//    fun getSecuredData(key: String) = dataStore.data
//        .secureMap<String>(key) { pref ->
//            pref[stringPreferencesKey(key)].orEmpty()
//        }

    suspend fun getSecuredData(key: String): String {
        return dataStore.data
            .secureMap<String>(key) { pref ->
                pref[stringPreferencesKey(key)].orEmpty()
            }.first()
    }


    suspend fun setSecuredData(key: String, value: String) {
        dataStore.secureEdit(key, value) { prefs, encryptedValue ->
            prefs[stringPreferencesKey(key)] = encryptedValue
        }
    }

    suspend fun hasKey(key: Preferences.Key<*>) = dataStore.edit { it.contains(key) }

    suspend fun clearDataStore() {
        dataStore.edit {
            it.clear()
        }
    }

    private inline fun <reified T> Flow<Preferences>.secureMap(
        key: String,
        crossinline fetchValue: (value: Preferences) -> String
    ): Flow<T> {
        return map {
            val value = fetchValue(it)
            val decryptedValue = if (value.isNotEmpty()) {
                val data = value.split("_")
                security.decryptData(
                    key,
                    data[0].split(bytesToStringSeparator).map { str ->
                        str.toByte()
                    }.toByteArray(),
                    data[1].split(bytesToStringSeparator).map { str ->
                        str.toByte()
                    }.toByteArray()
                )
            } else {
                EMPTY
            }

            Gson().fromJson(decryptedValue, object : TypeToken<T>() {}.type)
        }
    }


    private suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
        key: String,
        value: T,
        crossinline editStore: (MutablePreferences, String) -> Unit
    ) {
        edit {
            val encryptedValues = security.encryptData(key, Gson().toJson(value))
            val test = encryptedValues[0].joinToString(bytesToStringSeparator)
            editStore.invoke(
                it, test + "_" + encryptedValues[1].joinToString(bytesToStringSeparator)
            )
        }
    }
}