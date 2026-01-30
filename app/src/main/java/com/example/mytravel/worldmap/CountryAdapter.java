package com.example.mytravel.worldmap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mytravel.R;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.VH> {

    public interface OnCountryClick {
        void onClick(Country country);
    }

    private final List<Country> countries;
    private final Set<String> visited;
    private final OnCountryClick listener;

    // Konstruktor
    public CountryAdapter(List<Country> countries,
                          Set<String> visited,
                          OnCountryClick listener) {
        this.countries = countries;
        this.visited = visited;
        this.listener = listener;
    }

    // XML laden (item_country.xml)
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_country, parent, false);
        return new VH(v);
    }

    // Daten ins Item schreiben
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Country c = countries.get(position);
        boolean isVisited = visited.contains(c.id);

        h.countryName.setText(c.name);
        h.flag.setImageResource(c.flagRes);
        h.checkmark.setVisibility(isVisited ? View.VISIBLE : View.INVISIBLE);

        // Klick weitergeben an Activity
        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return countries.size();
    }

    // Liste neu zeichnen
    public void refresh() {
        notifyDataSetChanged();
    }

    // 🔹 ViewHolder
    static class VH extends RecyclerView.ViewHolder {

        ImageView flag;
        TextView countryName;
        TextView checkmark;

        VH(@NonNull View itemView) {
            super(itemView);
            flag = itemView.findViewById(R.id.flagge);
            countryName = itemView.findViewById(R.id.countryName);
            checkmark = itemView.findViewById(R.id.häckchen);
        }
    }
}
