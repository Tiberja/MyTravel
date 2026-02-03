package com.example.mytravel.worldmap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devs.vectorchildfinder.VectorChildFinder;

import com.example.mytravel.calendar.CalendarActivity;
import com.example.mytravel.HomeActivity;
import com.example.mytravel.NewTripActivity;
import com.example.mytravel.R;
import com.example.mytravel.SettingsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorldMapActivity extends AppCompatActivity {

    // Ui
    private ImageView mapImage;
    private TextView counterTv;

    // Recycler
    private CountryAdapter adapter;
    private List<Country> countries;

    // State -> IDs besuchter Länder
    private final Set<String> visited = new HashSet<>();

    // Firebase
    private FirebaseFirestore db;
    private FirebaseUser user;

    // Vector helper -> findet Pfade im Vector über android:name
    private VectorChildFinder vectorFinder;

    // Farben
    private static final int COLOR_VISITED = 0xFFADD1EF;
    private static final int COLOR_DEFAULT = 0xFFECECEC;

    //Einstiegspunkt
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_world_map);

        //Views verbinden
        mapImage = findViewById(R.id.imageView);
        counterTv = findViewById(R.id.textView);


        //VectorFinder -> arbeitet mit dem VectorDrawable "world" im ImageView
        vectorFinder = new VectorChildFinder(this, R.drawable.world, mapImage);

        //RecyclerView initialisieren
        RecyclerView rv = findViewById(R.id.countryList);
        rv.setLayoutManager(new LinearLayoutManager(this));

        //Länder
        countries = Arrays.asList(
                new Country("egypt", "Ägypten", R.drawable.aegypten_flagge),
                new Country("brazil", "Brasilien", R.drawable.brasilien_flagge),
                new Country("costa_rica", "Costa Rica", R.drawable.costarica_flagge),
                new Country("germany", "Deutschland", R.drawable.deutschland_flagge),
                new Country("france", "Frankreich", R.drawable.frankreich_flagge),
                new Country("ghana", "Ghana", R.drawable.ghana_flagge),
                new Country("greece", "Griechenland", R.drawable.griechenland_flagge),
                new Country("india", "Indien", R.drawable.indien_flagge),
                new Country("italy", "Italien", R.drawable.italien_flagge),
                new Country("japan", "Japan", R.drawable.japan_flagge),
                new Country("croatia", "Kroatien", R.drawable.kroatien_flagge),
                new Country("morocco", "Marokko", R.drawable.marokko_flagge),
                new Country("mexico", "Mexico", R.drawable.mexico_flagge),
                new Country("netherlands", "Niederlande", R.drawable.niederlande_flagge),
                new Country("portugal", "Portugal", R.drawable.portugal_flagge),
                new Country("sweden", "Schweden", R.drawable.schweden_flagge),
                new Country("spain", "Spanien", R.drawable.spanien_flagge),
                new Country("south_korea", "Südkorea", R.drawable.suedkorea_flagge),
                new Country("turkey", "Türkei", R.drawable.tuerkei_flagge),
                new Country("usa", "USA", R.drawable.usa_flagge)
        );

        //Adapter -> zeigt Liste + meldet Klicks an onCountryClicked
        adapter = new CountryAdapter(countries, visited, this::onCountryClicked);
        rv.setAdapter(adapter);

        updateCounter();

        //Firebase initialisieren
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Besuchte Länder laden (wenn eingeloggt)
        if (user != null) {
            loadVisitedFromFirestore(user.getUid());
        }

        //Navigation
        ImageView btn = findViewById(R.id.navigation_btn);
        View navRoot = findViewById(R.id.nav_include);
        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        btn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(
                    navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        navRoot.findViewById(R.id.menu_home).setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        navRoot.findViewById(R.id.menu_calendar).setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, CalendarActivity.class));
            finish();
        });

        navRoot.findViewById(R.id.menu_settings).setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        navRoot.findViewById(R.id.menu_newtrip).setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, NewTripActivity.class));
            finish();
        });

        navRoot.findViewById(R.id.menu_worldmap).setOnClickListener(v ->
                navRoot.setVisibility(View.GONE)
        );

        //Insets/Padding für Edge-to-Edge setzen
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Klick auf Land in der Liste
    private void onCountryClicked(Country c) {
        boolean nowVisited;

        if (visited.contains(c.id)) {
            visited.remove(c.id);
            nowVisited = false;
        } else {
            visited.add(c.id);
            nowVisited = true;
        }

        //Land im Vector einfärben
        Object p = vectorFinder.findPathByName(c.id);
        if (p != null) {
            try {
                p.getClass()
                        .getMethod("setFillColor", int.class)
                        .invoke(p, nowVisited ? COLOR_VISITED : COLOR_DEFAULT);
                mapImage.invalidate();
            } catch (Exception e) {
                android.util.Log.e("WorldMapDebug", "setFillColor failed for " + c.id, e);
            }
        }


        //UI aktualisieren
        adapter.refresh();
        updateCounter();

        //Firestore speichern/löschen
        if (user != null) {
            saveVisitedToFirestore(user.getUid(), c.id, nowVisited);
        }
    }

    //Firebase -> Laden
    private void loadVisitedFromFirestore(String uid) {
        db.collection("benutzer")
                .document(uid)
                .collection("besuchteLaender")
                .get()
                .addOnSuccessListener(qs -> {
                    visited.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : qs.getDocuments()) {
                        String id = doc.getId();
                        visited.add(id);

                        Object p = vectorFinder.findPathByName(id);
                        if (p != null) {
                            try {
                                p.getClass()
                                        .getMethod("setFillColor", int.class)
                                        .invoke(p, COLOR_VISITED);
                            } catch (Exception e) {
                                android.util.Log.e("WorldMapDebug", "setFillColor failed for " + id, e);
                            }
                        }

                    }

                    mapImage.invalidate();
                    adapter.refresh();
                    updateCounter();
                });
    }

    //Firebase -> Speichern/Löschen
    private void saveVisitedToFirestore(String uid, String countryId, boolean isVisited) {
        com.google.firebase.firestore.DocumentReference ref = db.collection("benutzer")
                .document(uid)
                .collection("besuchteLaender")
                .document(countryId);


        if (isVisited) {
            ref.set(new java.util.HashMap<String, Object>() {{
                put("visited", true);
                put("ts", System.currentTimeMillis());
            }});
        } else {
            ref.delete();
        }
    }

    //Zähler
    private void updateCounter() {
        int count = 0;
        for (Country c : countries) {
            if (visited.contains(c.id)) count++;
        }
        counterTv.setText("Besuchte Länder: " + count);
    }
}
