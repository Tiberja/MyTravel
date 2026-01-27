package com.example.mytravel;

import android.content.res.ColorStateList;
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
    private String uid;
    private String reiseId;

    private ImageView imgCity, btnBack, btnAddPack, btnAddActivity, btnAddFoodspot;
    private TextView tvCity;

    private LinearLayout packlistContainer;
    private LinearLayout activitiesContainer;
    private LinearLayout foodspotsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        imgCity = findViewById(R.id.imgCity);
        btnBack = findViewById(R.id.btnBack);
        tvCity = findViewById(R.id.tvCity);

        btnAddPack = findViewById(R.id.btnAddPack);
        btnAddActivity = findViewById(R.id.btnAddActivity);
        btnAddFoodspot = findViewById(R.id.btnAddFoodspot);

        packlistContainer = findViewById(R.id.packlistContainer);
        activitiesContainer = findViewById(R.id.activitiesContainer);
        foodspotsContainer = findViewById(R.id.foodspotsContainer);

        btnBack.setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = user.getUid();
        reiseId = getIntent().getStringExtra("reiseId");
        if (reiseId == null || reiseId.trim().isEmpty()) {
            Toast.makeText(this, "Keine Reise-ID übergeben.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        // Trip + Listen laden
        loadTrip();
        loadPacklist();
        loadActivities();
        loadFoodspots();

        // PLUS Buttons (mit echten Buttons im Dialog)
        btnAddPack.setOnClickListener(v ->
                showAddDialogSimple("Packliste hinzufügen", "z.B. Spiegel", this::addPackItem)
        );

        btnAddActivity.setOnClickListener(v ->
                showAddDialogSimple("Aktivität hinzufügen", "z.B. Sagrada Familia", this::addActivity)
        );

        btnAddFoodspot.setOnClickListener(v ->
                showAddDialogSimple("Foodspot hinzufügen", "z.B. Honest Greens", this::addFoodspot)
        );
    }

    // Bild + Stadtname
    private void loadTrip() {
        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Reise nicht gefunden.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String ort = doc.getString("ort");
                    String bild = doc.getString("bild");
                    Timestamp start = doc.getTimestamp("startdatum");
                    Timestamp ende = doc.getTimestamp("enddatum");

                    if (ort != null && !ort.trim().isEmpty()) {
                        tvCity.setText(ort.toUpperCase(Locale.getDefault()));
                    } else {
                        tvCity.setText("CITY");
                    }

                    setImageFromName(bild);

                    Log.d(TAG, "Trip: ort=" + ort + " start=" + formatDate(start) + " ende=" + formatDate(ende));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Trip laden fehlgeschlagen", e);
                    Toast.makeText(this, "Fehler Trip: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setImageFromName(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()) return;

        int resId = getResources().getIdentifier(
                imageName.toLowerCase(Locale.getDefault()),
                "drawable",
                getPackageName()
        );

        if (resId != 0) {
            imgCity.setImageResource(resId);
        } else {
            Log.w(TAG, "Kein Drawable gefunden für: " + imageName);
        }
    }

    private String formatDate(Timestamp ts) {
        if (ts == null) return "-";
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(ts.toDate());
    }

    //PACKLISTE
    private void loadPacklist() {
        packlistContainer.removeAllViews();

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("packliste")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("Keine Packliste gespeichert.");
                        tv.setTextColor(getColor(android.R.color.black));
                        packlistContainer.addView(tv);
                        return;
                    }

                    ColorStateList black = ColorStateList.valueOf(getColor(android.R.color.black));

                    for (QueryDocumentSnapshot d : q) {
                        String name = d.getString("name");
                        boolean checked = Boolean.TRUE.equals(d.getBoolean("checked"));

                        CheckBox cb = new CheckBox(this);
                        cb.setText(name != null ? name : d.getId());
                        cb.setChecked(checked);
                        cb.setTextColor(getColor(android.R.color.black));
                        cb.setButtonTintList(black);
                        cb.setPadding(0, 6, 0, 6);

                        // Haken speichern
                        cb.setOnCheckedChangeListener((buttonView, isChecked) ->
                                d.getReference().update("checked", isChecked)
                                        .addOnFailureListener(e -> Log.e(TAG, "Packlist checked update fail", e))
                        );

                        // Long-Press -> Bearbeiten / Löschen
                        cb.setOnLongClickListener(v -> {
                            String[] options = {"✏️ Bearbeiten", "🗑️ Löschen"};

                            new AlertDialog.Builder(this)
                                    .setTitle("Packliste")
                                    .setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            showEditDialogSimple("Packliste bearbeiten", cb.getText().toString(),
                                                    newText -> d.getReference().update("name", newText)
                                                            .addOnSuccessListener(x -> loadPacklist()));
                                        } else {
                                            showConfirmDeleteSimple("Löschen?", () ->
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
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ACTIVITIES
    private void loadActivities() {
        activitiesContainer.removeAllViews();

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("aktivitäten")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("Keine Aktivitäten gespeichert.");
                        tv.setTextColor(getColor(android.R.color.black));
                        activitiesContainer.addView(tv);
                        return;
                    }

                    for (QueryDocumentSnapshot d : q) {
                        String titel = d.getString("titel");

                        TextView tv = new TextView(this);
                        tv.setText("• " + (titel != null ? titel : d.getId()));
                        tv.setTextColor(getColor(android.R.color.black));
                        tv.setTextSize(16f);
                        tv.setPadding(0, 6, 0, 6);

                        tv.setOnLongClickListener(v -> {
                            String current = (titel != null ? titel : "");
                            String[] options = {"✏️ Bearbeiten", "🗑️ Löschen"};

                            new AlertDialog.Builder(this)
                                    .setTitle("Aktivität")
                                    .setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            showEditDialogSimple("Aktivität bearbeiten", current,
                                                    newText -> d.getReference().update("titel", newText)
                                                            .addOnSuccessListener(x -> loadActivities()));
                                        } else {
                                            showConfirmDeleteSimple("Löschen?", () ->
                                                    d.getReference().delete().addOnSuccessListener(x -> loadActivities())
                                            );
                                        }
                                    })
                                    .show();

                            return true;
                        });

                        activitiesContainer.addView(tv);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Activities laden fail", e);
                    Toast.makeText(this, "Aktivitäten konnten nicht geladen werden.", Toast.LENGTH_LONG).show();
                });
    }

    private void addActivity(String titel) {
        Map<String, Object> m = new HashMap<>();
        m.put("titel", titel);
        m.put("createdAt", FieldValue.serverTimestamp());

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("aktivitäten")
                .add(m)
                .addOnSuccessListener(r -> loadActivities())
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // FOODSPOTS
    private void loadFoodspots() {
        foodspotsContainer.removeAllViews();

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("foodspots")
                .get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        TextView tv = new TextView(this);
                        tv.setText("Keine Foodspots gespeichert.");
                        tv.setTextColor(getColor(android.R.color.black));
                        foodspotsContainer.addView(tv);
                        return;
                    }

                    for (QueryDocumentSnapshot d : q) {
                        String name = d.getString("name");

                        TextView tv = new TextView(this);
                        tv.setText("• " + (name != null ? name : d.getId()));
                        tv.setTextColor(getColor(android.R.color.black));
                        tv.setTextSize(16f);
                        tv.setPadding(0, 6, 0, 6);

                        tv.setOnLongClickListener(v -> {
                            String current = (name != null ? name : "");
                            String[] options = {"✏️ Bearbeiten", "🗑️ Löschen"};

                            new AlertDialog.Builder(this)
                                    .setTitle("Foodspot")
                                    .setItems(options, (dialog, which) -> {
                                        if (which == 0) {
                                            showEditDialogSimple("Foodspot bearbeiten", current,
                                                    newText -> d.getReference().update("name", newText)
                                                            .addOnSuccessListener(x -> loadFoodspots()));
                                        } else {
                                            showConfirmDeleteSimple("Löschen?", () ->
                                                    d.getReference().delete().addOnSuccessListener(x -> loadFoodspots())
                                            );
                                        }
                                    })
                                    .show();

                            return true;
                        });

                        foodspotsContainer.addView(tv);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Foodspots laden fail", e);
                    Toast.makeText(this, "Foodspots konnten nicht geladen werden.", Toast.LENGTH_LONG).show();
                });
    }

    private void addFoodspot(String name) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("createdAt", FieldValue.serverTimestamp());

        db.collection("benutzer").document(uid)
                .collection("reisen").document(reiseId)
                .collection("foodspots")
                .add(m)
                .addOnSuccessListener(r -> loadFoodspots())
                .addOnFailureListener(e -> Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // DIALOGS mit Buttons
    private interface OnTextSaved {
        void onSave(String text);
    }

    private void showAddDialogSimple(String title, String hint, OnTextSaved onSave) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 40, 50, 20);

        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(true);
        root.addView(input);

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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

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

    private void showEditDialogSimple(String title, String currentText, OnTextSaved onSave) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 40, 50, 20);

        EditText input = new EditText(this);
        input.setText(currentText);
        input.setSelection(input.getText().length());
        input.setSingleLine(true);
        root.addView(input);

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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

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

    private void showConfirmDeleteSimple(String title, Runnable onDelete) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 40, 50, 20);

        TextView tv = new TextView(this);
        tv.setText("Willst du das wirklich löschen?");
        tv.setTextSize(16f);
        root.addView(tv);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 30, 0, 0);

        android.widget.Button btnCancel = new android.widget.Button(this);
        btnCancel.setText("Abbrechen");

        android.widget.Button btnDelete = new android.widget.Button(this);
        btnDelete.setText("Löschen");

        btnRow.addView(btnCancel);
        btnRow.addView(btnDelete);
        root.addView(btnRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            onDelete.run();
        });

        dialog.show();
    }
}