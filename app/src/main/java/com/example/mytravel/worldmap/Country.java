package com.example.mytravel.worldmap;

//Datenklasse für ein Land
public class Country {
    public final String id;
    public final String name;
    public final int flagRes;

    //Konstruktor
    public Country(String id, String name, int flagRes) {
        this.id = id;
        this.name = name;
        this.flagRes = flagRes;
    }
}
