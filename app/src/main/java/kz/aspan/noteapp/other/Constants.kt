package kz.aspan.noteapp.other

import androidx.datastore.dataStore
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {
    const val USE_LOCALHOST = true
    const val HTTP_BASE_URL = ""
    const val HTTP_BASE_URL_LOCALHOST = "http://10.0.2.2:8080/"

    val IGNORE_AUTH_URLS = listOf("/login", "/register")

    const val DATABASE_NAME = "notes_db"

    const val ENCRYPTED_DATA_STORE_PREF_NAME = "enc_datastore_pref"
    const val KEY_LOGGED_IN_EMAIL = "KEY_LOGGED_IN_EMAIL"
    const val KEY_PASSWORD = "KEY_PASSWORD"

    val SECURED_DATA = stringPreferencesKey("secured_data")

}