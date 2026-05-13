package com.example.roommvvmdemo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.example.roommvvmdemo.data.NoteRepository;
import com.example.roommvvmdemo.data.local.Note;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private final NoteRepository repository;

    // LiveData pour la recherche
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // LiveData pour le filtre catégorie
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>(null);

    // Notes affichées selon recherche
    private final LiveData<List<Note>> displayedNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);

        // Switche dynamiquement entre recherche et liste complète
        displayedNotes = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllNotes();
            } else {
                return repository.searchNotes(query.trim());
            }
        });
    }

    public void insert(Note note) { repository.insert(note); }
    public void update(Note note) { repository.update(note); }
    public void delete(Note note) { repository.delete(note); }
    public void deleteAll() { repository.deleteAll(); }

    public LiveData<List<Note>> getDisplayedNotes() { return displayedNotes; }
    public LiveData<Integer> getNoteCount() { return repository.getNoteCount(); }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }

    public LiveData<List<Note>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    // Bascule l'épinglage d'une note
    public void togglePin(Note note) {
        note.setPinned(!note.isPinned());
        repository.update(note);
    }
}