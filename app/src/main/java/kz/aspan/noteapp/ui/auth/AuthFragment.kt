package kz.aspan.noteapp.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kz.aspan.noteapp.R
import kz.aspan.noteapp.databinding.FragmentAuthBinding
import kz.aspan.noteapp.other.datastore.DataStoreUtil
import kz.aspan.noteapp.other.snackbar
import kz.aspan.noteapp.ui.auth.AuthViewModel.Event.*
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    @Inject
    lateinit var dataStore: DataStoreUtil

    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)


        lifecycleScope.launchWhenStarted {
//            dataStore.setData("setData")
//            dataStore.setSecuredData("setSecuredData")
            dataStore.getSecuredData().collect {
                binding.btnLogin.text = it
            }
////
////
////            delay(6000L)
//
////            dataStore.getData().collect {
////                binding.btnLogin.text = it
////            }
////            delay(6000L)

        }

        subscribeToObservers()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.register(email, password, "qwert")
        }

    }

    private fun subscribeToObservers() = lifecycleScope.launchWhenCreated {
        viewModel.registerStatus.collect { event ->
            when (event) {
                is RegisterLoadingEvent -> {
                    binding.etProgress.visibility = View.VISIBLE
                }
                is RegisterErrorEvent -> {
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
                    snackbar(R.string.error_not_a_valid_email)
                }
                is RegisterEvent -> {
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