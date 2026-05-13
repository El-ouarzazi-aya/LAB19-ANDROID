package com.example.roommvvmdemo.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.roommvvmdemo.R;
import com.example.roommvvmdemo.data.local.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder> {

    private List<Note> notes = new ArrayList<>();
    private OnNoteClickListener clickListener;
    private OnNoteLongClickListener longClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteLongClickListener {
        void onNoteLongClick(Note note, View anchor);
    }

    public void setNotes(List<Note> newNotes) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return notes.size(); }
            @Override public int getNewListSize() { return newNotes.size(); }
            @Override public boolean areItemsTheSame(int o, int n) {
                return notes.get(o).getId() == newNotes.get(n).getId();
            }
            @Override public boolean areContentsTheSame(int o, int n) {
                Note a = notes.get(o), b = newNotes.get(n);
                return a.getTitle().equals(b.getTitle())
                        && a.getContent().equals(b.getContent())
                        && a.isPinned() == b.isPinned()
                        && a.getPriority() == b.getPriority();
            }
        });
        notes = newNotes;
        result.dispatchUpdatesTo(this);
    }

    public void setOnNoteClickListener(OnNoteClickListener l) { this.clickListener = l; }
    public void setOnNoteLongClickListener(OnNoteLongClickListener l) { this.longClickListener = l; }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NoteHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder h, int pos) {
        Note note = notes.get(pos);
        h.tvTitle.setText(note.getTitle());
        h.tvContent.setText(note.getContent());
        h.tvCategory.setText(note.getCategory());
        h.tvDate.setText(formatDate(note.getCreatedAt()));

        // Icône épingle
        h.ivPin.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);

        // Couleur selon catégorie
        int color = getCategoryColor(note.getCategory());
        h.viewStripe.setBackgroundColor(color);
        h.tvCategory.setTextColor(color);

        // Badge priorité
        h.tvPriority.setText(getPriorityLabel(note.getPriority()));
        h.tvPriority.setBackgroundTintList(
                ColorStateList.valueOf(getPriorityColor(note.getPriority())));

        // Nombre de mots
        int wordCount = note.getContent().trim().isEmpty() ? 0
                : note.getContent().trim().split("\\s+").length;
        h.tvWordCount.setText(wordCount + " mots");
    }

    @Override
    public int getItemCount() { return notes.size(); }

    private String formatDate(long timestamp) {
        return new SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.FRENCH)
                .format(new Date(timestamp));
    }

    private int getCategoryColor(String cat) {
        switch (cat) {
            case "TRAVAIL":  return Color.parseColor("#4F8EF7");
            case "PERSO":    return Color.parseColor("#34C97B");
            case "IDÉES":    return Color.parseColor("#F7A84F");
            case "URGENT":   return Color.parseColor("#F75F5F");
            default:         return Color.parseColor("#9B8BF4");
        }
    }

    private int getPriorityColor(int p) {
        switch (p) {
            case 3: return Color.parseColor("#F75F5F");
            case 2: return Color.parseColor("#F7A84F");
            default: return Color.parseColor("#34C97B");
        }
    }

    private String getPriorityLabel(int p) {
        switch (p) {
            case 3: return "● HAUTE";
            case 2: return "● MOYENNE";
            default: return "● BASSE";
        }
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        CardView card;
        View viewStripe;
        TextView tvTitle, tvContent, tvCategory, tvDate, tvPriority, tvWordCount;
        ImageView ivPin;

        NoteHolder(@NonNull View v) {
            super(v);
            card = v.findViewById(R.id.cardNote);
            viewStripe = v.findViewById(R.id.viewStripe);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvCategory = v.findViewById(R.id.tvCategory);
            tvDate = v.findViewById(R.id.tvDate);
            tvPriority = v.findViewById(R.id.tvPriority);
            tvWordCount = v.findViewById(R.id.tvWordCount);
            ivPin = v.findViewById(R.id.ivPin);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_POSITION)
                    clickListener.onNoteClick(notes.get(pos));
            });

            v.setOnLongClickListener(view -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    longClickListener.onNoteLongClick(notes.get(pos), view);
                    return true;
                }
                return false;
            });
        }
    }
}