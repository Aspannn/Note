package kz.aspan.noteapp.repositories

import android.content.Context
import kz.aspan.noteapp.R
import kz.aspan.noteapp.data.local.NoteDao
import kz.aspan.noteapp.data.remote.NoteApi
import kz.aspan.noteapp.data.remote.requests.AccountRequest
import kz.aspan.noteapp.other.Resource
import kz.aspan.noteapp.other.checkForInternetConnection
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class DefaultNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteApi: NoteApi,
    private val context: Context
) : NoteRepository {

    override suspend fun register(email: String, password: String): Resource<String> {
        if (!context.checkForInternetConnection()) {
            return Resource.Error(context.getString(R.string.error_internet_turned_off))
        }

        val response = try {
            noteApi.register(AccountRequest(email, password))
        } catch (e: HttpException) {
            return Resource.Error(context.getString(R.string.error_http))
        } catch (e: IOException) {
            return Resource.Error(context.getString(R.string.check_internet_connection))
        }

        return if (response.isSuccessful && response.body()?.successful == true) {
            Resource.Success(response.body()!!.message)
        } else if (response.body()?.successful == false) {
            Resource.Error(response.body()!!.message)
        } else {
            Resource.Error(context.getString(R.string.error_unknown))
        }
    }

    override suspend fun login(email: String, password: String): Resource<String> {
        if (!context.checkForInternetConnection()) {
            return Resource.Error(context.getString(R.string.error_internet_turned_off))
        }

        val response = try {
            noteApi.login(AccountRequest(email, password))
        } catch (e: HttpException) {
            return Resource.Error(context.getString(R.string.error_http))
        } catch (e: IOException) {
            return Resource.Error(context.getString(R.string.check_internet_connection))
        }

        return if (response.isSuccessful && response.body()?.successful == true) {
            Resource.Success(response.body()!!.message)
        } else if (response.body()?.successful == false) {
            Resource.Error(response.body()!!.message)
        } else {
            Resource.Error(context.getString(R.string.error_unknown))
        }

    }

}