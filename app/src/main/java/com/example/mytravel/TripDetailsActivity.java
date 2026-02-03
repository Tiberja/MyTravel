package com.example.mytravel;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TripDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TripDetailsActivity";

    private FirebaseFirestore db;
    private String uid, reiseId;

    private ImageView imgCity, btnBack, btnAddPack, btnAddActivity, btnAddFoodspot;
    private TextView tvCity, tvTripDates;

    private LinearLayout packlistContainer, activitiesContainer, foodspotsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // Views
        imgCity = findViewById(R.id.imgCity);
        btnBack = findViewById(R.id.btnBack);
        tvCity = findViewById(R.id.tvCity);
        tvTripDates = findViewById(R.id.tvTripDates);

        btnAddPack = findViewById(R.id.btnAddPack);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        btnAddFoodspot = findViewById(R.id.btnAddFoodspot);

        packlistContainer = findViewById(R.id.packlistContainer);
        activitiesContainer = findViewById(R.id.activitiesContainer);
        foodspotsContainer = findViewById(R.id.foodspotsContainer);

        btnBack.setOnClickListener(v -> finish());

        // Login prüfen
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        uid = user.getUid();

        // Reise-ID prüfen
        reiseId = getIntent().getStringExtra("reiseId");
        if (reiseId == null || reiseId.trim().isEmpty()) {
            Toast.makeText(this, "Keine Reise-ID übergeben.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Firestore initialisieren
        db = FirebaseFirestore.getInstance();

        // Daten laden: Öffnen Dialoge zum Hinzufügen
        loadTrip();
        loadPacklist();
        loadSimpleList("aktivitaeten", "titel", activitiesContainer, "Keine Aktivitäten gespeichert.");
        loadSimpleList("foodspots", "name", foodspotsContainer, "Keine Foodspots gespeichert.");

        // + Buttons
        btnAddPack.setOnClickListener(v ->
                showTextDialog("Packliste hinzufügen", "z.B. Spiegel", "", this::addPackItem)
        );

        btnAddActivity.setOnClickListener(v ->
                showTextDialog("Aktivität hinzufügen", "z.B. Sagrada Familia", "", this::addActivity)
        );

        btnAddFoodspot.setOnClickListener(v ->
                showTextDialog("Foodspot hinzufügen", "z.B. Honest Greens", "", this::addFoodspot)
        );
    }

    // Lädt Basisinformationen der Reise (Ort, Bild, Datum)
    private void loadTrip() {
        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Reise nicht gefunden.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Ort, Bild und Zeitraum aus Firestore lesen
                    String ort = doc.getString("ort");
                    String bild = doc.getString("bild");
                    Timestamp start = doc.getTimestamp("startdatum");
                    Timestamp ende = doc.getTimestamp("enddatum");

                    tvCity.setText((ort != null && !ort.trim().isEmpty())
                            ? ort.toUpperCase(Locale.getDefault())
                            : "CITY");

                    // Datum formatieren
                    String startStr = formatDate(start);
                    String endStr = formatDate(ende);
                    if (start == null || ende == null) tvTripDates.setText("");
                    else tvTripDates.setText(startStr + " - " + endStr);

                    setImageFromName(bild);

                    Log.d(TAG, "Trip: ort=" + ort + " start=" + startStr + " ende=" + endStr);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Trip laden fehlgeschlagen", e);
                    Toast.makeText(this, "Fehler Trip: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
    // Setzt das Bild der Reise
    private void setImageFromName(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) return;

        // URI aus Galerie/Files
        if (imageName.startsWith("content://") || imageName.startsWith("file://")) {
            try {
                imgCity.setImageURI(Uri.parse(imageName));
            } catch (Exception e) {
                Log.w(TAG, "Bild-URI konnte nicht geladen werden: " + imageName, e);
            }
            return;
        }

        // drawable Name ("paris", "london"...)
        int resId = getResources().getIdentifier(
                imageName.toLowerCase(Locale.getDefault()),
                "drawable",
                getPackageName()
        );

        if (resId != 0) imgCity.setImageResource(resId);
        else Log.w(TAG, "Kein Drawable gefunden für: " + imageName);
    }
    // Wandelt Firestore Timestamp in lesbares Datum um
    private String formatDate(Timestamp ts) {
        if (ts == null) return "-";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(ts.toDate());
    }


    // PACKLISTE (Checkbox)
    private void loadPacklist() {
        packlistContainer.removeAllViews();

        // Packliste aus Firestore laden
        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("packliste")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        showEmptyText(packlistContainer, "Keine Packliste gespeichert.");
                        return;
                    }

                    ColorStateList blackTint = ColorStateList.valueOf(getColor(android.R.color.black));
                    // Für jedes Dokument eine Checkbox erstellen
                    for (QueryDocumentSnapshot d : q) {
                        String name = d.getString("name");
                        boolean checked = Boolean.TRUE.equals(d.getBoolean("checked"));

                        CheckBox cb = new CheckBox(this);
                        cb.setText(name != null ? name : d.getId());
                        cb.setChecked(checked);
                        cb.setTextColor(getColor(android.R.color.black));
                        cb.setButtonTintList(blackTint);
                        cb.setPadding(0, 6, 0, 6);
                        // Haken ändern -> Firestore aktualisieren
                        cb.setOnCheckedChangeListener((buttonView, isChecked) ->
                                d.getReference().update("checked", isChecked)
                                        .addOnFailureListener(e -> Log.e(TAG, "Packlist checked update fail", e))
                        );
                        // LongClick: Bearbeiten oder Löschen
                        cb.setOnLongClickListener(v -> {
                            String current = cb.getText().toString();
                            String[] options = {"✏️ Bearbeiten", "🗑️ Löschen"};

                            new AlertDialog.Builder(this)
                                    .setTitle("Packliste")
                                    .setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            showTextDialog(
                                                    "Packliste bearbeiten",
                                                    "",
                                                    current,
                                                    newText -> d.getReference().update("name", newText)
                                                            .addOnSuccessListener(x -> loadPacklist())
                                            );
                                        } else {
                                            showConfirmDelete("Löschen?", () ->
                                                    d.getReference().delete().addOnSuccessListener(x -> loadPacklist())
                                            );
                                        }
                                    })
                                    .show();
                            return true;
                        });

                        packlistContainer.addView(cb);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Packliste laden fail", e);
                    Toast.makeText(this, "Packliste konnte nicht geladen werden.", Toast.LENGTH_LONG).show();
                });
    }
    // Fügt neuen Eintrag zur Packliste hinzu
    private void addPackItem(String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("checked", false);
        m.put("createdAt", FieldValue.serverTimestamp());

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("packliste")
                .add(m)
                .addOnSuccessListener(r -> loadPacklist())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // SIMPLE LISTS (Activities/Foodspots)
    private void loadSimpleList(String collectionName, String fieldName,
                                LinearLayout container, String emptyText) {
        container.removeAllViews();
        // Firestore Collection laden (aktivitaeten / foodspots)
        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection(collectionName)
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        showEmptyText(container, emptyText);
                        return;
                    }
                    // Für jedes Dokument in der Collection ein Listenelement anzeigen
                    for (QueryDocumentSnapshot d : q) {
                        String value = d.getString(fieldName);
                        String display = (value != null ? value : d.getId());
                        // TextView pro Eintrag erstellen (Anzeige im UI)
                        TextView tv = new TextView(this);
                        tv.setText("• " + display);
                        tv.setTextColor(getColor(android.R.color.black));
                        tv.setTextSize(16f);
                        tv.setPadding(0, 6, 0, 6);
                        // LongClick: Bearbeiten oder Löschen
                        tv.setOnLongClickListener(v -> {
                            String[] options = {"✏️ Bearbeiten", "🗑️ Löschen"};

                            new AlertDialog.Builder(this)
                                    .setTitle(collectionName.equals("aktivitaeten") ? "Aktivität" : "Foodspot")
                                    .setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            showTextDialog(
                                                    (collectionName.equals("aktivitaeten")
                                                            ? "Aktivität bearbeiten"
                                                            : "Foodspot bearbeiten"),
                                                    "",
                                                    display,
                                                    newText -> d.getReference().update(fieldName, newText)
                                                            .addOnSuccessListener(x ->
                                                                    loadSimpleList(collectionName, fieldName, container, emptyText)
                                                            )
                                            );
                                        } else {
                                            showConfirmDelete("Löschen?", () ->
                                                    d.getReference().delete().addOnSuccessListener(x ->
                                                            loadSimpleList(collectionName, fieldName, container, emptyText)
                                                    )
                                            );
                                        }
                                    })
                                    .show();

                            return true;
                        });

                        container.addView(tv);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, collectionName + " laden fail", e);
                    Toast.makeText(this, "Konnte nicht geladen werden.", Toast.LENGTH_LONG).show();
                });
    }
    // Fügt neue Aktivität zur Reise hinzu
    private void addActivity(String titel) {
        Map<String, Object> m = new HashMap<>();
        m.put("titel", titel);
        m.put("createdAt", FieldValue.serverTimestamp());

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("aktivitaeten")
                .add(m)
                .addOnSuccessListener(r ->
                        loadSimpleList("aktivitaeten", "titel", activitiesContainer, "Keine Aktivitäten gespeichert.")
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
    // Fügt neuen Foodspot zur Reise hinzu
    private void addFoodspot(String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("createdAt", FieldValue.serverTimestamp());

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("foodspots")
                .add(m)
                .addOnSuccessListener(r ->
                        loadSimpleList("foodspots", "name", foodspotsContainer, "Keine Foodspots gespeichert.")
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Zeigt Hinweistext, wenn Liste leer ist
    private void showEmptyText(LinearLayout container, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getColor(android.R.color.black));
        container.addView(tv);
    }

    private interface OnTextSaved {
        void onSave(String text);
    }
    // Wiederverwendbarer Dialog zum Hinzufügen/Bearbeiten von Text
    private void showTextDialog(String title, String hint, String initial, OnTextSaved onSave) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 40, 50, 20);

        EditText input = new EditText(this);
        if (hint != null && !hint.isEmpty()) input.setHint(hint);
        if (initial != null && !initial.isEmpty()) {
            input.setText(initial);
            input.setSelection(input.getText().length());
        }
        input.setSingleLine(true);
        root.addView(input);
        // Button-Zeile (Abbrechen / Speichern)
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 30, 0, 0);

        android.widget.Button btnCancel = new android.widget.Button(this);
        btnCancel.setText("Abbrechen");

        android.widget.Button btnSave = new android.widget.Button(this);
        btnSave.setText("Speichern");

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);
        root.addView(btnRow);
        // Dialog erstellen und Layout setzen
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        // Leere Eingabe verhindern
        btnSave.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Bitte etwas eingeben.", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            onSave.onSave(text);
        });

        dialog.show();
    }
    // Bestätigungsdialog vor dem Löschen
    private void showConfirmDelete(String title, Runnable onDelete) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Willst du das wirklich löschen?")
                .setNegativeButton("Abbrechen", (d, w) -> d.dismiss())
                .setPositiveButton("Löschen", (d, w) -> {
                    d.dismiss();
                    onDelete.run();
                })
                .show();
    }
}