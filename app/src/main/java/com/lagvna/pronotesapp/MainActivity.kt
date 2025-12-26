package com.lagvna.pronotesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lagvna.pronotesapp.data.database.NoteDatabase
import com.lagvna.pronotesapp.data.repository.NoteRepository
import com.lagvna.pronotesapp.ui.adapter.NoteAdapter
import com.lagvna.pronotesapp.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import androidx.appcompat.app.AlertDialog
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.lagvna.pronotesapp.data.model.Note
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fab = findViewById<FloatingActionButton>(R.id.fabAddNote)

        fab.setOnClickListener {
            showAddNoteDialog()
        }

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        noteAdapter = NoteAdapter(
            notes = emptyList(),
            onEditClick = { note ->
                showEditNoteDialog(note)
            },
            onDeleteClick = { note ->
                noteViewModel.deleteNote(note)
            }
        )

        recyclerView.adapter = noteAdapter

        // Database & Repository
        val dao = NoteDatabase.getDatabase(this).noteDao()
        val repository = NoteRepository(dao)

        // ViewModel
        noteViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NoteViewModel(repository) as T
                }
            }
        )[NoteViewModel::class.java]

        // Observe notes
        lifecycleScope.launch {
            noteViewModel.notes.collect { notes ->
                noteAdapter.updateNotes(notes)
            }
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_note, null)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)

        AlertDialog.Builder(this)
            .setTitle("Nueva nota")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val text = etNote.text.toString()
                if (text.isNotBlank()) {
                    noteViewModel.insertNote(text)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showEditNoteDialog(note: Note) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_note, null)
        val etNote = dialogView.findViewById<EditText>(R.id.etNote)
        etNote.setText(note.text)

        AlertDialog.Builder(this)
            .setTitle("Editar nota")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val updatedText = etNote.text.toString()
                if (updatedText.isNotBlank()) {
                    val updatedNote = note.copy(text = updatedText)
                    noteViewModel.updateNote(updatedNote)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


}
