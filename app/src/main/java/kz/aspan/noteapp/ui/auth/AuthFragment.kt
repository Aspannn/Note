package kz.aspan.noteapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kz.aspan.noteapp.R
import kz.aspan.noteapp.data.remote.BasicAuthInterceptor
import kz.aspan.noteapp.databinding.FragmentAuthBinding
import kz.aspan.noteapp.other.Constants.EMPTY
import kz.aspan.noteapp.other.Constants.KEY_LOGGED_IN_EMAIL
import kz.aspan.noteapp.other.Constants.KEY_PASSWORD
import kz.aspan.noteapp.other.datastore.DataStoreUtil
import kz.aspan.noteapp.other.snackbar
import kz.aspan.noteapp.ui.auth.AuthViewModel.Event.*
import okhttp3.internal.wait
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    @Inject
    lateinit var dataStore: DataStoreUtil

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var curEmail: String? = null
    private var curPassword: String? = null

    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)


//        redirectLogin()


        subscribeToRegisterEvent()
        subscribeToLoginEvent()



        binding.btnRegister.setOnClickListener {
            binding.apply {
                val email = etRegisterEmail.text.toString()
                val password = etRegisterPassword.text.toString()
                val confirmedPassword = etPasswordConfirm.text.toString()
                viewModel.register(email, password, confirmedPassword)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()
            curEmail = email
            curPassword = password
            viewModel.login(email, password)
        }

    }


    private fun isLogging() {
        //TODO hz ne isteuge bolady
    }

    private fun authenticateApi(email: String, password: String) {
        basicAuthInterceptor.email = email
        basicAuthInterceptor.password = password
    }

    private fun redirectLogin() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.authFragment, true)
            .build()
        findNavController().navigate(
            AuthFragmentDirections.actionAuthFragmentToNotesFragment(),
            navOptions
        )
    }

    private fun subscribeToLoginEvent() = lifecycleScope.launchWhenCreated {
        viewModel.loginStatus.collect() { event ->
            when (event) {
                is LoadingEvent -> {
                    binding.etProgress.visibility = View.VISIBLE
                }
                is SuccessEvent -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(R.string.success_logged_in)
                    dataStore.setSecuredData(KEY_LOGGED_IN_EMAIL, curEmail!!)
                    dataStore.setSecuredData(KEY_PASSWORD, curPassword!!)
                    authenticateApi(curEmail ?: "", curPassword ?: "")
                    redirectLogin()
                }
                is ErrorEvent -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(event.error)
                }
                is ErrorInputEmpty -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(R.string.error_input_empty)
                }
                else -> Unit

            }
        }
    }


    private fun subscribeToRegisterEvent() = lifecycleScope.launchWhenCreated {
        viewModel.registerStatus.collect { event ->
            when (event) {
                is LoadingEvent -> {
                    binding.etProgress.visibility = View.VISIBLE
                }
                is ErrorEvent -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(event.error)
                }
                is ErrorInputEmpty -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(getString(R.string.error_input_empty))
                }
                is PasswordsDoNotMatch -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(getString(R.string.passwords_do_not_match))
                }
                is NotAValidEmail -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(R.string.error_not_a_valid_email)
                }
                is SuccessEvent -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(R.string.success_registration)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}