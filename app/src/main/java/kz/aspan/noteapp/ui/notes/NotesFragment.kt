package kz.aspan.noteapp.ui.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import kz.aspan.noteapp.R
import kz.aspan.noteapp.databinding.FragmentNoteDetailBinding

class NotesFragment : Fragment(R.layout.fragment_notes) {

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: NotesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNoteDetailBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}