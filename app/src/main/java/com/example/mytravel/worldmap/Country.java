package com.example.mytravel.worldmap;

public class Country {
    public final String id;       // z.B. "germany"
    public final String name;     // "Deutschland"
    public final int flagRes;     // R.drawable.deutschland_flagge

    public Country(String id, String name, int flagRes) {
        this.id = id;
        this.name = name;
        this.flagRes = flagRes;
    }
}
