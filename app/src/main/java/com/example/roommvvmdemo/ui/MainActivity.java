package com.example.roommvvmdemo.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roommvvmdemo.R;
import com.example.roommvvmdemo.data.local.Note;
import com.example.roommvvmdemo.viewmodel.NoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class MainActivity extends AppCompatActivity {

    private NoteViewModel viewModel;
    private NoteAdapter adapter;
    private EditText etSearch;
    private TextView tvCount;
    private String activeCategory = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vues
        etSearch = findViewById(R.id.etSearch);
        tvCount = findViewById(R.id.tvCount);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        // RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Observer la liste
        viewModel.getDisplayedNotes().observe(this, notes -> {
            adapter.setNotes(notes);
            if (notes.isEmpty()) {
                findViewById(R.id.tvEmpty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tvEmpty).setVisibility(View.GONE);
            }
        });

        // Compteur
        viewModel.getNoteCount().observe(this, count ->
                tvCount.setText(count + " note" + (count > 1 ? "s" : "")));

        // Recherche en temps réel
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                viewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // FAB → dialog d'ajout
        fabAdd.setOnClickListener(v -> showAddNoteDialog());

        // Chips filtre catégorie
        setupChips(chipGroup);

        // Clic sur une note → dialog détail/édition
        adapter.setOnNoteClickListener(note -> showNoteDetailDialog(note));

        // Clic long → menu contextuel
        adapter.setOnNoteLongClickListener((note, anchor) -> showContextMenu(note, anchor));
    }

    private void setupChips(ChipGroup group) {
        String[] categories = {"TOUS", "TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        for (String cat : categories) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setCheckable(true);
            chip.setChecked(cat.equals("TOUS"));
            chip.setOnClickListener(v -> {
                activeCategory = cat.equals("TOUS") ? null : cat;
                if (activeCategory == null) {
                    viewModel.setSearchQuery(etSearch.getText().toString());
                } else {
                    viewModel.getNotesByCategory(activeCategory)
                            .observe(this, notes -> adapter.setNotes(notes));
                }
            });
            group.addView(chip);
        }
    }

    private void showAddNoteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etContent = dialogView.findViewById(R.id.etDialogContent);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerPrio = dialogView.findViewById(R.id.spinnerPriority);
        TextView tvLiveCount = dialogView.findViewById(R.id.tvLiveWordCount);

        // Compteur de mots en direct
        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                int w = s.toString().trim().isEmpty() ? 0
                        : s.toString().trim().split("\\s+").length;
                tvLiveCount.setText(w + " mot" + (w > 1 ? "s" : ""));
            }
        });

        String[] cats = {"TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        String[] prios = {"BASSE", "MOYENNE", "HAUTE"};

        spinnerCat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cats));
        spinnerPrio.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, prios));

        new AlertDialog.Builder(this)
                .setTitle("✦ Nouvelle note")
                .setView(dialogView)
                .setPositiveButton("Enregistrer", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty()) {
                        Toast.makeText(this, "Le titre est obligatoire", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String cat = cats[spinnerCat.getSelectedItemPosition()];
                    int prio = spinnerPrio.getSelectedItemPosition() + 1;
                    viewModel.insert(new Note(title, content, cat, prio));
                    Toast.makeText(this, "Note ajoutée ✓", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showNoteDetailDialog(Note note) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_note, null);

        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etContent = dialogView.findViewById(R.id.etDialogContent);
        Spinner spinnerCat = dialogView.findViewById(R.id.spinnerCategory);
        Spinner spinnerPrio = dialogView.findViewById(R.id.spinnerPriority);
        TextView tvLiveCount = dialogView.findViewById(R.id.tvLiveWordCount);

        etTitle.setText(note.getTitle());
        etContent.setText(note.getContent());

        String[] cats = {"TRAVAIL", "PERSO", "IDÉES", "URGENT"};
        String[] prios = {"BASSE", "MOYENNE", "HAUTE"};

        spinnerCat.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cats));
        spinnerPrio.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, prios));

        for (int i = 0; i < cats.length; i++)
            if (cats[i].equals(note.getCategory())) spinnerCat.setSelection(i);
        spinnerPrio.setSelection(note.getPriority() - 1);

        int w = note.getContent().trim().isEmpty() ? 0
                : note.getContent().trim().split("\\s+").length;
        tvLiveCount.setText(w + " mot" + (w > 1 ? "s" : ""));

        etContent.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                int wc = s.toString().trim().isEmpty() ? 0
                        : s.toString().trim().split("\\s+").length;
                tvLiveCount.setText(wc + " mot" + (wc > 1 ? "s" : ""));
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("✎ Modifier la note")
                .setView(dialogView)
                .setPositiveButton("Mettre à jour", (d, wi) -> {
                    String title = etTitle.getText().toString().trim();
                    String content = etContent.getText().toString().trim();
                    if (title.isEmpty()) return;
                    note.setTitle(title);
                    note.setContent(content);
                    note.setCategory(cats[spinnerCat.getSelectedItemPosition()]);
                    note.setPriority(spinnerPrio.getSelectedItemPosition() + 1);
                    viewModel.update(note);
                    Toast.makeText(this, "Note mise à jour ✓", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showContextMenu(Note note, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, note.isPinned() ? "📌 Désépingler" : "📌 Épingler");
        popup.getMenu().add(0, 2, 0, "🗑 Supprimer");
        popup.getMenu().add(0, 3, 0, "✎ Modifier");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    viewModel.togglePin(note);
                    Toast.makeText(this,
                            note.isPinned() ? "Désépinglée" : "Épinglée 📌",
                            Toast.LENGTH_SHORT).show();
                    return true;
                case 2:
                    confirmDelete(note);
                    return true;
                case 3:
                    showNoteDetailDialog(note);
                    return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmDelete(Note note) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer cette note ?")
                .setMessage("« " + note.getTitle() + " » sera supprimée définitivement.")
                .setPositiveButton("Supprimer", (d, w) -> {
                    viewModel.delete(note);
                    Toast.makeText(this, "Note supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}