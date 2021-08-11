package kz.aspan.noteapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kz.aspan.noteapp.data.local.NotesDatabase
import kz.aspan.noteapp.data.remote.BasicAuthInterceptor
import kz.aspan.noteapp.data.remote.NoteApi
import kz.aspan.noteapp.other.Constants
import kz.aspan.noteapp.other.Constants.DATABASE_NAME
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideNoteDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(context, NotesDatabase::class.java, DATABASE_NAME).build()

    @Singleton
    @Provides
    fun provideNoteDao(db: NotesDatabase) = db.noteDao()

    @Singleton
    @Provides
    fun provideBasicAuthInterceptor() = BasicAuthInterceptor()

    @Singleton
    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context) = context

    @Singleton
    @Provides
    fun provideNoteApi(
        basicAuthInterceptor: BasicAuthInterceptor
    ): NoteApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(basicAuthInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        return Retrofit.Builder()
            .baseUrl(if (Constants.USE_LOCALHOST) Constants.HTTP_BASE_URL_LOCALHOST else Constants.HTTP_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(NoteApi::class.java)

    }
}