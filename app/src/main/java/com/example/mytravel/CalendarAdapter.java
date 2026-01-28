package com.example.mytravel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.VH> {

    public interface OnDayClickListener {
        void onDayClick(String date); // "yyyy-MM-dd"
    }

    private final List<String> cells = new ArrayList<>(); // 42 Zellen: date oder ""
    private final Map<String, String> noteByDate = new HashMap<>();
    private final OnDayClickListener listener;

    public CalendarAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setCells(List<String> newCells) {
        cells.clear();
        cells.addAll(newCells);
        notifyDataSetChanged();
    }

    public void setNotes(Map<String, String> notes) {
        noteByDate.clear();
        noteByDate.putAll(notes);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String date = cells.get(position);

        if (date.isEmpty()) {
            h.tvDayNumber.setText("");
            h.tvNotePreview.setText("");
            h.itemView.setAlpha(0.3f);
            h.itemView.setOnClickListener(null);
            return;
        }

        h.itemView.setAlpha(1f);

        // Tag aus "yyyy-MM-dd" holen (letzte 2 Zeichen können auch 1-stellig sein -> simpel lösen)
        String[] parts = date.split("-");
        String day = parts[2]; // "01".."31"
        if (day.startsWith("0")) day = day.substring(1);
        h.tvDayNumber.setText(day);

        String note = noteByDate.get(date);
        h.tvNotePreview.setText(note == null ? "" : note);

        h.itemView.setOnClickListener(v -> listener.onDayClick(date));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvNotePreview;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvNotePreview = itemView.findViewById(R.id.tvNotePreview);
        }
    }
}
