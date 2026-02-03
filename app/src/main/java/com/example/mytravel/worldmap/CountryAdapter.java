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

//Adapter für RecyclerView der Länder
public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.VH> {

    //Adapter meldet Klick auf Land an Activity
    public interface OnCountryClick {
        void onClick(Country country);
    }

    private final List<Country> countries;
    private final Set<String> visited;
    private final OnCountryClick listener;

    // Konstruktor: Adapter bekommt alle Daten
    public CountryAdapter(List<Country> countries,
                          Set<String> visited,
                          OnCountryClick listener) {
        this.countries = countries;
        this.visited = visited;
        this.listener = listener;
    }

    // XML laden (item_country.xml)
    //aufgerufen wenn RecyclerView neue Zeile braucht
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_country, parent, false);
        return new VH(v);
    }

    //aufgerufen um Daten ins Item zu schreiben
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

    //RecyclerView fragt: wie viele Elemente anzeigen
    @Override
    public int getItemCount() {
        return countries.size();
    }

    //Liste neu zeichnen
    public void refresh() {
        notifyDataSetChanged();
    }

    // ViewHolder -> hält Referenzen auf die Views einer Zeile
    static class VH extends RecyclerView.ViewHolder {

        ImageView flag;
        TextView countryName;
        TextView checkmark;

        //ViewHolder-Konstruktor -> verbindet Views aus item_country.xml
        VH(@NonNull View itemView) {
            super(itemView);
            flag = itemView.findViewById(R.id.flagge);
            countryName = itemView.findViewById(R.id.countryName);
            checkmark = itemView.findViewById(R.id.häckchen);
        }
    }
}
