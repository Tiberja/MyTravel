package com.example.mytravel.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravel.R;

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
    private final Map<String, String> tripByDate = new HashMap<>();
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

    public void setTrips(Map<String, String> trips) {
        tripByDate.clear();
        tripByDate.putAll(trips);
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
            h.tvTripName.setText("");
            h.tvTripName.setVisibility(View.GONE);
            h.tvNotePreview.setText("");
            h.itemView.setAlpha(0.3f);
            h.itemView.setOnClickListener(null);
            h.itemView.setBackgroundColor(0x00000000); // transparent
            return;
        }

        h.itemView.setAlpha(1f);

        // Tag aus "yyyy-MM-dd" holen
        String[] parts = date.split("-");
        String day = parts[2];
        if (day.startsWith("0")) day = day.substring(1);
        h.tvDayNumber.setText(day);

        // Notiz
        String note = noteByDate.get(date);
        h.tvNotePreview.setText(note == null ? "" : note);

        // Reise anzeigen + markieren
        String trip = tripByDate.get(date);
        if (trip != null && !trip.isEmpty()) {
            h.tvTripName.setText(trip);
            h.tvTripName.setVisibility(View.VISIBLE);
            h.itemView.setBackgroundColor(0xFFD6EAF8); // hellblau
        } else {
            h.tvTripName.setText("");
            h.tvTripName.setVisibility(View.GONE);
            h.itemView.setBackgroundColor(0xFFFFFFFF); // weiß
        }

        h.itemView.setOnClickListener(v -> listener.onDayClick(date));
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvTripName, tvNotePreview;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvTripName = itemView.findViewById(R.id.tvTripName);
            tvNotePreview = itemView.findViewById(R.id.tvNotePreview);
        }
    }
}