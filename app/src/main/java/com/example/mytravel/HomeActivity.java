package com.example.mytravel;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // ✅ Auth check
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // ✅ Klicks auf Reisen (muss in onCreate sein)
        findViewById(R.id.card_reise1).setOnClickListener(v -> openTrip("reise1"));
        findViewById(R.id.card_reise2).setOnClickListener(v -> openTrip("reise2"));
        findViewById(R.id.card_reise3).setOnClickListener(v -> openTrip("reise3"));
        findViewById(R.id.card_reise4).setOnClickListener(v -> openTrip("reise4"));

        // ✅ NAVIGATION
        ImageView btn = findViewById(R.id.navigation_btn);

        View navRoot = findViewById(R.id.nav_include);
        navRoot.setVisibility(View.GONE); // verhindert Klick-Blocker
        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        // Öffnen / Schließen
        btn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // Klick neben Menü schließt
        backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        View menuHome = navRoot.findViewById(R.id.menu_home);
        View menuCalendar = navRoot.findViewById(R.id.menu_calendar);
        View menuSettings = navRoot.findViewById(R.id.menu_settings);
        View menuNewTrip = navRoot.findViewById(R.id.menu_newtrip);
        View menuWorldMap = navRoot.findViewById(R.id.menu_worldmap);

        // Home nicht neu starten, nur Menü schließen
        menuHome.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        menuCalendar.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, CalendarActivity.class));
            finish();
        });

        menuSettings.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        menuNewTrip.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, NewTripActivity.class));
            finish();
        });

        menuWorldMap.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, WorldMapActivity.class));
            finish();
        });

        // ✅ FIRESTORE: Reisen zählen
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        db.collection("benutzer")
                .document(uid)
                .collection("reisen")
                .get()
                .addOnSuccessListener(q -> {
                    Log.d(TAG, "Reisen gefunden: " + q.size());
                    Toast.makeText(this, "Du hast " + q.size() + " Reisen gespeichert", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Reisen laden fehlgeschlagen", e);
                    Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // ✅ INSETS
        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void openTrip(String reiseId) {
        Intent i = new Intent(this, TripDetailsActivity.class);
        i.putExtra("reiseId", reiseId);
        startActivity(i);
    }
}