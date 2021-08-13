package kz.aspan.noteapp.repositories

import kz.aspan.noteapp.other.Resource

interface NoteRepository {
    suspend fun register(email: String, password: String): Resource<String>
}