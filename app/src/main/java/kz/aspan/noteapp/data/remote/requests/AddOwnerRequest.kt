package kz.aspan.noteapp.data.remote.requests

data class AddOwnerRequest(
    val owner: String,
    val noteId: String
)
