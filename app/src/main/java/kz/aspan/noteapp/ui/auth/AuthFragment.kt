package kz.aspan.noteapp.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kz.aspan.noteapp.R
import kz.aspan.noteapp.databinding.FragmentAuthBinding
import kz.aspan.noteapp.other.snackbar
import kz.aspan.noteapp.ui.auth.AuthViewModel.Event.*

@AndroidEntryPoint
class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding: FragmentAuthBinding
        get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        binding.btnLogin.setOnClickListener {

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
                    binding.etProgress.text = event.error
                    snackbar(event.error)
                }
                is InputEmptyError -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(getString(R.string.empty_field))
                }
                is PasswordsDoNotMatch -> {
                    binding.etProgress.visibility = View.GONE
                    snackbar(getString(R.string.passwords_do_not_match))
                }
                is RegisterEvent -> {
                    binding.etProgress.text = event.message
                    binding.etProgress.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}