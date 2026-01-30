package com.example.mytravel;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytravel.calender.CalendarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // AUTH CHECK
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        LinearLayout tripsContainer = findViewById(R.id.tripsContainer);
        EditText search = findViewById(R.id.suchen);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        List<DocumentSnapshot> allTrips = new ArrayList<>();

        // FIRESTORE LIVE LOAD
        db.collection("benutzer")
                .document(uid)
                .collection("reisen")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        Toast.makeText(this, "Fehler: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
                        return;
                    }

                    allTrips.clear();
                    allTrips.addAll(snap.getDocuments());

                    // WICHTIG: aktuellen Suchtext berücksichtigen
                    renderTrips(tripsContainer, allTrips, search.getText().toString());
                });

        // SEARCH (NUR ANFANGSBUSCHTABEN)
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderTrips(tripsContainer, allTrips, s.toString());
            }
        });

        // NAVIGATION
        ImageView btn = findViewById(R.id.navigation_btn);

        View navRoot = findViewById(R.id.nav_include);
        navRoot.setVisibility(View.GONE);
        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        btn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        navRoot.findViewById(R.id.menu_home)
                .setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        navRoot.findViewById(R.id.menu_calendar)
                .setOnClickListener(v -> {
                    navRoot.setVisibility(View.GONE);
                    startActivity(new Intent(this, CalendarActivity.class));
                    finish();
                });

        navRoot.findViewById(R.id.menu_settings)
                .setOnClickListener(v -> {
                    navRoot.setVisibility(View.GONE);
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                });

        navRoot.findViewById(R.id.menu_newtrip)
                .setOnClickListener(v -> {
                    navRoot.setVisibility(View.GONE);
                    startActivity(new Intent(this, NewTripActivity.class));
                    finish();
                });

        navRoot.findViewById(R.id.menu_worldmap)
                .setOnClickListener(v -> {
                    navRoot.setVisibility(View.GONE);
                    startActivity(new Intent(this, com.example.mytravel.worldmap.WorldMapActivity.class));
                    finish();
                });

        // INSETS
        View main = findViewById(R.id.main);
        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    // ===== RENDER REISEN =====
    private void renderTrips(LinearLayout container, List<DocumentSnapshot> docs, String query) {
        container.removeAllViews();

        String q = query == null ? "" : query.trim().toLowerCase();

        for (DocumentSnapshot d : docs) {
            String ort = d.getString("ort");
            String bild = d.getString("bild");
            String id = d.getId();

            String ortLower = ort == null ? "" : ort.toLowerCase();

            // NUR ANFANG
            boolean match = q.isEmpty() || ortLower.startsWith(q);
            if (!match) continue;

            CardView card = new CardView(this);

            int topMargin = container.getChildCount() == 0 ? dp(20) : dp(30);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(150)
            );
            lp.setMargins(dp(15), topMargin, dp(15), 0);

            card.setLayoutParams(lp);
            card.setRadius(dp(20));
            card.setCardElevation(dp(10));
            card.setUseCompatPadding(false);

            android.widget.FrameLayout frame = new android.widget.FrameLayout(this);

            ImageView img = new ImageView(this);
            img.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (bild != null && !bild.isEmpty()) {

                // 1) NEU: Galerie/Datei (Uri)
                if (bild.startsWith("content://") || bild.startsWith("file://")) {
                    if (bild.startsWith("content://") || bild.startsWith("file://")) {
                        try {
                            img.setImageURI(Uri.parse(bild));
                        } catch (SecurityException e) {
                            img.setImageResource(android.R.drawable.ic_menu_gallery); // fallback
                        }
                    }


                    // 2) ALT: drawable Name ("london")
                } else {
                    int resId = getResources().getIdentifier(bild, "drawable", getPackageName());
                    if (resId != 0) {
                        img.setImageResource(resId);
                    }
                }
            }

            TextView title = new TextView(this);
            android.widget.FrameLayout.LayoutParams tlp =
                    new android.widget.FrameLayout.LayoutParams(
                            android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                            android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
                    );
            tlp.gravity = Gravity.BOTTOM;

            title.setLayoutParams(tlp);
            title.setBackgroundColor(Color.parseColor("#ADD1EF"));
            title.setPadding(dp(6), dp(6), dp(6), dp(6));
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            title.setTextSize(20);
            title.setTypeface(null, Typeface.BOLD);
            title.setText(ort == null ? "" : ort.toUpperCase());

            frame.addView(img);
            frame.addView(title);
            card.addView(frame);

            card.setOnClickListener(v -> {
                Intent i = new Intent(this, TripDetailsActivity.class);
                i.putExtra("reiseId", id);
                startActivity(i);
            });

            container.addView(card);
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
