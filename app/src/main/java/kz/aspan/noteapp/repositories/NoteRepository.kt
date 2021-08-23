package kz.aspan.noteapp.repositories

import kotlinx.coroutines.flow.Flow
import kz.aspan.noteapp.data.local.entities.Note
import kz.aspan.noteapp.other.Resource

interface NoteRepository {
    fun getAllNotes(): Flow<Resource<out List<Note>>>

    suspend fun register(email: String, password: String): Resource<String>

    suspend fun login(email: String, password: String): Resource<String>
}