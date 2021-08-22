package kz.aspan.noteapp.ui.notes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kz.aspan.noteapp.R
import kz.aspan.noteapp.databinding.FragmentNoteDetailBinding
import kz.aspan.noteapp.databinding.FragmentNotesBinding
import kz.aspan.noteapp.other.Constants
import kz.aspan.noteapp.other.Constants.KEY_LOGGED_IN_EMAIL
import kz.aspan.noteapp.other.Constants.KEY_PASSWORD
import kz.aspan.noteapp.other.datastore.DataStoreUtil
import javax.inject.Inject

@AndroidEntryPoint
class NotesFragment : Fragment(R.layout.fragment_notes) {

    @Inject
    lateinit var dataStore: DataStoreUtil

    private var _binding: FragmentNotesBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)
        lifecycleScope.launchWhenStarted {
            binding.tvTest.text = dataStore.getSecuredData(KEY_LOGGED_IN_EMAIL)
        }

    }


    private fun logout() {
        lifecycleScope.launchWhenStarted {
            dataStore.setSecuredData(KEY_LOGGED_IN_EMAIL, "")
            dataStore.setSecuredData(KEY_PASSWORD, "")
        }
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.notesFragment, true)
            .build()
        findNavController().navigate(
            NotesFragmentDirections.actionNotesFragmentToAuthFragment(), navOptions
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_notes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miLogout -> logout()
        }
        return super.onOptionsItemSelected(item)
    }
}