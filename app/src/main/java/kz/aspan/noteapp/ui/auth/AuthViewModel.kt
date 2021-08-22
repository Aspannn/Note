package kz.aspan.noteapp.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kz.aspan.noteapp.other.DispatcherProvider
import kz.aspan.noteapp.other.Resource
import kz.aspan.noteapp.repositories.NoteRepository
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    sealed class Event {
        object LoadingEvent : Event()
        object ErrorInputEmpty : Event()
        object PasswordsDoNotMatch : Event()
        object NotAValidEmail : Event()

        data class SuccessEvent(val message: String) : Event()
        data class ErrorEvent(val error: String) : Event()
    }

    private val _registerStatus = MutableSharedFlow<Event>()
    val registerStatus: SharedFlow<Event> = _registerStatus

    private val _loginStatus = MutableSharedFlow<Event>()
    val loginStatus: SharedFlow<Event> = _loginStatus

    fun login(email: String, password: String) {
        viewModelScope.launch(dispatchers.io) {
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()
            _loginStatus.emit(Event.LoadingEvent)

            when {
                (trimmedEmail.isEmpty() || trimmedPassword.isEmpty()) -> {
                    _loginStatus.emit(Event.ErrorInputEmpty)
                }
                else -> {
                    val result = repository.login(trimmedEmail, trimmedPassword)
                    if (result is Resource.Success) {
                        _loginStatus.emit(Event.SuccessEvent(result.data ?: return@launch))
                    } else {
                        _loginStatus.emit(Event.ErrorEvent(result.message ?: return@launch))
                    }
                }
            }
        }
    }

    fun register(email: String, password: String, repeatedPassword: String) {
        viewModelScope.launch(dispatchers.io) {
            val trimmedEmail = email.trim()
            val trimmedPassword = password.trim()
            val trimmedRepeatPassword = repeatedPassword.trim()
            _registerStatus.emit(Event.LoadingEvent)

            when {
                (trimmedEmail.isEmpty() || trimmedPassword.isEmpty() || trimmedRepeatPassword.isEmpty()) -> {
                    _registerStatus.emit(Event.ErrorInputEmpty)
                }
                trimmedPassword != trimmedRepeatPassword -> {
                    _registerStatus.emit(Event.PasswordsDoNotMatch)
                }
                !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> {
                    _registerStatus.emit(Event.NotAValidEmail)
                }
                else -> {
                    val result = repository.register(trimmedEmail, trimmedPassword)
                    if (result is Resource.Success) {
                        _registerStatus.emit(Event.SuccessEvent(result.data ?: return@launch))
                    } else {
                        _registerStatus.emit(
                            Event.ErrorEvent(result.message ?: return@launch)
                        )
                    }
                }
            }
        }
    }


}